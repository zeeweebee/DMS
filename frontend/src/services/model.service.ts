import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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

  constructor(private http: HttpClient) {}

  getAll(): Observable<VehicleModel[]> {
    return this.http.get<VehicleModel[]>(this.apiUrl);
  }

  getActive(): Observable<VehicleModel[]> {
    return this.http.get<VehicleModel[]>(`${this.apiUrl}/active`);
  }

  getById(id: number): Observable<VehicleModel> {
    return this.http.get<VehicleModel>(`${this.apiUrl}/${id}`);
  }

  create(model: VehicleModel): Observable<VehicleModel> {
    return this.http.post<VehicleModel>(this.apiUrl, model);
  }

  update(id: number, model: VehicleModel): Observable<VehicleModel> {
    return this.http.put<VehicleModel>(`${this.apiUrl}/${id}`, model);
  }

  delete(id: number): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/${id}`);
  }
}
