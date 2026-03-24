import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../app/services/auth.service';

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

  constructor(private http: HttpClient, private authService: AuthService) {}

  getAll(): Observable<Dealer[]> {
    return this.http.get<Dealer[]>(this.apiUrl, {
      headers: this.authService.getAuthHeaders()
    });
  }

  getById(id: number): Observable<Dealer> {
    return this.http.get<Dealer>(`${this.apiUrl}/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  create(dealer: Dealer): Observable<Dealer> {
    return this.http.post<Dealer>(this.apiUrl, dealer, {
      headers: this.authService.getAuthHeaders()
    });
  }

  update(id: number, dealer: Dealer): Observable<Dealer> {
    return this.http.put<Dealer>(`${this.apiUrl}/${id}`, dealer, {
      headers: this.authService.getAuthHeaders()
    });
  }

  delete(id: number) {
    return this.http.delete(`${this.apiUrl}/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }
}