import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { AuthResponseDTO } from './auth-response.dto';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private apiUrl = '/api/v1/users';

    constructor(private http: HttpClient) {}

    register(data: {email: string, password: string}): Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/register`, data);
    }

    login(credentials: {email: string, password: string}): Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/login`, credentials).pipe(
        tap(res => localStorage.setItem('token', res.token)));
    }

    logout() {
        localStorage.removeItem('token');
    }

    isLoggedIn(): boolean {
        return !!localStorage.getItem('token');
    }

    getToken(): string | null {
        return localStorage.getItem('token');
    }

    saveDTO(dto: AuthResponseDTO) {
        localStorage.setItem('token', dto.accessToken);
        localStorage.setItem('user', JSON.stringify(dto.userDetails));
    }
}
