import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { DealerService, Dealer } from '../../services/dealer';
import { AuthService } from '../../app/services/auth.service';
import { PLATFORM_ID } from '@angular/core';

@Component({
  selector: 'app-dealer-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dealer-list.html',
  styleUrl: './dealer-list.css'
})
export class DealerListComponent implements OnInit {

  dealers: Dealer[] = [];
  loading = true;
  error = '';
  platformId = inject(PLATFORM_ID);

  constructor(private dealerService: DealerService, private authService: AuthService, private router: Router, private cdr: ChangeDetectorRef,
) {}

  ngOnInit(): void {
    this.loadDealers();
  }

  loadDealers(): void {
    this.loading = true;
    this.dealerService.getAll().subscribe({
      next: (data) => { this.dealers = data; this.loading = false; this.cdr.detectChanges();},
      error: () => { this.error = 'Failed to load dealers.'; this.loading = false; this.cdr.detectChanges();}
    });
  }

  deleteDealer(id: number) {
    if (!confirm('Delete this dealer?')) return;
    this.dealerService.delete(id).subscribe({
      next: () => this.loadDealers(),
      error: () => { this.error = 'Delete failed.'; }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}