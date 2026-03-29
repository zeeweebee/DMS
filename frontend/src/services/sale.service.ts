import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface Sale {
  saleId?: number;
  bookingId?: number;
  invoiceNumber?: string;
  saleDate?: string;
  paymentStatus?: string;

  customerId?: number;
  customerName?: string;
  customerPhone?: string;
  customerEmail?: string;

  vin?: string;
  modelName?: string;
  variant?: string;
  fuelType?: string;
  transmission?: string;
  color?: string;
  manufactureDate?: string;

  dealerId?: number;
  dealerName?: string;
  dealerAddress?: string;
  dealerPhone?: string;
  dealerEmail?: string;

  exShowroomPrice?: number;
  bookingAmount?: number;
  salePrice?: number;
  paymentMode?: string;
  loanAmount?: number;
  financeBank?: string;
  exchangeVehicle?: string;
  exchangeValue?: number;
  remarks?: string;

  createdAt?: string;
}

export interface CreateSaleRequest {
  bookingId: number;
  salePrice: number;
  paymentMode: string;
  saleDate?: string;
  loanAmount?: number;
  financeBank?: string;
  exchangeVehicle?: string;
  exchangeValue?: number;
  remarks?: string;
}

@Injectable({ providedIn: 'root' })
export class SaleService {
  private apiUrl = 'http://localhost:8080/api/sales';

  constructor(private http: HttpClient) {}

  getAll(params?: {
    page?: number; pageSize?: number;
    keyword?: string; paymentStatus?: string; dealerId?: number;
  }): Observable<{ content: Sale[]; totalCount: number }> {
    return this.http
      .get<any>(this.apiUrl, { params: { page: 0, pageSize: 50, ...params } as any })
      .pipe(map(r => ({ content: r.data.content, totalCount: r.data.totalCount })));
  }

  getById(id: number): Observable<Sale> {
    return this.http.get<any>(`${this.apiUrl}/${id}`).pipe(map(r => r.data));
  }

  create(req: CreateSaleRequest): Observable<Sale> {
    return this.http.post<any>(this.apiUrl, req).pipe(map(r => r.data));
  }

  /** Downloads the invoice PDF directly from the browser. */
  downloadInvoice(saleId: number, invoiceNumber: string): void {
    const url = `${this.apiUrl}/${saleId}/invoice`;
    // Use fetch with the JWT token from localStorage for the download
    const token = localStorage.getItem('authToken');
    fetch(url, {
      headers: { Authorization: `Bearer ${token}` }
    })
      .then(res => {
        if (!res.ok) throw new Error('Download failed');
        return res.blob();
      })
      .then(blob => {
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = `${invoiceNumber}.pdf`;
        a.click();
        URL.revokeObjectURL(a.href);
      })
      .catch(err => console.error('Invoice download error:', err));
  }
}
