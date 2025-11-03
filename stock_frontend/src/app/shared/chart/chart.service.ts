import { Injectable } from '@angular/core';
import { Candlestick } from './candlestick';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';
import { map } from 'rxjs/internal/operators/map';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ChartService {
    symbol: string = "BTCUSDC"; 

    setSymbol(sym: string) { this.symbol = sym; }
    getSymbol(): string { return this.symbol; }

    private apiUrl = '/api/v1/candles';

    constructor(private http: HttpClient) { }

    getCandlestickData(interval: string = '1h'): Observable<Candlestick[]> {
        if (!this.symbol) {
            this.symbol = 'BTCUSDC';
        }
        // backend zwraca { candles: [...] }
        return this.http.get<{ candles: Candlestick[] }>(`${this.apiUrl}/${this.symbol}/${interval}`)
            .pipe(map(res => res?.candles ?? []));
    }
}