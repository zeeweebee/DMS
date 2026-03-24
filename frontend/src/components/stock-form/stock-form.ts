import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { StockService, CreateStockRequest } from '../../services/stock.service';
import { ModelService, VehicleModel } from '../../services/model.service';
import { DealerService } from '../../services/dealer';
import { AuthService } from '../../app/services/auth.service';

@Component({
  selector: 'app-stock-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './stock-form.html',
  styleUrl: './stock-form.css'
})
export class StockFormComponent implements OnInit {
  request: CreateStockRequest = {
    vin: '',
    modelId: 0,
    color: '',
    manufactureDate: ''
  };

  selectedDealerId: number | null = null;
  models: VehicleModel[] = [];
  dealers: any[] = [];

  role: string | null = null;
  saving = false;
  error = '';

  constructor(
    private stockService: StockService,
    private modelService: ModelService,
    private dealerService: DealerService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.role = this.authService.getRole();
    this.modelService.getActive().subscribe({ next: (d) => this.models = d });
    if (this.isAdmin()) {
      this.dealerService.getAll().subscribe({ next: (d) => this.dealers = d });
    }
  }

  isAdmin(): boolean { return this.role === 'ADMIN'; }

  save(): void {
    if (!this.request.vin.trim()) { this.error = 'VIN is required.'; return; }
    if (!this.request.modelId) { this.error = 'Please select a model.'; return; }

    this.saving = true;
    this.error = '';

    const op = this.isAdmin() && this.selectedDealerId
      ? this.stockService.addStockForDealer(this.selectedDealerId, this.request)
      : this.stockService.addStock(this.request);

    op.subscribe({
      next: () => this.router.navigate(['/stock']),
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message ?? 'Failed to add stock.';
      }
    });
  }

  cancel(): void { this.router.navigate(['/stock']); }
}
