import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../app/services/auth.service';

export interface VehicleModel {
  modelId?: number;
  modelName: string;
  variant: string;
  fuelType: 'PETROL' | 'DIESEL' | 'EV';
  transmission: 'MANUAL' | 'AUTOMATIC';
  exShowroomPrice: number;
  status?: 'ACTIVE' | 'INACTIVE';
}

@Injectable({ providedIn: 'root' })
export class ModelService {
  private apiUrl = 'http://localhost:8080/api/models';

  constructor(private http: HttpClient, private auth: AuthService) {}

  getAll(): Observable<VehicleModel[]> {
    return this.http.get<VehicleModel[]>(this.apiUrl, { headers: this.auth.getAuthHeaders() });
  }

  getActive(): Observable<VehicleModel[]> {
    return this.http.get<VehicleModel[]>(`${this.apiUrl}/active`, { headers: this.auth.getAuthHeaders() });
  }

  getById(id: number): Observable<VehicleModel> {
    return this.http.get<VehicleModel>(`${this.apiUrl}/${id}`, { headers: this.auth.getAuthHeaders() });
  }

  create(model: VehicleModel): Observable<VehicleModel> {
    return this.http.post<VehicleModel>(this.apiUrl, model, { headers: this.auth.getAuthHeaders() });
  }

  update(id: number, model: VehicleModel): Observable<VehicleModel> {
    return this.http.put<VehicleModel>(`${this.apiUrl}/${id}`, model, { headers: this.auth.getAuthHeaders() });
  }

  delete(id: number): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/${id}`, { headers: this.auth.getAuthHeaders() });
  }
}