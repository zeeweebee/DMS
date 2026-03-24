import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { StockService, VehicleStock, TransferStockRequest } from '../../services/stock.service';
import { DealerService } from '../../services/dealer';
import { AuthService } from '../../app/services/auth.service';

@Component({
  selector: 'app-stock-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './stock-list.html',
  styleUrl: './stock-list.css'
})
export class StockListComponent implements OnInit {
  stocks: VehicleStock[] = [];
  filteredStocks: VehicleStock[] = [];
  role: string | null = null;
  loading = true;
  error = '';
  successMsg = '';

  // Transfer modal state
  showTransfer = false;
  transferVin = '';
  targetDealerId: number | null = null;
  dealers: any[] = [];
  transferring = false;

  // Filter
  statusFilter = 'ALL';

  constructor(
    private stockService: StockService,
    private dealerService: DealerService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.role = this.authService.getRole();
    this.loadStock();
    if (this.isAdmin()) this.loadDealers();
  }

  loadStock(): void {
    this.loading = true;
    this.stockService.getAll().subscribe({
      next: (data) => {
        this.stocks = data;
        this.applyFilter();
        this.loading = false;
      },
      error: () => { this.error = 'Failed to load stock.'; this.loading = false; }
    });
  }

  loadDealers(): void {
    this.dealerService.getAll().subscribe({
      next: (d) => this.dealers = d,
      error: () => {}
    });
  }

  applyFilter(): void {
    this.filteredStocks = this.statusFilter === 'ALL'
      ? [...this.stocks]
      : this.stocks.filter(s => s.stockStatus === this.statusFilter);
  }

  isAdmin(): boolean { return this.role === 'ADMIN'; }
  isDealer(): boolean { return this.role === 'DEALER'; }

  markBooked(vin: string): void {
    this.stockService.markAsBooked(vin).subscribe({
      next: () => { this.flash('Marked as BOOKED'); this.loadStock(); },
      error: (e) => { this.error = e?.error?.message ?? 'Operation failed.'; }
    });
  }

  markSold(vin: string): void {
    this.stockService.markAsSold(vin).subscribe({
      next: () => { this.flash('Marked as SOLD'); this.loadStock(); },
      error: (e) => { this.error = e?.error?.message ?? 'Operation failed.'; }
    });
  }

  openTransfer(vin: string): void {
    this.transferVin = vin;
    this.targetDealerId = null;
    this.showTransfer = true;
  }

  confirmTransfer(): void {
    if (!this.targetDealerId) return;
    this.transferring = true;
    const req: TransferStockRequest = { vin: this.transferVin, targetDealerId: this.targetDealerId };
    this.stockService.transfer(req).subscribe({
      next: () => {
        this.transferring = false;
        this.showTransfer = false;
        this.flash('Stock transferred successfully');
        this.loadStock();
      },
      error: (e) => {
        this.transferring = false;
        this.error = e?.error?.message ?? 'Transfer failed.';
      }
    });
  }

  deleteStock(vin: string): void {
    if (!confirm(`Delete VIN ${vin}?`)) return;
    this.stockService.delete(vin).subscribe({
      next: () => { this.flash('Stock deleted'); this.loadStock(); },
      error: (e) => { this.error = e?.error?.message ?? 'Delete failed.'; }
    });
  }

  flash(msg: string): void {
    this.successMsg = msg;
    this.error = '';
    setTimeout(() => this.successMsg = '', 3000);
  }

  statusClass(s: string): string {
    return { AVAILABLE: 'status-available', BOOKED: 'status-booked', SOLD: 'status-sold' }[s] ?? '';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
