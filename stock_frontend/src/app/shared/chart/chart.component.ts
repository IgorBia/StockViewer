import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-chart',
  standalone: true,
  templateUrl: './chart.component.html'
})
export class ChartComponent{
  @Input() symbol!: string;

  ngOnInit() {
    if(!this.symbol) this.symbol = "BTCUSDC";
  }
}
