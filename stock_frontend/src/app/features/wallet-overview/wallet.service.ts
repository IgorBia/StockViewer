import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../core/auth/auth.service';
import { OwnedAsset } from '../trade/trade.service';
import { firstValueFrom } from 'rxjs';
import { BehaviorSubject } from 'rxjs';
import { TradeService } from '../trade/trade.service';
import { map } from 'rxjs/operators'; // na górze pliku obok importów


@Injectable({ providedIn: 'root' })
export class WalletService {
  // BehaviorSubjects przechowujące stan
  private _assets = new BehaviorSubject<Array<{ name: string; amount: number; usdValue: number, pnl: number }>>([]);
  readonly assets$ = this._assets.asObservable();

  readonly sortedAssets$ = this.assets$.pipe(
    map(list => (list || []).slice().sort((a, b) => {
      const aUsd = Number(a?.usdValue ?? 0);
      const bUsd = Number(b?.usdValue ?? 0);
      return bUsd - aUsd; // descending
    }))
  );

  private _walletWorthSnapshots = new BehaviorSubject<Array<{ timestamp: string; totalWorthUsd: number }>>([]);
  readonly walletWorthSnapshots$ = this._walletWorthSnapshots.asObservable();

  private _loading = new BehaviorSubject<boolean>(true);
  readonly loading$ = this._loading.asObservable();

  private _totalUsd = new BehaviorSubject<number>(0);
  readonly totalUsd$ = this._totalUsd.asObservable();

  private _percentChange = new BehaviorSubject<number>(0);
  readonly percentChange$ = this._percentChange.asObservable();

  private _roi = new BehaviorSubject<number>(0);
  readonly roi$ = this._roi.asObservable();

  // dodatkowo getters jeśli chcesz sync access
  get assetsSnapshot() { return this._assets.value; }
  get loadingSnapshot() { return this._loading.value; }

  private readonly initialUsd: number = 10000;
  private readonly candlesApi = '/api/v1/candles';

  constructor(private http: HttpClient, private auth: AuthService, public tradeService: TradeService) {
    this.loadWallet();
    this.loadWalletWorthSnapshots();
  }

  public async loadWallet() {
    this._loading.next(true);
    this._assets.next([]);
    this._totalUsd.next(0);

    try {
      const owned = await firstValueFrom(this.tradeService.getOwnedAssets());
      let total = 0;
      const list: Array<{ name: string; amount: number; usdValue: number, pnl: number }> = [];

      for (const a of (owned || []) as OwnedAsset[]) {
        const price = await this.getPriceForAsset(a.name);
        const usdValue = (a.amount || 0) * (price || 0);
        const pnl = usdValue - ((a.amount || 0) * (a.avgPrice || 0));
        list.push({ name: a.name, amount: a.amount, usdValue, pnl });
        total += usdValue;
      }

      this._assets.next(list);
      this._totalUsd.next(total);
      this._percentChange.next(this.initialUsd === 0 ? 0 : ((total - this.initialUsd) / this.initialUsd) * 100);
      this._roi.next(total / this.initialUsd - 1);
    } catch (err) {
      console.error('[WalletOverview] failed to load wallet', err);
      this._assets.next([]);
      this._totalUsd.next(0);
      this._percentChange.next(0);
      this._roi.next(0);
    } finally {
      this._loading.next(false);
    }
  }

  private async getPriceForAsset(name?: string): Promise<number> {
    if (!name) return 0;
    const nm = name.toUpperCase();
    if (nm === 'USDC') return 1;

    const symbol = `${nm}USDC`;
    try {
      const res: any = await firstValueFrom(this.http.get(`${this.candlesApi}/${symbol}/1h/refresh`));
      const closeVal = parseFloat(res?.close ?? '0');
      return isNaN(closeVal) ? 0 : closeVal;
    } catch (err) {
      console.warn('[WalletOverview] price fetch failed for', symbol, err);
      return 0;
    }
  }

  public async loadWalletWorthSnapshots() {
    try {
      const res: any = await firstValueFrom(this.http.get(`/api/v1/wallet/snapshots`, {
        headers: new HttpHeaders().set('Authorization', `Bearer ${this.auth.getToken()}`)
      }));
      const snapshots = (res || []) as Array<{ timestamp: string; totalWorthUsd: number }>;
      console.log('[WalletOverview] loaded wallet worth snapshots:', snapshots);
      this._walletWorthSnapshots.next(snapshots);
    } catch (err) {
      console.error('[WalletOverview] failed to load wallet worth snapshots', err);
      this._walletWorthSnapshots.next([]);
    }
  }
}