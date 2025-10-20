import { Injectable } from '@angular/core';
import { Candlestick } from './candlestick';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';

@Injectable({ providedIn: 'root' })
export class ChartService {
    private apiUrl = '/api/v1/candles';

    constructor(private http: HttpClient) { }

    getCandlestickData(symbol: string, interval: string = '1m'): Observable<Candlestick[]> {
        if (!symbol) {
            symbol = 'BTCUSDC';
        }
        return this.http.get<Candlestick[]>(`${this.apiUrl}/${symbol}/${interval}`);
    }
}