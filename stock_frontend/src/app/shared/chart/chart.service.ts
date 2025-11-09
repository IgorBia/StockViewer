import { Injectable } from '@angular/core';
import { Candlestick } from './candlestick';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';
import { map } from 'rxjs/internal/operators/map';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ChartService {
    symbol: string = "BTCUSDC"; 
    interval: string = "1h";

    setSymbol(sym: string) { this.symbol = sym; }
    getSymbol(): string { return this.symbol; }

    setInterval(interval: string) { this.interval = interval; }
    getInterval(): string { return this.interval; }

    private apiUrl = '/api/v1/candles';

    constructor(private http: HttpClient) { }

    getCandlestickData(): Observable<Candlestick[]> {
        if (!this.symbol) {
            this.symbol = 'BTCUSDC';
        }
        // backend zwraca { candles: [...] }
        return this.http.get<{ candles: Candlestick[] }>(`${this.apiUrl}/${this.symbol}/${this.interval}`)
            .pipe(map(res => res?.candles ?? []));
    }
}