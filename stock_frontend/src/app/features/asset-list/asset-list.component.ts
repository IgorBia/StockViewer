import { Component, EventEmitter, OnInit, OnDestroy, Output, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WalletService } from '../wallet-overview/wallet.service';
import { ChartService } from '../../shared/chart/chart.service';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { combineLatest } from 'rxjs';
import { map } from 'rxjs/operators';
import { OwnedAsset } from '../trade/trade.service';
import { TradeService } from '../trade/trade.service';

@Component({
    selector: 'asset-list',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './asset-list.component.html',
    styleUrls: ['./asset-list.component.scss']
})
export class AssetListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  public baseAsset?: Observable<OwnedAsset>;
  public baseAssetName?: string;
  public baseAssetAmount?: number;
  public quoteAsset?: Observable<OwnedAsset>;
  public quoteAssetName?: string;
  public quoteAssetAmount?: number;
  public filteredAssets$ = new Observable<Array<{ name: string; amount: number; usdValue: number, pnl: number }>>(); 

  constructor(private cd: ChangeDetectorRef, public walletService: WalletService, public chartService: ChartService, public tradeService: TradeService) {}
  
  ngOnInit(): void {

    this.filteredAssets$ = combineLatest([
      this.walletService.sortedAssets$,
      this.chartService.getBaseAsset(),
      this.chartService.getQuoteAsset()
    ]).pipe(
      map(([list, base, quote]) => {
        const B = (base ?? '').toUpperCase();
        const Q = (quote ?? '').toUpperCase();
        return (list || []).filter(a => {
          const name = (a.name ?? '').toUpperCase();
          return name !== B && name !== Q;
        });
      })
    );

    this.chartService.getBaseAsset()
      .pipe(takeUntil(this.destroy$))
      .subscribe(b => {
      this.baseAsset = this.tradeService.getOwnedAsset((b ?? '').toUpperCase());
      this.baseAsset?.pipe(takeUntil(this.destroy$)).subscribe(asset => {
        this.baseAssetName = asset?.name || '';
        this.baseAssetAmount = asset?.amount || 0;
        console.log('[Asset List] baseAssetAmount:', this.baseAssetAmount);
        this.cd.markForCheck();
      });
      console.log('[Asset List] baseAsset updated:', this.baseAsset);
      });

    this.chartService.getQuoteAsset()
      .pipe(takeUntil(this.destroy$))
      .subscribe(q => {
      this.quoteAsset = this.tradeService.getOwnedAsset((q ?? '').toUpperCase());
      this.quoteAsset?.pipe(takeUntil(this.destroy$)).subscribe(asset => {
        this.quoteAssetName = asset?.name || '';
        this.quoteAssetAmount = asset?.amount || 0;
        console.log('[Asset List] quoteAssetAmount:', this.quoteAssetAmount);
        this.cd.markForCheck();
      });
      console.log('[Asset List] quoteAsset updated:', this.quoteAsset);
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
