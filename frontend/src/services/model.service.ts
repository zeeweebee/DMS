import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { tap } from 'rxjs/operators'; // Add this to your imports

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
  return this.http.get<any>(this.apiUrl).pipe(
    tap(fullResponse => console.log('Raw Backend Response:', fullResponse)), // <--- ADD THIS
    map(response => response.data.content),
    tap(unwrappedData => console.log('Unwrapped Table Data:', unwrappedData)) // <--- ADD THIS
  );
}

  getActive(): Observable<VehicleModel[]> {
    return this.http.get<any>(`${this.apiUrl}/active`).pipe(
      map(response => response.data) // This one uses List<DTO>, so just .data
    );
  }

  getById(id: number): Observable<VehicleModel> {
    return this.http.get<any>(`${this.apiUrl}/${id}`).pipe(
      map(response => response.data)
    );
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
