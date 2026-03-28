import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../app/services/auth.service';

@Component({
  selector: 'app-hello',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './hello.html',
  styleUrl: './hello.css'
})
export class HelloComponent implements OnInit {
  role: string | null = null;
  username: string | null = null;

  readonly modules = [
    {
      icon: '📋',
      name: 'Vehicle Models',
      desc: 'Browse and manage the model catalog',
      link: '/models',
      roles: ['ADMIN', 'DEALER', 'EMPLOYEE'],
      color: '#5b7fff'
    },
    {
      icon: '🏭',
      name: 'Vehicle Stock',
      desc: 'Track inventory, book and sell vehicles',
      link: '/stock',
      roles: ['ADMIN', 'DEALER'],
      color: '#34d399'
    },
    {
      icon: '🏢',
      name: 'Dealers',
      desc: 'Manage dealership accounts',
      link: '/dealers',
      roles: ['ADMIN'],
      color: '#f59e0b'
    }
  ];

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    this.authService.userRole$.subscribe(role => { this.role = role; });
  }

  get visibleModules() {
    return this.modules.filter(m => m.roles.includes(this.role ?? ''));
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
