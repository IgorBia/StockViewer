import { CommonModule } from '@angular/common';
import { Component, EventEmitter, OnInit, Output, ChangeDetectorRef } from '@angular/core';
import { PieChartComponent } from '../pie-chart/pie-chart.component';
import { WalletWorthChartComponent } from '../wallet-worth-chart/wallet-worth-chart.component';
import { TradeHistoryService } from './trade-history-service';

@Component({
	selector: 'app-trade-history',
	standalone: true,
	imports: [CommonModule, PieChartComponent, WalletWorthChartComponent],
	templateUrl: './trade-history.component.html',
	styleUrls: ['./trade-history.component.scss']
})
export class TradeHistoryComponent implements OnInit {
	@Output() closed = new EventEmitter<void>();


    constructor(private cd: ChangeDetectorRef, public tradeHistoryService: TradeHistoryService) {}

    ngOnInit(): void {
        
    }

    close() {
		this.closed.emit();
	}
}