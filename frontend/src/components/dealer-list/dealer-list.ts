import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DealerService, Dealer } from '../../services/dealer';
import { isPlatformBrowser } from '@angular/common';
import { inject, PLATFORM_ID } from '@angular/core';

@Component({
  selector: 'app-dealer-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dealer-list.html'
})
export class DealerListComponent implements OnInit {

  dealers: Dealer[] = [];

  constructor(private dealerService: DealerService) {}

platformId = inject(PLATFORM_ID);

ngOnInit(): void {
  if (isPlatformBrowser(this.platformId)) {
    this.loadDealers();
  }
}
  loadDealers() {
    this.dealerService.getAll().subscribe(data => {
      this.dealers = data;
    });
  }

  deleteDealer(id: number) {
    this.dealerService.delete(id).subscribe(() => {
      this.loadDealers();
    });
  }
}