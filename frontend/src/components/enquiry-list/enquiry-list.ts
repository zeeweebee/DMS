import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { EnquiryService, Enquiry } from '../../services/enquiry.service';
import { AuthService } from '../../app/services/auth.service';

@Component({
  selector: 'app-enquiry-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  providers: [EnquiryService],
  templateUrl: './enquiry-list.html',
  styleUrl: './enquiry-list.css'
})
export class EnquiryListComponent implements OnInit {
  enquiries: Enquiry[] = [];
  filtered: Enquiry[] = [];
  role: string | null = null;
  loading = true;
  error = '';
  successMsg = '';

  statusFilter = 'ALL';
  keyword = '';

  readonly statuses = ['ALL', 'NEW', 'CONTACTED', 'TEST_DRIVE', 'NEGOTIATING', 'CONVERTED', 'LOST'];

  constructor(
    private enquiryService: EnquiryService,
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
    this.enquiryService.getAll({ pageSize: 100 }).subscribe({
      next: ({ content }) => {
        this.enquiries = content;
        this.applyFilter();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Failed to load enquiries.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  applyFilter(): void {
    let list = [...this.enquiries];
    if (this.statusFilter !== 'ALL') {
      list = list.filter(e => e.status === this.statusFilter);
    }
    if (this.keyword.trim()) {
      const kw = this.keyword.toLowerCase();
      list = list.filter(e =>
        e.customerName?.toLowerCase().includes(kw) ||
        e.customerPhone?.toLowerCase().includes(kw) ||
        e.modelName?.toLowerCase().includes(kw)
      );
    }
    this.filtered = list;
  }

  advanceStatus(enquiry: Enquiry): void {
    const order = ['NEW', 'CONTACTED', 'TEST_DRIVE', 'NEGOTIATING'];
    const idx = order.indexOf(enquiry.status ?? '');
    if (idx === -1 || idx === order.length - 1) return;
    const next = order[idx + 1];
    this.enquiryService.updateStatus(enquiry.enquiryId!, next).subscribe({
      next: updated => {
        const i = this.enquiries.findIndex(e => e.enquiryId === updated.enquiryId);
        if (i > -1) this.enquiries[i] = updated;
        this.applyFilter();
        this.flash('Status updated to ' + next);
      },
      error: e => { this.error = e?.error?.message ?? 'Update failed.'; }
    });
  }

  markLost(enquiry: Enquiry): void {
    if (!confirm('Mark this enquiry as LOST?')) return;
    this.enquiryService.updateStatus(enquiry.enquiryId!, 'LOST').subscribe({
      next: updated => {
        const i = this.enquiries.findIndex(e => e.enquiryId === updated.enquiryId);
        if (i > -1) this.enquiries[i] = updated;
        this.applyFilter();
        this.flash('Enquiry marked as LOST');
      },
      error: e => { this.error = e?.error?.message ?? 'Update failed.'; }
    });
  }

  delete(id: number): void {
    if (!confirm('Delete this enquiry?')) return;
    this.enquiryService.delete(id).subscribe({
      next: () => { this.load(); this.flash('Enquiry deleted'); },
      error: e => { this.error = e?.error?.message ?? 'Delete failed.'; }
    });
  }

  isAdmin(): boolean { return this.role === 'ADMIN'; }

  nextLabel(status: string): string {
    const map: Record<string, string> = {
      NEW: 'Mark Contacted',
      CONTACTED: 'Test Drive',
      TEST_DRIVE: 'Negotiating',
      NEGOTIATING: 'Book →'
    };
    return map[status] ?? '';
  }

  canAdvance(status: string): boolean {
    return ['NEW', 'CONTACTED', 'TEST_DRIVE', 'NEGOTIATING'].includes(status ?? '');
  }

  canBook(enquiry: Enquiry): boolean {
    return enquiry.status === 'NEGOTIATING';
  }

  statusClass(s: string): string {
    const map: Record<string, string> = {
      NEW: 'badge-new',
      CONTACTED: 'badge-contacted',
      TEST_DRIVE: 'badge-testdrive',
      NEGOTIATING: 'badge-negotiating',
      CONVERTED: 'badge-converted',
      LOST: 'badge-lost'
    };
    return map[s] ?? '';
  }

  flash(msg: string): void {
    this.successMsg = msg;
    this.error = '';
    setTimeout(() => this.successMsg = '', 3000);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
