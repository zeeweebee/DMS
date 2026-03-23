import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DealerService, Dealer } from '../../services/dealer';
import { Observable } from 'rxjs';
import { PLATFORM_ID } from '@angular/core';

@Component({
  selector: 'app-dealer-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dealer-list.html'
})
export class DealerListComponent {

  dealers$: Observable<Dealer[]>; // Observable for async pipe
  platformId = inject(PLATFORM_ID);

  constructor(private dealerService: DealerService) {
    this.dealers$ = this.dealerService.getAll(); // Assign observable directly
  }

  // Delete dealer and refresh observable
  deleteDealer(id: number) {
    this.dealerService.delete(id).subscribe(() => {
      this.dealers$ = this.dealerService.getAll(); // Reassign observable to refresh table
    });
  }
}