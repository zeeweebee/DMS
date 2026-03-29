import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface Booking {
  bookingId?: number;
  enquiryId?: number;
  customerId?: number;
  customerName?: string;
  customerPhone?: string;
  vin?: string;
  modelName?: string;
  variant?: string;
  color?: string;
  dealerId?: number;
  dealerName?: string;
  bookingDate?: string;
  bookingAmount?: number;
  bookingStatus?: string;
  cancellationReason?: string;
  refundAmount?: number;
  createdAt?: string;
}

export interface CreateBookingRequest {
  enquiryId: number;
  vin?: string;
  bookingDate?: string;
  bookingAmount?: number;
}

export interface CancelBookingRequest {
  cancellationReason: string;
  refundAmount?: number;
}

@Injectable({ providedIn: 'root' })
export class BookingService {
  private apiUrl = 'http://localhost:8080/api/bookings';

  constructor(private http: HttpClient) {}

  getAll(params?: { page?: number; pageSize?: number; status?: string; dealerId?: number }): Observable<{ content: Booking[]; totalCount: number }> {
    return this.http.get<any>(this.apiUrl, { params: { page: 0, pageSize: 50, ...params } as any }).pipe(
      map(r => ({ content: r.data.content, totalCount: r.data.totalCount }))
    );
  }

  getById(id: number): Observable<Booking> {
    return this.http.get<any>(`${this.apiUrl}/${id}`).pipe(map(r => r.data));
  }

  create(req: CreateBookingRequest): Observable<Booking> {
    return this.http.post<any>(this.apiUrl, req).pipe(map(r => r.data));
  }

  cancel(id: number, req: CancelBookingRequest): Observable<Booking> {
    return this.http.patch<any>(`${this.apiUrl}/${id}/cancel`, req).pipe(map(r => r.data));
  }
}
