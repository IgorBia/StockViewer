import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PublicDashboardComponent } from '../public-dashboard/public-dashboard.component';
import { WatchlistComponent } from '../watchlist/watchlist.component'
import { TradeComponent } from '../trade/trade.component';
import { AuthService } from '../../core/auth/auth.service';
import { TopBarComponent } from '../topbar/topbar.component';
import { AssetListComponent } from '../asset-list/asset-list.component';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule, PublicDashboardComponent, WatchlistComponent, TradeComponent, TopBarComponent, AssetListComponent],
  templateUrl: './user-dashboard.component.html',
  styleUrls: ['./user-dashboard.component.scss']
})
export class UserDashboardComponent implements OnInit {
  error = '';

  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/']);
  }
}
