import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface Enquiry {
  enquiryId?: number;
  customerId?: number;
  customerName?: string;
  customerPhone?: string;
  customerEmail?: string;
  modelId?: number;
  modelName?: string;
  variant?: string;
  dealerId?: number;
  dealerName?: string;
  source?: string;
  enquiryDate?: string;
  status?: string;
  createdAt?: string;
}

export interface CreateEnquiryRequest {
  customerId?: number | null;
  customerName?: string;
  customerPhone?: string;
  customerEmail?: string;
  modelId: number;
  dealerId?: number | null;
  source?: string;
  enquiryDate?: string;
  status?: string;
}

@Injectable({ providedIn: 'root' })
export class EnquiryService {
  private apiUrl = 'http://localhost:8080/api/enquiries';

  constructor(private http: HttpClient) {}

  getAll(params?: { page?: number; pageSize?: number; keyword?: string; status?: string; dealerId?: number }): Observable<{ content: Enquiry[]; totalCount: number }> {
    return this.http.get<any>(this.apiUrl, { params: { page: 0, pageSize: 50, ...params } as any }).pipe(
      map(r => ({ content: r.data.content, totalCount: r.data.totalCount }))
    );
  }

  getById(id: number): Observable<Enquiry> {
    return this.http.get<any>(`${this.apiUrl}/${id}`).pipe(map(r => r.data));
  }

  create(req: CreateEnquiryRequest): Observable<Enquiry> {
    return this.http.post<any>(this.apiUrl, req).pipe(map(r => r.data));
  }

  updateStatus(id: number, status: string): Observable<Enquiry> {
    return this.http.patch<any>(`${this.apiUrl}/${id}/status`, { status }).pipe(map(r => r.data));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
