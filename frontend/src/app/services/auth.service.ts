import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private tokenKey = 'authToken';
  private roleKey = 'userRole';
  private isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  private userRoleSubject = new BehaviorSubject<string | null>(this.getRole());

  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();
  public userRole$ = this.userRoleSubject.asObservable();

  constructor(private http: HttpClient) {}

login(username: string, password: string): Observable<any> {
  return this.http.post(`${this.apiUrl}/login`, { username, password }).pipe(
    tap((response: any) => {
      console.log('Backend Response:', response); // Debugging line

      // Your backend returns { success: true, data: { token: '...', role: '...' } }
      // So we must access response.data
      if (response && response.data && response.data.token) {
        this.setItem(this.tokenKey, response.data.token);
        this.setItem(this.roleKey, response.data.role);
        
        this.isAuthenticatedSubject.next(true);
        this.userRoleSubject.next(response.data.role);
        
        console.log('Token saved to localStorage');
      } else {
        console.error('Token not found in response structure', response);
      }
    })
  );
}
  logout(): void {
    this.removeItem(this.tokenKey);
    this.removeItem(this.roleKey);
    this.isAuthenticatedSubject.next(false);
    this.userRoleSubject.next(null);
  }

  getToken(): string | null {
    return this.getItem(this.tokenKey);
  }

  getRole(): string | null {
    return this.getItem(this.roleKey);
  }

  getRoles(): string[] {
    const role = this.getRole();
    return role ? role.split(',').map(r => r.trim().toUpperCase()) : [];
  }

  hasRole(role: string): boolean {
    return this.getRoles().includes(role.toUpperCase());
  }

  hasAnyRole(roles: string[]): boolean {
    return roles.some(r => this.hasRole(r));
  }

  isAuthenticated(): boolean {
    return this.hasToken();
  }

  private hasToken(): boolean {
    return !!this.getToken();
  }

  private getItem(key: string): string | null {
    return this.isBrowser ? localStorage.getItem(key) : null;
  }

  private setItem(key: string, value: string): void {
    if (this.isBrowser) localStorage.setItem(key, value);
  }

  private removeItem(key: string): void {
    if (this.isBrowser) localStorage.removeItem(key);
  }
}
