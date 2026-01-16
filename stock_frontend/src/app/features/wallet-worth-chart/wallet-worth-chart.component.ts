import { Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import { Chart, registerables } from 'chart.js';
import { WalletService } from '../wallet-overview/wallet.service';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';

Chart.register(...registerables);

@Component({
  selector: 'wallet-worth-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './wallet-worth-chart.component.html',
  styleUrls: ['./wallet-worth-chart.component.scss']
})
export class WalletWorthChartComponent implements OnInit, OnDestroy {
  @ViewChild('chartCanvas', { static: false }) canvas!: ElementRef<HTMLCanvasElement>;
  private chart?: Chart;
  private sub?: Subscription;
  private viewInited = false;
  private pendingRender = false;

  labels: string[] = [];
  data: number[] = [];

  constructor(private walletService: WalletService) {}

  ngOnInit(): void {
    console.log('WalletWorthChartComponent initialized');
    this.sub = this.walletService.walletWorthSnapshots$.subscribe(snapshots => {
      console.log('Received wallet worth snapshots:', snapshots);
      this.labels = snapshots.map(s => {
        const t = typeof s.timestamp === 'number' ? s.timestamp : Date.parse(s.timestamp);
        const d = new Date(t);
        return d.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit', hour12: false });
      });      
      this.data = snapshots.map(s => s.totalWorthUsd);
      console.log('Processed labels:', this.labels, 'and data:', this.data); // this data is full of undefined?
      if (this.viewInited && this.canvas) {
        this.renderOrUpdate();
      } else {
        this.pendingRender = true;
      }
    });
  }

  ngAfterViewInit(): void {
    console.log('WalletWorthChartComponent view initialized');
    this.viewInited = true;
    this.renderOrUpdate();
    this.pendingRender = false;
  }


  private renderOrUpdate() {
    console.log('Rendering or updating wallet worth chart');
    if (!this.canvas || !this.canvas.nativeElement) return;

    const ctx = this.canvas.nativeElement.getContext('2d')!; 
    console.log('Rendering wallet worth chart with labels:', this.labels, 'and data:', this.data);
    if (!this.chart) {
      this.chart = new Chart(ctx, {
        type: 'line',
        data: {
          labels: this.labels,
          datasets: [{ // bez leegendy
            label: '',
            data: this.data,
            fill: false,
            borderColor: 'white',
            tension: 0.0
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false}
          }
        }
      });
    } else {
      this.chart.data.labels = this.labels;
      (this.chart.data.datasets[0].data as number[]) = this.data;
      this.chart.update();
    }
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.chart?.destroy();
  }
}