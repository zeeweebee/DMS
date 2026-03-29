import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { BookingService, CreateBookingRequest } from '../../services/booking.service';
import { EnquiryService, Enquiry } from '../../services/enquiry.service';
import { StockService, VehicleStock } from '../../services/stock.service';
import { AuthService } from '../../app/services/auth.service';

@Component({
  selector: 'app-booking-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './booking-form.html',
  styleUrl: './booking-form.css'
})
export class BookingFormComponent implements OnInit {
  enquiries: Enquiry[] = [];
  availableVins: VehicleStock[] = [];

  enquiryId: number | null = null;
  selectedVin = '';
  bookingDate = new Date().toISOString().split('T')[0];
  bookingAmount: number | null = null;
  useFifo = true;

  selectedEnquiry: Enquiry | null = null;
  role: string | null = null;
  saving = false;
  error = '';

  constructor(
    private bookingService: BookingService,
    private enquiryService: EnquiryService,
    private stockService: StockService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.role = this.authService.getRole();

    // Pre-fill enquiryId from query param (when navigating from enquiry list "Book" button)
    const qEnquiryId = this.route.snapshot.queryParamMap.get('enquiryId');
    if (qEnquiryId) {
      this.enquiryId = Number(qEnquiryId);
    }

    // Load open enquiries (NEGOTIATING only — ready to book)
    this.enquiryService.getAll({ status: 'NEGOTIATING', pageSize: 100 }).subscribe({
      next: ({ content }) => {
        this.enquiries = content;
        if (this.enquiryId) this.onEnquiryChange();
      }
    });
  }

  onEnquiryChange(): void {
    this.selectedEnquiry = this.enquiries.find(e => e.enquiryId === Number(this.enquiryId)) ?? null;
    this.selectedVin = '';
    this.availableVins = [];

    if (this.selectedEnquiry) {
      // Load available stock for this model + dealer for VIN picker
      this.stockService.getAll().subscribe({
        next: stocks => {
          this.availableVins = stocks.filter(s =>
            s.stockStatus === 'AVAILABLE' &&
            s.modelId === this.selectedEnquiry!.modelId &&
            (this.role !== 'ADMIN' || s.dealerId === this.selectedEnquiry!.dealerId)
          );
        }
      });
    }
  }

  save(): void {
    if (!this.enquiryId) { this.error = 'Please select an enquiry.'; return; }

    this.saving = true;
    this.error = '';

    const req: CreateBookingRequest = {
      enquiryId: this.enquiryId,
      bookingDate: this.bookingDate,
      bookingAmount: this.bookingAmount ?? undefined
    };

    if (!this.useFifo && this.selectedVin) {
      req.vin = this.selectedVin;
    }

    this.bookingService.create(req).subscribe({
      next: () => this.router.navigate(['/bookings']),
      error: err => {
        this.saving = false;
        this.error = err?.error?.message ?? 'Failed to create booking.';
      }
    });
  }

  cancel(): void { this.router.navigate(['/bookings']); }
}
