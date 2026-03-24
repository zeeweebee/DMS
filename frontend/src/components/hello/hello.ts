import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../app/services/auth.service';

@Component({
  selector: 'app-hello',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './hello.html'
})
export class HelloComponent implements OnInit {
  role: string | null = null;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    this.authService.userRole$.subscribe(role => {
      this.role = role;
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}