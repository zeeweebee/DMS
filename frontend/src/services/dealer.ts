import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Dealer {
  dealerId?: number;
  dealerName: string;
  dealerCode?: string;
  address: string;
  cityId: number;
  stateId: number;
  phone: string;
  email: string;
  status?: string;
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class DealerService {

  private apiUrl = 'http://localhost:8080/api/dealers';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Dealer[]> {
    return this.http.get<Dealer[]>(this.apiUrl);
  }

  getById(id: number): Observable<Dealer> {
    return this.http.get<Dealer>(`${this.apiUrl}/${id}`);
  }

  create(dealer: Dealer): Observable<Dealer> {
    return this.http.post<Dealer>(this.apiUrl, dealer);
  }

  update(id: number, dealer: Dealer): Observable<Dealer> {
    return this.http.put<Dealer>(`${this.apiUrl}/${id}`, dealer);
  }

  delete(id: number) {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}