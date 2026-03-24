import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { DealerService, Dealer } from '../../services/dealer';
import { AuthService } from '../../app/services/auth.service';

@Component({
  selector: 'app-dealer-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './dealer-form.html'
})
export class DealerFormComponent implements OnInit {

  dealer: Dealer = {
    dealerName: '',
    address: '',
    cityId: 0,
    stateId: 0,
    phone: '',
    email: ''
  };

  id?: number;

  constructor(
    private dealerService: DealerService,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const param = this.route.snapshot.paramMap.get('id');
    if (param) {
      this.id = Number(param);
      this.dealerService.getById(this.id).subscribe(data => {
        this.dealer = data;
      });
    }
  }

  save() {
    if (this.id) {
      this.dealerService.update(this.id, this.dealer).subscribe(() => {
        this.router.navigate(['/']);
      });
    } else {
      this.dealerService.create(this.dealer).subscribe(() => {
        this.router.navigate(['/']);
      });
    }
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}