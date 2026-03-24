import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { ModelService, VehicleModel } from '../../services/model.service';
import { AuthService } from '../../app/services/auth.service';

@Component({
  selector: 'app-model-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './model-list.html',
  styleUrl: './model-list.css'
})
export class ModelListComponent implements OnInit {
  models: VehicleModel[] = [];
  role: string | null = null;
  loading = true;
  error = '';

  constructor(
    private modelService: ModelService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.role = this.authService.getRole();
    this.loadModels();
  }

  loadModels(): void {
    this.loading = true;
    this.modelService.getAll().subscribe({
      next: (data) => { this.models = data; this.loading = false; },
      error: () => { this.error = 'Failed to load models.'; this.loading = false; }
    });
  }

  isAdmin(): boolean { return this.role === 'ADMIN'; }

  delete(id: number): void {
    if (!confirm('Delete this model?')) return;
    this.modelService.delete(id).subscribe({
      next: () => this.loadModels(),
      error: () => { this.error = 'Delete failed.'; }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  fuelBadgeClass(fuel: string): string {
    return { PETROL: 'badge-petrol', DIESEL: 'badge-diesel', EV: 'badge-ev' }[fuel] ?? '';
  }

  statusBadgeClass(status: string): string {
    return status === 'ACTIVE' ? 'badge-active' : 'badge-inactive';
  }
}
