import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { SaleService, CreateSaleRequest } from '../../services/sale.service';
import { BookingService, Booking } from '../../services/booking.service';
import { AuthService } from '../../app/services/auth.service';

@Component({
  selector: 'app-sale-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  providers: [SaleService, BookingService, AuthService],
  templateUrl: './sale-form.html',
  styleUrl: './sale-form.css'
})
export class SaleFormComponent implements OnInit {

  // Confirmed bookings eligible for conversion
  bookings: Booking[] = [];
  selectedBooking: Booking | null = null;

  // Form fields
  bookingId: number | null = null;
  salePrice: number | null = null;
  paymentMode = 'CASH';
  saleDate = new Date().toISOString().split('T')[0];
  loanAmount: number | null = null;
  financeBank = '';
  exchangeVehicle = '';
  exchangeValue: number | null = null;
  remarks = '';

  role: string | null = null;
  saving = false;
  error = '';

  readonly paymentModes = ['CASH', 'LOAN', 'EXCHANGE', 'MIXED'];

  constructor(
    private saleService: SaleService,
    private bookingService: BookingService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.role = this.authService.getRole();

    // Pre-fill from query param (?bookingId=X) if navigating from booking list
    const qbid = this.route.snapshot.queryParamMap.get('bookingId');
    if (qbid) this.bookingId = Number(qbid);

    // Load CONFIRMED, non-sold bookings
    this.bookingService.getAll({ status: 'CONFIRMED', pageSize: 200 }).subscribe({
      next: ({ content }) => {
        this.bookings = content;
        if (this.bookingId) this.onBookingChange();
      },
      error: () => { this.error = 'Failed to load bookings.'; }
    });
  }

  onBookingChange(): void {
    this.selectedBooking = this.bookings.find(b => b.bookingId === Number(this.bookingId)) ?? null;
    // Pre-fill sale price from booking's enquiry model ex-showroom price if available
    // We only have what the booking DTO exposes — seed with null, user fills in
    this.salePrice = null;
    this.loanAmount = null;
    this.financeBank = '';
    this.exchangeVehicle = '';
    this.exchangeValue = null;
  }

  get showLoanFields(): boolean {
    return this.paymentMode === 'LOAN' || this.paymentMode === 'MIXED';
  }

  get showExchangeFields(): boolean {
    return this.paymentMode === 'EXCHANGE' || this.paymentMode === 'MIXED';
  }

  save(): void {
    if (!this.bookingId)  { this.error = 'Please select a booking.'; return; }
    if (!this.salePrice || this.salePrice <= 0) { this.error = 'Sale price is required.'; return; }

    this.saving = true;
    this.error = '';

    const req: CreateSaleRequest = {
      bookingId:      this.bookingId,
      salePrice:      this.salePrice,
      paymentMode:    this.paymentMode,
      saleDate:       this.saleDate || undefined,
      loanAmount:     this.loanAmount ?? undefined,
      financeBank:    this.financeBank || undefined,
      exchangeVehicle: this.exchangeVehicle || undefined,
      exchangeValue:  this.exchangeValue ?? undefined,
      remarks:        this.remarks || undefined
    };

    this.saleService.create(req).subscribe({
      next: sale => {
        // Navigate to sales list and trigger a PDF download automatically
        this.router.navigate(['/sales']).then(() => {
          if (sale.saleId && sale.invoiceNumber) {
            this.saleService.downloadInvoice(sale.saleId, sale.invoiceNumber);
          }
        });
      },
      error: err => {
        this.saving = false;
        this.error = err?.error?.message ?? 'Failed to record sale.';
      }
    });
  }

  cancel(): void { this.router.navigate(['/sales']); }
}
