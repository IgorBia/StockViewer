import { Injectable } from '@angular/core';
import { Candlestick } from './candlestick';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';
import { map } from 'rxjs/internal/operators/map';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ChartService {
    private symbol$ = new BehaviorSubject<string | null>(null);
    currentSymbol$ = this.symbol$.asObservable();

    setSymbol(sym: string | null) { this.symbol$.next(sym); }
    getSymbol(): string | null { return this.symbol$.getValue(); }

    private apiUrl = '/api/v1/candles';

    constructor(private http: HttpClient) { }

    getCandlestickData(symbol: string, interval: string = '1h'): Observable<Candlestick[]> {
        if (!symbol) {
            symbol = 'BTCUSDC';
        }
        // backend zwraca { candles: [...] }
        return this.http.get<{ candles: Candlestick[] }>(`${this.apiUrl}/${symbol}/${interval}`)
            .pipe(map(res => res?.candles ?? []));
    }
}