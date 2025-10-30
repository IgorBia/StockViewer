import { Component, Inject, ChangeDetectorRef, NgZone} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {WatchlistService} from './watchlist.service'
import { Watchlist } from './watchlist';
import { AuthService } from '../../core/auth/auth.service';
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
    private cd: ChangeDetectorRef,
    private ngZone: NgZone
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
            // don't auto-redirect, just show message
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

}
