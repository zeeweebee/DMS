import { Component, OnInit } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet></router-outlet>`
})
export class App implements OnInit {
  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    if (this.authService.isAuthenticated()) {
      const role = this.authService.getRole();
      if (role === 'ADMIN') {
        this.router.navigate(['/']);
      } else {
        this.router.navigate(['/hello']);
      }
    } else {
      this.router.navigate(['/login']);
    }
  }
}