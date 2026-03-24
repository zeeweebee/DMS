import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../app/services/auth.service';

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

  constructor(private http: HttpClient, private auth: AuthService) {}

  getAll(): Observable<VehicleStock[]> {
    return this.http.get<VehicleStock[]>(this.apiUrl, { headers: this.auth.getAuthHeaders() });
  }

  getByVin(vin: string): Observable<VehicleStock> {
    return this.http.get<VehicleStock>(`${this.apiUrl}/${vin}`, { headers: this.auth.getAuthHeaders() });
  }

  /** Dealer: dealerId comes from JWT on backend — never sent from frontend */
  addStock(request: CreateStockRequest): Observable<VehicleStock> {
    return this.http.post<VehicleStock>(this.apiUrl, request, { headers: this.auth.getAuthHeaders() });
  }

  /** Admin only: add stock for a specific dealer */
  addStockForDealer(dealerId: number, request: CreateStockRequest): Observable<VehicleStock> {
    return this.http.post<VehicleStock>(
      `${this.apiUrl}/admin/dealer/${dealerId}`,
      request,
      { headers: this.auth.getAuthHeaders() }
    );
  }

  markAsBooked(vin: string): Observable<VehicleStock> {
    return this.http.patch<VehicleStock>(`${this.apiUrl}/${vin}/book`, {}, { headers: this.auth.getAuthHeaders() });
  }

  markAsSold(vin: string): Observable<VehicleStock> {
    return this.http.patch<VehicleStock>(`${this.apiUrl}/${vin}/sell`, {}, { headers: this.auth.getAuthHeaders() });
  }

  transfer(request: TransferStockRequest): Observable<VehicleStock> {
    return this.http.post<VehicleStock>(`${this.apiUrl}/transfer`, request, { headers: this.auth.getAuthHeaders() });
  }

  delete(vin: string): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/${vin}`, { headers: this.auth.getAuthHeaders() });
  }
}