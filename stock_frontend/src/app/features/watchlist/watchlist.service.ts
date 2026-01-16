import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Watchlist } from './watchlist';
import { Observable } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';

@Injectable({ providedIn: 'root' })
export class WatchlistService {
  private getUrl = '/api/v1/watchlist/all';
  private addUrl = '/api/v1/watchlist/item';

  constructor(private http: HttpClient, private auth: AuthService) {}

  getWatchlists(): Observable<Watchlist[]> {
    const token = this.auth.getToken();
    const headers = token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : undefined;
    return this.http.get<Watchlist[]>(this.getUrl, { headers });
  }

  addItemToWatchlist(symbol: string) {
    const token = this.auth.getToken();
    const headers = token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : undefined;
    return this.http.post<Watchlist[]>(this.addUrl, { symbol: symbol, watchlistName: 'Default' }, { headers });
  }

  removeItemFromWatchlist(watchlistName: string, symbol: string) {
    const token = this.auth.getToken();
    const headers = token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : undefined;
    return this.http.delete(`${this.addUrl}`, { headers, body: { symbol: symbol, watchlistName: watchlistName } });
  }
}
