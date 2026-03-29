import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface Customer {
  customerId?: number;
  customerName: string;
  phone?: string;
  email?: string;
  createdAt?: string;
}

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private apiUrl = 'http://localhost:8080/api/customers';

  constructor(private http: HttpClient) {}

  getAll(params?: { keyword?: string; page?: number; pageSize?: number }): Observable<Customer[]> {
    return this.http.get<any>(this.apiUrl, { params: { page: 0, pageSize: 100, ...params } }).pipe(
      map(r => r.data.content)
    );
  }

  getById(id: number): Observable<Customer> {
    return this.http.get<any>(`${this.apiUrl}/${id}`).pipe(map(r => r.data));
  }

  create(customer: Customer): Observable<Customer> {
    return this.http.post<any>(this.apiUrl, customer).pipe(map(r => r.data));
  }

  update(id: number, customer: Customer): Observable<Customer> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, customer).pipe(map(r => r.data));
  }
}
