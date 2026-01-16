import { Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import { Chart, registerables } from 'chart.js';
import { WalletService } from '../wallet-overview/wallet.service';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';

Chart.register(...registerables);

@Component({
  selector: 'pie-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pie-chart.component.html',
  styleUrls: ['./pie-chart.component.scss']
})
export class PieChartComponent implements OnInit, OnDestroy {
  @ViewChild('pieCanvas', { static: false }) canvas!: ElementRef<HTMLCanvasElement>;
  private chart?: Chart;
  private sub?: Subscription;
  private viewInited = false;
  private pendingRender = false;

  labels: string[] = [];
  data: number[] = [];
  total = 0;

  constructor(private walletService: WalletService) {}

  ngOnInit(): void {
    console.log('PieChartComponent initialized');
    this.sub = this.walletService.assets$.subscribe(list => {
      const filtered = (list || []).filter(a => Number(a.usdValue) > 0);
      this.labels = filtered.map(a => a.name);
      this.data = filtered.map(a => Number(a.usdValue));
      this.total = this.data.reduce((sum, val) => sum + val, 0);
      if (this.viewInited && this.canvas) {
        this.renderOrUpdate();
      } else {
        this.pendingRender = true;
      }
    });
  }

  ngAfterViewInit(): void {
    console.log('PieChartComponent view initialized');
    this.viewInited = true;
    this.renderOrUpdate();
    this.pendingRender = false;
  }


  private renderOrUpdate() {
    console.log('Rendering or updating pie chart');
    if (!this.canvas || !this.canvas.nativeElement) return;

    const ctx = this.canvas.nativeElement.getContext('2d')!; 
    console.log('Rendering pie chart with labels:', this.labels, 'and data:', this.data);
    if (!this.chart) {
      this.chart = new Chart(ctx, {
        type: 'pie',
        data: {
          labels: this.labels,
          datasets: [{
            data: this.data,
            backgroundColor: [
              '#D4AF37', '#4F46E5', '#06B6D4', '#10B981', '#F97316', '#EF4444', '#60A5FA'
            ],
            borderColor: '#071129',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { position: 'right' },
            tooltip: {
              callbacks: {
                label: (tooltipItem) => {
                  const idx = tooltipItem.dataIndex ?? 0;
                  const value = Number(this.data[idx]) || 0;
                  const percent = this.total > 0 ? (value / this.total) * 100 : 0;
                  const label = this.labels[idx] ?? '';
                  return `${label}: ${value.toLocaleString()} USD (${percent.toFixed(2)}%)`;
                }
              }
            }
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