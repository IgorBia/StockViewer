import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs/internal/BehaviorSubject';
import { firstValueFrom } from 'rxjs/internal/firstValueFrom';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../core/auth/auth.service';
import { map } from 'rxjs/internal/operators/map';

@Injectable({ providedIn: 'root' })
export class TradeHistoryService {

    private _tradeHistory = new BehaviorSubject<Array<{ pairSymbol: string; transactionType: string; timestamp: string; baseAmount: number; quoteAmount: number; price: number; pnl: number }>>([]);
    public tradeHistory$ = this._tradeHistory.asObservable();

    // sorted trade history by timestamp descending so newest trades are first
    readonly sortedTradeHistory$ = this.tradeHistory$.pipe(
        map(list => (list || []).slice().sort((a, b) => {
            const aTime = new Date(a?.timestamp ?? '').getTime();
            const bTime = new Date(b?.timestamp ?? '').getTime();
            return bTime - aTime; // descending
        }))
    );

    private _loading = new BehaviorSubject<boolean>(true);
    readonly loading$ = this._loading.asObservable();

    constructor(private http: HttpClient, private auth: AuthService) {
        this.loadTradeHistory();
    }

    public async loadTradeHistory() {
        try {
            const res: any = await firstValueFrom(this.http.get(`/api/v1/trade/history`, {
                headers: new HttpHeaders().set('Authorization', `Bearer ${this.auth.getToken()}`)
            }));
            const trades = (res || []) as Array<{ pairSymbol: string; transactionType: string; timestamp: string; baseAmount: number; quoteAmount: number; price: number; pnl: number }>;
            console.log('[TradeHistory] loaded trade history:', trades);
            this._tradeHistory.next(trades);
        } catch (err) {
            console.error('[TradeHistory] failed to load trade history', err);
            this._tradeHistory.next([]);
        } finally {
            this._loading.next(false);
        }
    }
}