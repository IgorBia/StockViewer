import { Component, Input, OnInit, OnChanges, SimpleChanges, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ChartService } from '../../shared/chart/chart.service';
import { AuthService } from '../../core/auth/auth.service';
import { TradeService, OwnedAsset } from './trade.service';
import { WalletOverviewComponent } from '../wallet-overview/wallet-overview.component';
import { Observable, Subject, Subscription } from 'rxjs';
import { timeout, finalize, takeUntil, debounceTime } from 'rxjs/operators';
import { WalletService } from '../wallet-overview/wallet.service';
import { TradeHistoryComponent } from '../trade-history/trade-history.component';

@Component({
  selector: 'app-trade',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, WalletOverviewComponent, TradeHistoryComponent],
  templateUrl: './trade.component.html',
  styleUrls: ['./trade.component.scss']
})
export class TradeComponent implements OnInit, OnChanges {
  @Input() symbol?: string;
  form: FormGroup;
  submitting = false;
  message = '';
  baseAsset?: Observable<OwnedAsset>;
  baseAmountAvailable = 0;
  quoteAsset?: Observable<OwnedAsset>;
  quoteAmountAvailable = 0;
  private destroy$ = new Subject<void>();
  isTradeHistoryOpen = false;
  private updatingFromCode = false;

  private symbolSub?: Subscription;

  constructor(
    private fb: FormBuilder,
    private chartService: ChartService,
    private auth: AuthService,
    private tradeService: TradeService,
    private cd: ChangeDetectorRef,
    public walletService: WalletService
  ) {
    this.form = this.fb.group({
      side: ['buy'],
      amount: [0],
      percent: [0]
    });
  }

  ngOnInit(): void {

    // subskrybuj zmiany symbolu z ChartService — kompatybilne z dotychczasowym setSymbol()
    this.symbolSub = this.chartService.symbolChanges().subscribe(sym => {
      // aktualizujemy lokalne pole symbol (jeśli używasz Input też to uwzględni)
      this.symbol = sym;
      console.log('[Trade] ChartService symbol changed ->', sym);
    });
    this.chartService.getBaseAsset()
      .pipe(takeUntil(this.destroy$))
      .subscribe(b => {
      this.baseAsset = this.tradeService.getOwnedAsset((b ?? '').toUpperCase());
      this.baseAsset?.pipe(takeUntil(this.destroy$)).subscribe(asset => {
        this.baseAmountAvailable = asset?.amount || 0;
        console.log('[Trade] baseAmountAvailable:', this.baseAmountAvailable);
        this.cd.markForCheck();
      });
      console.log('[Trade] baseAsset updated:', this.baseAsset);
      });

    this.chartService.getQuoteAsset()
      .pipe(takeUntil(this.destroy$))
      .subscribe(q => {
      this.quoteAsset = this.tradeService.getOwnedAsset((q ?? '').toUpperCase());
      this.quoteAsset?.pipe(takeUntil(this.destroy$)).subscribe(asset => {
        this.quoteAmountAvailable = asset?.amount || 0;
        console.log('[Trade] quoteAmountAvailable:', this.quoteAmountAvailable);
        this.cd.markForCheck();
      });
      console.log('[Trade] quoteAsset updated:', this.quoteAsset);
      });


    this.form.get('side')!.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.recomputeFromPercent();
    });

    // when percent slider changes -> update amount
    this.form.get('percent')!.valueChanges.pipe(takeUntil(this.destroy$), debounceTime(50)).subscribe(p => {
      if (this.updatingFromCode) return;
      this.updatingFromCode = true;
      this.updateAmountFromPercent(Number(p));
      console.log('[Trade] percent changed to', p);
      this.updatingFromCode = false;
    });

    // when amount input changes -> update percent
    this.form.get('amount')!.valueChanges.pipe(takeUntil(this.destroy$), debounceTime(100)).subscribe(a => {
      if (this.updatingFromCode) return;
      this.updatingFromCode = true;
      this.updatePercentFromAmount(Number(a));
      console.log('[Trade] amount changed to', a);
      this.updatingFromCode = false;
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['symbol'] && !changes['symbol'].isFirstChange()) {
      console.log('[Trade] @Input() symbol changed ->', changes['symbol'].currentValue);
    }
  }

  ngOnDestroy(): void {
    if (this.symbolSub) this.symbolSub.unsubscribe();
  }

  private updateAmountFromPercent(percent: number) {
    const side = this.form.get('side')!.value.toUpperCase();
    let avail = 0;
    if ( side === "BUY") {
      console.log('[Trade] quoteAmountAvailable:', this.quoteAmountAvailable);
      avail = this.quoteAmountAvailable || 0;
    }
    if ( side === "SELL") {
      console.log('[Trade] baseAmountAvailable:', this.baseAmountAvailable);
      avail = this.baseAmountAvailable || 0;
    }
    let amountBase = avail * (percent / 100);
    console.log('[Trade] computed amount from percent:', amountBase);
    this.form.get('amount')!.setValue(Number(amountBase.toFixed(8)), {emitEvent: false});
  }

  private updatePercentFromAmount(amount: number) {
    const side = this.form.get('side')!.value;
    let avail = 0;
    if ( side === "BUY") {
      avail = this.quoteAmountAvailable || 0;
    }
    if ( side === "SELL") {
      avail = this.baseAmountAvailable || 0;
    }
    let percent = 0;
    if (avail <= 0) {
      percent = 0;
    } else {
      percent = (amount / avail) * 100;
    }
    // clamp 0..100
    percent = Math.max(0, Math.min(100, percent));
    console.log('[Trade] computed percent from amount:', percent);
    this.form.get('percent')!.setValue(Number(percent.toFixed(2)), {emitEvent: false});
  }

  private recomputeFromPercent() {
    const p = Number(this.form.get('percent')!.value || 0);
    this.updateAmountFromPercent(p);
  }

  get currentSymbol() {
    return this.symbol || this.chartService.getSymbol() || '';
  }

  submit(): void {
    if (this.form.invalid) return;
    const payload = {
      pairSymbol: this.currentSymbol,
      transactionType: this.form.value.side.toUpperCase(),
      amount: this.form.value.amount
    };

    this.submitting = true;
    this.message = '';
    this.tradeService.executeTrade(payload)
      .pipe(
        // fail fast if server doesn't respond within 15s
        timeout(15000),
        // always clear submitting flag when observable completes/errors
        finalize(() => {
          this.submitting = false;
          this.cd.detectChanges();
          this.walletService.loadWallet(); 
          this.chartService.refreshPairData();
        })
      )
      .subscribe({
        next: (res: any) => {
          this.message = res?.message || 'Order executed';
        },
        error: (err: any) => {
          console.error('[Trade] executeTrade error', err);
          if (err?.name === 'TimeoutError') {
            this.message = 'Request timed out';
          } else {
            this.message = err?.error?.message || 'Failed to execute trade';
          }
        }
      });
  }
}