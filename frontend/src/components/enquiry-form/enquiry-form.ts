import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { EnquiryService, CreateEnquiryRequest } from '../../services/enquiry.service';
import { ModelService, VehicleModel } from '../../services/model.service';
import { DealerService } from '../../services/dealer';
import { CustomerService, Customer } from '../../services/customer.service';
import { AuthService } from '../../app/services/auth.service';

@Component({
  selector: 'app-enquiry-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './enquiry-form.html',
  styleUrl: './enquiry-form.css',
  providers: [EnquiryService, ModelService, DealerService, CustomerService]
})
export class EnquiryFormComponent implements OnInit {
  // Customer section — toggle between existing and new
  useExistingCustomer = false;
  existingCustomerId: number | null = null;
  customers: Customer[] = [];

  // New customer fields
  customerName = '';
  customerPhone = '';
  customerEmail = '';

  // Enquiry fields
  modelId: number = 0;
  dealerId: number | null = null;
  source = '';
  enquiryDate = new Date().toISOString().split('T')[0];
  status = 'NEW';

  // Dropdowns
  models: VehicleModel[] = [];
  dealers: any[] = [];

  role: string | null = null;
  saving = false;
  error = '';

  readonly sources = ['Walk-in', 'Phone', 'Website', 'Referral', 'Social Media', 'Other'];
  readonly statuses = ['NEW', 'CONTACTED', 'TEST_DRIVE', 'NEGOTIATING'];

  constructor(
    private enquiryService: EnquiryService,
    private modelService: ModelService,
    private dealerService: DealerService,
    private customerService: CustomerService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.role = this.authService.getRole();
    this.modelService.getActive().subscribe({ next: m => this.models = m });
    if (this.isAdmin()) {
      this.dealerService.getAll().subscribe({ next: d => this.dealers = d });
      this.customerService.getAll().subscribe({ next: c => this.customers = c });
    } else {
      this.customerService.getAll().subscribe({ next: c => this.customers = c });
    }
  }

  isAdmin(): boolean { return this.role === 'ADMIN'; }

  save(): void {
    if (!this.modelId) { this.error = 'Please select a vehicle model.'; return; }
    if (this.isAdmin() && !this.dealerId) { this.error = 'Please select a dealer.'; return; }
    if (this.useExistingCustomer && !this.existingCustomerId) { this.error = 'Please select a customer.'; return; }
    if (!this.useExistingCustomer && !this.customerName.trim()) { this.error = 'Customer name is required.'; return; }

    this.saving = true;
    this.error = '';

    const req: CreateEnquiryRequest = {
      modelId: this.modelId,
      dealerId: this.dealerId,
      source: this.source,
      enquiryDate: this.enquiryDate,
      status: this.status
    };

    if (this.useExistingCustomer) {
      req.customerId = this.existingCustomerId;
    } else {
      req.customerName = this.customerName;
      req.customerPhone = this.customerPhone;
      req.customerEmail = this.customerEmail;
    }

    this.enquiryService.create(req).subscribe({
      next: () => this.router.navigate(['/enquiries']),
      error: err => {
        this.saving = false;
        this.error = err?.error?.message ?? 'Failed to create enquiry.';
      }
    });
  }

  cancel(): void { this.router.navigate(['/enquiries']); }
}
