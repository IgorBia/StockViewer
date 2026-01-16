import { Component, Inject, ChangeDetectorRef, NgZone, Output, EventEmitter} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {WatchlistService} from './watchlist.service'
import { Watchlist } from './watchlist';
import { ChartService } from '../../shared/chart/chart.service';
import { Router } from '@angular/router';

@Component({
  selector: 'watchlist',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './watchlist.component.html',
  styleUrls: ['./watchlist.component.scss']
})
export class WatchlistComponent {
  watchlists: Watchlist[] = [];
  loading = false;
  error = '';

  constructor(
    private watchlistService: WatchlistService,
    public chartService: ChartService,
    private cd: ChangeDetectorRef,
    private ngZone: NgZone,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadWatchlists();
  }

  loadWatchlists(): void {
    this.loading = true;
    this.error = '';

    this.watchlistService.getWatchlists().subscribe({
      next: (res: any) => {
        let data: any[] = [];
        if (Array.isArray(res)) data = res;

        this.ngZone.run(() => {
          this.watchlists = data as Watchlist[];
          this.loading = false;
          this.error = '';
          this.cd.detectChanges();
          console.debug('[Watchlist] parsed watchlists:', this.watchlists);
        });
      },
      error: (err) => {
        console.error('[Watchlist] failed to load watchlists', err);
        this.ngZone.run(() => {
          this.loading = false;
          if (err && err.status === 401) {
            this.error = 'Not authenticated — please log in';
          } else if (err && err.status === 0) {
            this.error = 'Network error / server unreachable';
          } else {
            this.error = err?.error?.message || 'Failed to load watchlists';
          }
          this.cd.detectChanges();
        });
      }
    });
  }

  selectSymbol(sym: string) {
    this.chartService.setSymbol(sym);
  }
  
  add(){
    const symbol = this.chartService.getSymbol();
    if (!symbol) return;
    this.loading = true;
    this.watchlistService.addItemToWatchlist(symbol).subscribe({
      next: () => {
        this.loadWatchlists();
      },
      error: (err) => {
        console.error('[Watchlist] failed to add item', err);
        this.ngZone.run(() => {
          this.loading = false;
          this.error = err?.error?.message || 'Failed to add item to watchlist';
          this.cd.detectChanges();
        });
      }
    });
  }

  removeFromWatchlist(event: MouseEvent, watchlistId: string, symbol: string) {
    console.log('[Watchlist] removeFromWatchlist', watchlistId, symbol);
    if (!watchlistId || !symbol) return;

    // prevent default context menu and stop propagation
    event.preventDefault(); // prevent default context menu
    event.stopPropagation(); // stop propagation to avoid triggering other click events

    if (!watchlistId || !symbol) return;

    this.loading = true;
    this.watchlistService.removeItemFromWatchlist(watchlistId, symbol).subscribe({
      next: () => {
        this.loadWatchlists();
      },
      error: (err) => {
        console.error('[Watchlist] failed to remove item', err);
        this.ngZone.run(() => {
          this.loading = false;
          this.error = err?.error?.message || 'Failed to remove item from watchlist';
          this.cd.detectChanges();
        });
      }
    });
  }
}
