import { Component, AfterViewInit, ElementRef, ViewChild, Input, OnDestroy } from '@angular/core';
import { ChartService } from './chart.service';
import { interval, Subscription } from 'rxjs';
import * as lwc from 'lightweight-charts';
import {CandlestickSeriesOptions, createChart, DeepPartial, IChartApi, ISeriesApi, PriceLineSource, SeriesDefinition, SeriesOptionsCommon, SeriesOptionsMap} from 'lightweight-charts';
import { Candlestick } from './candlestick';

type Time = UTCTimestamp | BusinessDay;
type UTCTimestamp = number;       // sekundy od 1970-01-01
type BusinessDay = { year: number; month: number; day: number };


@Component({
  selector: 'app-chart',
  standalone: true,
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.scss']
})
export class ChartComponent implements AfterViewInit, OnDestroy {
  @Input() symbol!: string;
  @ViewChild('chartContainer') chartContainer!: ElementRef;
  private refreshInterval = 60*15;
  private refreshIntervalName = '15m';
  private chart!: IChartApi;
  private candleSeries!: ISeriesApi<'Candlestick'>;
  private candleData: Candlestick[] = [];
  chartService: ChartService;
  private refreshSub?: Subscription;

  constructor(chartService: ChartService) {
    this.chartService = chartService;
  }

  ngOnInit() {
    if(!this.symbol) this.symbol = "BTCUSDC";
  }

  ngOnChanges() {
    this.loadChartData(this.refreshIntervalName);
  }

  ngAfterViewInit() {
    this.chart = createChart(this.chartContainer.nativeElement, {
      width: this.chartContainer.nativeElement.offsetWidth,
      height: this.chartContainer.nativeElement.offsetHeight,
      layout: { background: { color: '#000D1B' }, textColor: '#d1d4dc' },
      grid: { vertLines: { color: '#001226' }, horzLines: { color: '#001226' } },
      rightPriceScale: { borderColor: '#001226' },
      timeScale: { borderColor: '#001226', timeVisible: true, secondsVisible: false},
    });

    // assign to the class field and use the correct API
    // Support multiple lightweight-charts versions at runtime: prefer addCandlestickSeries, fallback to addSeries
    const seriesOptions: DeepPartial<CandlestickSeriesOptions> = {
      upColor: '#FFFFFF',
      downColor: '#B68308',
      borderUpColor: '#FFFFFF',
      borderDownColor: '#B68308',
      wickUpColor: '#FFFFFF',
      wickDownColor: '#B68308',
      borderVisible: true,
      wickVisible: true,
    };

    const anyChart = this.chart as any;
    let createdSeries: ISeriesApi<'Candlestick'> | undefined;

    // Prefer the dedicated API when available
    if (typeof anyChart.addCandlestickSeries === 'function') {
      createdSeries = anyChart.addCandlestickSeries(seriesOptions);
    } else if (typeof anyChart.addSeries === 'function') {
      // Fallback: try addSeries with a runtime SeriesDefinition from the lwc namespace
      const lwcObj = lwc as any;
      const runtimeDef = lwcObj.CandlestickSeries ?? lwcObj.candlestickSeries ?? lwcObj.Candlestick;
      if (runtimeDef) {
        createdSeries = anyChart.addSeries(runtimeDef, seriesOptions);
      } else {
        // Last resort: try passing options directly (may throw on some runtimes)
        try {
          createdSeries = anyChart.addSeries(seriesOptions);
        } catch (err) {
          console.error('[chart] failed to create candlestick series - no runtime SeriesDefinition and addSeries(options) failed', err);
          throw err;
        }
      }
    } else {
      throw new Error('Chart library does not expose addCandlestickSeries or addSeries');
    }

    this.candleSeries = createdSeries as any;

    const initialData: { time: number; open: number; high: number; low: number; close: number }[] = [
      { time: 1697904000 as unknown as number, open: 30000, high: 30500, low: 29500, close: 30200 },
      { time: 1697990400 as unknown as number, open: 30200, high: 30800, low: 30000, close: 30600 },
    ];

    this.candleSeries.setData(initialData as any);

    // Load real data immediately and start a refresh every round minute +3 seconds e.g. 15:00:03, 15:01:03 etc.
    const now = new Date();
    const seconds = now.getSeconds();
    const delay = ((60 - seconds) + 3) % 60;
    setTimeout(() => {
      this.loadChartData(this.refreshIntervalName);
      this.refreshSub = interval(this.refreshInterval * 1000).subscribe(() => this.loadChartData(this.refreshIntervalName));
    }, delay * 1000);

    this.loadChartData(this.refreshIntervalName);
    this.refreshSub = interval(this.refreshInterval * 1000).subscribe(() => this.loadChartData(this.refreshIntervalName));
  }

  ngOnDestroy(): void {
    if (this.refreshSub) {
      this.refreshSub.unsubscribe();
      this.refreshSub = undefined;
    }
    try {
      if (this.chart && typeof (this.chart as any).remove === 'function') {
        (this.chart as any).remove();
      }
    } catch (e) {
      console.warn('chart cleanup warning:', e);
    }
  }


  loadChartData(interval: string) {
    if (!this.candleSeries) {
      return;
    }

    this.chartService.getCandlestickData(this.symbol, interval).subscribe((data: Candlestick[]) => {
      if (!Array.isArray(data)) {
        return;
      }
      console.log('[chart] loaded candlestick data', data.length, 'points for symbol', this.symbol);
      console.log(data);
      const candlesticks = data.map(d => {
        const t = (d as any).timestamp ?? (d as any).openTime; 
        const time = Math.floor(new Date(t).getTime() / 1000); 
        return {
          time,
          open: Number((d as any).open),
          high: Number((d as any).high),
          low: Number((d as any).low),
          close: Number((d as any).close),
        };
      });

      this.candleSeries.setData(candlesticks as any);
    });
  }
}
