import { Component, OnInit, OnChanges, SimpleChanges, } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/auth/auth.service';
import { Router } from '@angular/router';
import { WalletOverviewComponent } from '../wallet-overview/wallet-overview.component';
import { WalletService } from '../wallet-overview/wallet.service';

@Component({
  selector: 'top-bar',
  standalone: true,
  imports: [CommonModule, WalletOverviewComponent],
  templateUrl: './topbar.component.html',
  styleUrls: ['./topbar.component.scss']
})
export class TopBarComponent implements OnInit, OnChanges {

  public userEmail: string | null = localStorage.getItem('userEmail');
  isWalletOpen = false;
  walletBalance: number = 0;

  constructor(
    private auth: AuthService,
    private router: Router,
    public walletService: WalletService
  ) {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
  }

  ngOnDestroy(): void {
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/']);
  }
}
