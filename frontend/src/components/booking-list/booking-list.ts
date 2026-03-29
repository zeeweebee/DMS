import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { BookingService, Booking, CancelBookingRequest } from '../../services/booking.service';
import { AuthService } from '../../app/services/auth.service';

@Component({
  selector: 'app-booking-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './booking-list.html',
  styleUrl: './booking-list.css'
})
export class BookingListComponent implements OnInit {
  bookings: Booking[] = [];
  filtered: Booking[] = [];
  role: string | null = null;
  loading = true;
  error = '';
  successMsg = '';

  statusFilter = 'ALL';

  // Cancel modal
  showCancelModal = false;
  cancellingId: number | null = null;
  cancelReason = '';
  refundAmount: number | null = null;
  cancelling = false;

  readonly statuses = ['ALL', 'CONFIRMED', 'PENDING', 'CANCELLED'];

  constructor(
    private bookingService: BookingService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.role = this.authService.getRole();
    this.load();
  }

  load(): void {
    this.loading = true;
    this.bookingService.getAll({ pageSize: 100 }).subscribe({
      next: ({ content }: { content: Booking[] }) => {
        this.bookings = content;
        this.applyFilter();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Failed to load bookings.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  applyFilter(): void {
    this.filtered = this.statusFilter === 'ALL'
      ? [...this.bookings]
      : this.bookings.filter(b => b.bookingStatus === this.statusFilter);
  }

  openCancel(id: number): void {
    this.cancellingId = id;
    this.cancelReason = '';
    this.refundAmount = null;
    this.showCancelModal = true;
  }

  confirmCancel(): void {
    if (!this.cancellingId) return;
    if (!this.cancelReason.trim()) { this.error = 'Cancellation reason is required.'; return; }
    this.cancelling = true;
    const req: CancelBookingRequest = {
      cancellationReason: this.cancelReason,
      refundAmount: this.refundAmount ?? undefined
    };
    this.bookingService.cancel(this.cancellingId, req).subscribe({
      next: () => {
        this.cancelling = false;
        this.showCancelModal = false;
        this.flash('Booking cancelled — VIN returned to AVAILABLE');
        this.load();
      },
      error: e => {
        this.cancelling = false;
        this.error = e?.error?.message ?? 'Cancellation failed.';
      }
    });
  }

  isAdmin(): boolean { return this.role === 'ADMIN'; }

  statusClass(s: string): string {
    return {
      CONFIRMED: 'badge-confirmed',
      PENDING:   'badge-pending',
      CANCELLED: 'badge-cancelled'
    }[s] ?? '';
  }

  flash(msg: string): void {
    this.successMsg = msg;
    this.error = '';
    setTimeout(() => this.successMsg = '', 4000);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
