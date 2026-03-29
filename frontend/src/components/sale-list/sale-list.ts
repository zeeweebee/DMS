import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { SaleService, Sale } from '../../services/sale.service';
import { AuthService } from '../../app/services/auth.service';

@Component({
  selector: 'app-sale-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  providers: [SaleService, AuthService],
  templateUrl: './sale-list.html',
  styleUrl: './sale-list.css'
})
export class SaleListComponent implements OnInit {
  sales: Sale[] = [];
  filtered: Sale[] = [];
  role: string | null = null;
  loading = true;
  error = '';
  successMsg = '';
  keyword = '';
  statusFilter = 'ALL';
  downloadingId: number | null = null;

  readonly statuses = ['ALL', 'PAID', 'PENDING'];

  constructor(
    private saleService: SaleService,
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
    this.saleService.getAll({ pageSize: 100 }).subscribe({
      next: ({ content }) => {
        this.sales = content;
        this.applyFilter();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Failed to load sales.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  applyFilter(): void {
    let list = [...this.sales];
    if (this.statusFilter !== 'ALL') {
      list = list.filter(s => s.paymentStatus === this.statusFilter);
    }
    if (this.keyword.trim()) {
      const kw = this.keyword.toLowerCase();
      list = list.filter(s =>
        s.invoiceNumber?.toLowerCase().includes(kw) ||
        s.customerName?.toLowerCase().includes(kw) ||
        s.vin?.toLowerCase().includes(kw) ||
        s.modelName?.toLowerCase().includes(kw)
      );
    }
    this.filtered = list;
  }

  downloadInvoice(sale: Sale): void {
    if (!sale.saleId || !sale.invoiceNumber) return;
    this.downloadingId = sale.saleId;
    try {
      this.saleService.downloadInvoice(sale.saleId, sale.invoiceNumber);
      this.flash('Invoice download started — ' + sale.invoiceNumber);
    } finally {
      setTimeout(() => { this.downloadingId = null; this.cdr.detectChanges(); }, 1500);
    }
  }

  isAdmin(): boolean { return this.role === 'ADMIN'; }

  statusClass(s: string): string {
    return s === 'PAID' ? 'badge-paid' : 'badge-pending';
  }

  paymentModeClass(m: string): string {
    const map: Record<string, string> = {
      CASH: 'mode-cash', LOAN: 'mode-loan',
      EXCHANGE: 'mode-exchange', MIXED: 'mode-mixed'
    };
    return map[m] ?? '';
  }

  flash(msg: string): void {
    this.successMsg = msg;
    this.error = '';
    setTimeout(() => { this.successMsg = ''; this.cdr.detectChanges(); }, 4000);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
