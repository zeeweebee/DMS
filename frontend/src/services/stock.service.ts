import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface VehicleStock {
  vin: string;
  modelId?: number;
  modelName?: string;
  variant?: string;
  dealerId?: number;
  dealerName?: string;
  color: string;
  manufactureDate: string;
  stockStatus?: 'AVAILABLE' | 'BOOKED' | 'SOLD';
  createdAt?: string;
}

export interface CreateStockRequest {
  vin: string;
  modelId: number;
  color: string;
  manufactureDate: string;
}

export interface TransferStockRequest {
  vin: string;
  targetDealerId: number;
}

@Injectable({ providedIn: 'root' })
export class StockService {
  private apiUrl = 'http://localhost:8080/api/stock';

  constructor(private http: HttpClient) {}

  getAll(): Observable<VehicleStock[]> {
    return this.http.get<any>(this.apiUrl).pipe(
      map(response => response.data.content) // Extract the array
    );
  }

  getByVin(vin: string): Observable<VehicleStock> {
    return this.http.get<VehicleStock>(`${this.apiUrl}/${vin}`);
  }

  addStock(request: CreateStockRequest): Observable<VehicleStock> {
    return this.http.post<VehicleStock>(this.apiUrl, request);
  }

  addStockForDealer(dealerId: number, request: CreateStockRequest): Observable<VehicleStock> {
    return this.http.post<VehicleStock>(`${this.apiUrl}/admin/dealer/${dealerId}`, request);
  }

  markAsBooked(vin: string): Observable<VehicleStock> {
    return this.http.patch<VehicleStock>(`${this.apiUrl}/${vin}/book`, {});
  }

  markAsSold(vin: string): Observable<VehicleStock> {
    return this.http.patch<VehicleStock>(`${this.apiUrl}/${vin}/sell`, {});
  }

  transfer(request: TransferStockRequest): Observable<VehicleStock> {
    return this.http.post<VehicleStock>(`${this.apiUrl}/transfer`, request);
  }

  delete(vin: string): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/${vin}`);
  }
}
