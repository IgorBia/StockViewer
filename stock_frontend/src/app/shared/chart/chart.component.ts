import {Component, Input} from '@angular/core';
import { ChartService } from './chart.service';

@Component({
  selector: 'app-chart',
  standalone: true,
  templateUrl: './chart.component.html'
})
export class ChartComponent{
  @Input() symbol!: string;

  constructor(private chartService: ChartService) {}

  ngOnInit() {
    if(!this.symbol) this.symbol = "BTCUSDC";
  }

  ngOnChanges() {
    this.loadChartData();
  }

  loadChartData() {
    this.chartService.getCandlestickData(this.symbol).subscribe(data => {
      console.log('Candlestick data:', data);
      // Process and display the data as needed
    });
  }
}
