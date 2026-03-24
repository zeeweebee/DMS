import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ModelService, VehicleModel } from '../../services/model.service';
import { AuthService } from '../../app/services/auth.service';

@Component({
  selector: 'app-model-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './model-form.html',
  styleUrl: './model-form.css'
})
export class ModelFormComponent implements OnInit {
  model: VehicleModel = {
    modelName: '',
    variant: '',
    fuelType: 'PETROL',
    transmission: 'MANUAL',
    exShowroomPrice: 0,
    status: 'ACTIVE'
  };

  id?: number;
  saving = false;
  error = '';

  fuelTypes = ['PETROL', 'DIESEL', 'EV'];
  transmissions = ['MANUAL', 'AUTOMATIC'];
  statuses = ['ACTIVE', 'INACTIVE'];

  constructor(
    private modelService: ModelService,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const param = this.route.snapshot.paramMap.get('id');
    if (param) {
      this.id = Number(param);
      this.modelService.getById(this.id).subscribe({
        next: (data) => this.model = data,
        error: () => { this.error = 'Failed to load model.'; }
      });
    }
  }

  get isEdit(): boolean { return !!this.id; }

  save(): void {
    this.saving = true;
    this.error = '';

    const op = this.isEdit
      ? this.modelService.update(this.id!, this.model)
      : this.modelService.create(this.model);

    op.subscribe({
      next: () => this.router.navigate(['/models']),
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message ?? 'Save failed. Please try again.';
      }
    });
  }

  cancel(): void { this.router.navigate(['/models']); }
}
