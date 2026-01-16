import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom, Observable } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';

export interface OwnedAsset {
  name: string;
  amount: number;
  avgPrice: number;
}

@Injectable({ providedIn: 'root' })
export class TradeService {
  constructor(private http: HttpClient, private auth: AuthService) {}

  private getHeaders(): HttpHeaders {
    const token = this.auth?.getToken?.() || localStorage.getItem('access_token');
    return token ? new HttpHeaders().set('Authorization', `Bearer ${token}`) : new HttpHeaders();
  }

  executeTrade(payload: { pairSymbol: string; transactionType: string; amount: number }): Observable<any> {
    const headers = this.getHeaders();
    return this.http.post('/api/v1/trade/execute', payload, { headers });
  }

  getOwnedAssets(): Observable<OwnedAsset[]> {
    const headers = this.getHeaders();
    return this.http.get<OwnedAsset[]>('/api/v1/wallet', { headers });
  }

  getOwnedAsset(assetSymbol: string): Observable<OwnedAsset> {
    const headers = this.getHeaders();
    return this.http.get<OwnedAsset>(`/api/v1/wallet/${assetSymbol}`, { headers });
  }

  async getTickerPrice(symbol: string): Promise<number> {
    const resp = await firstValueFrom(
      this.http.get<{ symbol: string; price: number }>(`/api/v1/market/ticker/${symbol}`)
    );
    return resp.price;
  }

  enableManagedAsset(riskTolerance: number) {
    const headers = this.getHeaders();
    return this.http.post<void>('/api/v1/trade/setManagedAsset', { riskTolerance }, { headers });
  }
}