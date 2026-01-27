import { Component, EventEmitter, OnInit, Output, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TradeService, OwnedAsset } from '../trade/trade.service';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { WalletService } from './wallet.service';
import { PieChartComponent } from '../pie-chart/pie-chart.component';
import { WalletWorthChartComponent } from '../wallet-worth-chart/wallet-worth-chart.component';

@Component({
	selector: 'app-wallet-overview',
	standalone: true,
	imports: [CommonModule, FormsModule, PieChartComponent, WalletWorthChartComponent],
	templateUrl: './wallet-overview.component.html',
	styleUrls: ['./wallet-overview.component.scss']
})
export class WalletOverviewComponent implements OnInit {
	@Output() closed = new EventEmitter<void>();
	selectedRisk = 1;
	loadingManaged = false;
	riskOptions = [
	{ value: 0, label: 'Wyłączony' },
	{ value: 1, label: 'Konserwatywny' },
	{ value: 2, label: 'Umiarkowany' },
	{ value: 3, label: 'Agresywny' },
	{ value: 99, label: 'Eksperymentalny' }
	];

  constructor(private cd: ChangeDetectorRef, public walletService: WalletService) {}

  	ngOnInit(): void {
  	}

  	ngOnDestroy(): void {
  	}

	closeModal() {
		this.closed.emit();
	}

	close() {
		this.closed.emit();
	}

	enableManaged() {
	this.loadingManaged = true;
	this.walletService.tradeService.enableManagedAsset(this.selectedRisk).subscribe({
		next: () => {
		this.loadingManaged = false;
		this.walletService.loadWallet();
		console.log('Managed asset set to risk', this.selectedRisk);
		},
		error: (err) => {
		this.loadingManaged = false;
		console.error('Failed to set managed asset', err);
		}
	});
	}

	closePosition(name: string, amount: number) {
		this.walletService.tradeService.executeTrade({
			pairSymbol: name+"USDC",
			transactionType: 'SELL',
			amount: amount
		})
		.subscribe({
			next: (response) => {
				console.log('Position closed successfully:', response);
				this.walletService.loadWallet();
			},
			error: (err) => console.error('Failed to close position:', err)
		});
	}
}
