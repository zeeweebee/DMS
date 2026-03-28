import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { DealerService, Dealer } from '../../services/dealer';

@Component({
  selector: 'app-dealer-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './dealer-form.html',
  styleUrl: './dealer-form.css'
})
export class DealerFormComponent implements OnInit {
  dealer: Dealer = { dealerName: '', address: '', cityId: 0, stateId: 0, phone: '', email: '' };
  id?: number;
  saving = false;
  error = '';

  constructor(
    private dealerService: DealerService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const param = this.route.snapshot.paramMap.get('id');
    if (param) {
      this.id = Number(param);
      this.dealerService.getById(this.id).subscribe({
        next: (data) => this.dealer = data,
        error: () => { this.error = 'Failed to load dealer.'; }
      });
    }
  }

  save() {
    this.saving = true;
    this.error = '';
    const op = this.id
      ? this.dealerService.update(this.id, this.dealer)
      : this.dealerService.create(this.dealer);

    op.subscribe({
      next: () => this.router.navigate(['/dealers']),
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message ?? 'Failed to save dealer.';
      }
    });
  }

  cancel() { this.router.navigate(['/dealers']); }
}
