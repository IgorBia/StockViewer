import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Watchlist } from './watchlist';
import { Observable } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';

@Injectable({ providedIn: 'root' })
export class WatchlistService {
  private apiUrl = '/api/v1/watchlist/all';

  constructor(private http: HttpClient, private auth: AuthService) {}

  getWatchlists(): Observable<Watchlist[]> {
    const token = this.auth.getToken();
    const headers = token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : undefined;
    return this.http.get<Watchlist[]>(this.apiUrl, { headers });
  }
}
