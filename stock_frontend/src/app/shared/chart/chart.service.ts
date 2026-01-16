import { Injectable } from '@angular/core';
import { Candlestick } from './candlestick';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';
import { map } from 'rxjs/internal/operators/map';
import { tap } from 'rxjs/operators';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ChartService {
  private symbol$ = new BehaviorSubject<string>('BTCUSDC');
  private interval$ = new BehaviorSubject<string>('1h');
  private baseAsset$ = new BehaviorSubject<string>('BTC'); 
  private quoteAsset$ = new BehaviorSubject<string>('USDC');

  getBaseAsset(): Observable<string> { return this.baseAsset$.asObservable(); }
  getQuoteAsset(): Observable<string> { return this.quoteAsset$.asObservable(); }
  setBaseAsset(base: string) { 
    this.baseAsset$.next(base); 
    console.debug('[ChartService] Base asset set to', base);
  }
  setQuoteAsset(quote: string) { 
    this.quoteAsset$.next(quote);
    console.debug('[ChartService] Quote asset set to', quote);
  }

  setSymbol(sym: string) { 
    this.symbol$.next(sym);
    console.debug('[ChartService] Symbol set to', sym);
  }
  getSymbol(): string { return this.symbol$.getValue(); }
  symbolChanges(): Observable<string> { return this.symbol$.asObservable(); }

  setInterval(interval: string) { this.interval$.next(interval); }
  getInterval(): string { return this.interval$.getValue(); }
  intervalChanges(): Observable<string> { return this.interval$.asObservable(); }

  private apiUrl = '/api/v1/candles';

  constructor(private http: HttpClient) {
    // whenever the symbol changes, refresh base/quote asset info
    this.symbol$.subscribe(sym => {
      this.updatePairInfo().subscribe({
        next: () => console.log('[ChartService] pair info updated for', sym),
        error: (err) => console.error('[ChartService] failed to update pair info for', sym, err)
      });
    });
  }

  getCandlestickData(): Observable<Candlestick[]> {
    const symbol = this.getSymbol() || 'BTCUSDC';
    const interval = this.getInterval() || '1h';
    return this.http.get<{ candles: Candlestick[] }>(`${this.apiUrl}/${symbol}/${interval}`)
      .pipe(map(res => res?.candles ?? []));
  }

  updatePairInfo() {
    const symbol = this.getSymbol();
    return this.http.get<{ baseAsset: Asset; quoteAsset: Asset }>(`${this.apiUrl}/${symbol}/info`)
      .pipe(tap(res => {
        this.setBaseAsset(res.baseAsset.symbol);
        this.setQuoteAsset(res.quoteAsset.symbol);
      }));
  }

  refreshPairData() {
    return this.updatePairInfo().subscribe({
      next: () => console.log('[ChartService] pair info refreshed for', this.getSymbol()),
      error: (err) => console.error('[ChartService] failed to refresh pair info for', this.getSymbol(), err)
    });
  }
}

interface Asset {
  id: string;
  symbol: string;
  displayName: string;
}