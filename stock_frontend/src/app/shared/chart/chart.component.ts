
import { Component, AfterViewInit, ElementRef, ViewChild, Input, OnDestroy } from '@angular/core';
import { ChartService } from './chart.service';
import { interval, Subscription } from 'rxjs';
import * as lwc from 'lightweight-charts';
import {CandlestickSeriesOptions, createChart, DeepPartial, IChartApi, ISeriesApi, IRange, Time} from 'lightweight-charts';
import { Candlestick } from './candlestick';

@Component({
  selector: 'app-chart',
  standalone: true,
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.scss']
})
export class ChartComponent implements AfterViewInit, OnDestroy {
  @Input() symbol!: string;
  @ViewChild('chartContainer') chartContainer!: ElementRef;
  @ViewChild('macdContainer') macdContainer!: ElementRef; // Nowy kontener dla MACD
  
  private refreshInterval = 60*15;
  private chart!: IChartApi; // Główny wykres (świece + EMA9)
  private macdChart!: IChartApi; // Osobny wykres dla MACD
  
  private candleSeries!: ISeriesApi<'Candlestick'>;
  private ema9Series?: ISeriesApi<'Line'>;
  
  // MACD na osobnym wykresie
  private macdLineSeries?: ISeriesApi<'Line'>;
  private macdSignalSeries?: ISeriesApi<'Line'>;
  private macdHistogramSeries?: ISeriesApi<'Histogram'>;
  
  chartService: ChartService;
  private refreshSub?: Subscription;
  private timeoutId?: number;

  constructor(chartService: ChartService) {
    this.chartService = chartService;
  }

  // ...existing code...

  ngAfterViewInit() {
    this.buildChart();
    this.buildMACDChart(); // Nowy wykres dla MACD
    this.buildSeries();
    this.loadAndListen();
  }

  ngOnDestroy(): void {
    if (this.refreshSub) {
      this.refreshSub.unsubscribe();
    }
    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
    }
    try {
      if (this.chart) this.chart.remove();
      if (this.macdChart) this.macdChart.remove();
    } catch (e) {
      console.error('[chart] error removing chart', e);
    }
  }

  changeInterval(newInterval: string) {
    this.chartService.setInterval(newInterval);

    if(newInterval[newInterval.length -1] === 'm') {
      this.refreshInterval = 60 * (newInterval.slice(0, newInterval.length -1) as unknown as number); 
    } else if(newInterval[newInterval.length -1] === 'h') {
      this.refreshInterval = 60 * 60 * (newInterval.slice(0, newInterval.length -1) as unknown as number); 
    } else if(newInterval[newInterval.length -1] === 'd') {
      this.refreshInterval = 60 * 60 * 24 * (newInterval.slice(0, newInterval.length -1) as unknown as number); 
    }

    this.loadAndListen();
  }

   buildMACDChart() {
    if (!this.macdContainer) {
      console.error('[macdChart] container ref not found');
      return;
    }

    const el = this.macdContainer.nativeElement as HTMLElement;
    const lwcObj = lwc as any;
    const createChartFn = lwcObj.createChart ?? lwcObj.default?.createChart;

    if (!createChartFn || typeof createChartFn !== 'function') {
      throw new Error('lightweight-charts does not expose createChart');
    }

    this.macdChart = createChartFn(el, {
      width: this.macdContainer.nativeElement.offsetWidth,
      height: this.macdContainer.nativeElement.offsetHeight,
      layout: { background: { color: '#000D1B' }, textColor: '#d1d4dc' },
      grid: { vertLines: { color: '#001226' }, horzLines: { color: '#001226' } },
      rightPriceScale: { borderColor: '#001226' },
      timeScale: { borderColor: '#001226', timeVisible: true, secondsVisible: false},
    });

    if (this.chart && this.chart.timeScale) {
      this.chart.timeScale().subscribeVisibleTimeRangeChange((timeRange: any) => {
        if (!timeRange) return;
        if (!this.macdChart || !this.macdChart.timeScale) return;
        try {
          this.macdChart.timeScale().setVisibleRange(timeRange as any);
        } catch (e) {
          console.warn('[chart sync] failed to set macd visible range', e);
        }
      });
    }

    if (this.macdChart && this.macdChart.timeScale) {
      this.macdChart.timeScale().subscribeVisibleTimeRangeChange((timeRange: any) => {
        if (!timeRange) return;
        if (!this.chart || !this.chart.timeScale) return;
        try {
          this.chart.timeScale().setVisibleRange(timeRange as any);
        } catch (e) {
          console.warn('[chart sync] failed to set main chart visible range', e);
        }
      });
    }
  }

  buildSeries() {
    // Główny wykres: Candlesticks + EMA9
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

    if (typeof anyChart.addCandlestickSeries === 'function') {
      createdSeries = anyChart.addCandlestickSeries(seriesOptions);
    } else if (typeof anyChart.addSeries === 'function') {
      const lwcObj = lwc as any;
      const runtimeDef = lwcObj.CandlestickSeries ?? lwcObj.candlestickSeries ?? lwcObj.Candlestick;
      if (runtimeDef) {
        createdSeries = anyChart.addSeries(runtimeDef, seriesOptions);
      } else {
        throw new Error('Cannot create candlestick series');
      }
    }

    this.candleSeries = createdSeries as any;

    // EMA9 na głównym wykresie
    const lwcObj = lwc as any;
    const LineSeriesDef = lwcObj.LineSeries ?? lwcObj.lineSeries ?? lwcObj.Line;
    
    if (LineSeriesDef && typeof anyChart.addSeries === 'function') {
      this.ema9Series = anyChart.addSeries(LineSeriesDef, {
        color: '#2962FF',
        lineWidth: 2,
        title: 'EMA9',
        priceScaleId: 'right'
      });
    }

    // MACD na osobnym wykresie
    const anyMacdChart = this.macdChart as any;
    const HistogramSeriesDef = lwcObj.HistogramSeries ?? lwcObj.histogramSeries ?? lwcObj.Histogram;
    
    if (HistogramSeriesDef && typeof anyMacdChart.addSeries === 'function') {
      // Histogram (rysuj PIERWSZY, żeby był w tle)
      this.macdHistogramSeries = anyMacdChart.addSeries(HistogramSeriesDef, {
        color: '#26a69a',
        priceFormat: {
          type: 'price',
          precision: 4,
          minMove: 0.0001,
        },
        priceScaleId: 'right'
      });
    }

    if (LineSeriesDef && typeof anyMacdChart.addSeries === 'function') {
      // MACD line
      this.macdLineSeries = anyMacdChart.addSeries(LineSeriesDef, {
        color: '#2196F3',
        lineWidth: 2,
        title: 'MACD',
        priceScaleId: 'right'
      });

      // Signal line
      this.macdSignalSeries = anyMacdChart.addSeries(LineSeriesDef, {
        color: '#FF6D00',
        lineWidth: 2,
        title: 'Signal',
        priceScaleId: 'right'
      });
    }

    const initialData: { time: number; open: number; high: number; low: number; close: number }[] = [
      { time: 1697904000 as unknown as number, open: 30000, high: 30500, low: 29500, close: 30200 },
      { time: 1697990400 as unknown as number, open: 30200, high: 30800, low: 30000, close: 30600 },
    ];

    this.candleSeries.setData(initialData as any);
  }

  loadChartData() {
    if (!this.candleSeries) {
      return;
    }

    this.chartService.getCandlestickData().subscribe((data: Candlestick[]) => {
      if (!Array.isArray(data)) {
        return;
      }
      console.log('[chart] loaded candlestick data', data.length, 'points');

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

      const ema9Data: Array<{time: number, value: number}> = [];
      const macdLineData: Array<{time: number, value: number}> = [];
      const macdSignalData: Array<{time: number, value: number}> = [];
      const macdHistogramData: Array<{time: number, value: number, color?: string}> = [];

      data.forEach(d => {
        const t = (d as any).timestamp ?? (d as any).openTime;
        const time = Math.floor(new Date(t).getTime() / 1000);
        
        if (d.indicators && Array.isArray(d.indicators)) {
          let macdValue: number | null = null;
          let signalValue: number | null = null;

          d.indicators.forEach(indicator => {
            if (indicator.name === 'EMA9' && indicator.value != null) {
              ema9Data.push({ time, value: Number(indicator.value) });
            }
            if (indicator.name === 'MACD' && indicator.value != null) {
              macdValue = Number(indicator.value);
              macdLineData.push({ time, value: macdValue });
            }
            if (indicator.name === 'MACD_SIGNAL' && indicator.value != null) {
              signalValue = Number(indicator.value);
              macdSignalData.push({ time, value: signalValue });
            }
          });

          // Histogram = MACD - Signal
          if (macdValue !== null && signalValue !== null) {
            const histValue = macdValue - signalValue;
            macdHistogramData.push({
              time,
              value: histValue,
              color: histValue >= 0 ? '#26a69a' : '#ef5350'
            });
          }
        }
      });

      // Aktualizuj serie
      if (this.ema9Series && ema9Data.length > 0) {
        this.ema9Series.setData(ema9Data as any);
      }
      if (this.macdLineSeries && macdLineData.length > 0) {
        this.macdLineSeries.setData(macdLineData as any);
      }
      if (this.macdSignalSeries && macdSignalData.length > 0) {
        this.macdSignalSeries.setData(macdSignalData as any);
      }
      if (this.macdHistogramSeries && macdHistogramData.length > 0) {
        this.macdHistogramSeries.setData(macdHistogramData as any);
      }
    });
  }

  loadAndListen() {

    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
    }
    if (this.refreshSub) {
      this.refreshSub.unsubscribe();
    }

    const now = new Date();
    const seconds = now.getSeconds();
    const delay = ((60 - seconds) + 3) % 60;

    this.timeoutId = window.setTimeout(() => {
      this.loadChartData();
      this.refreshSub = interval(this.refreshInterval * 1000).subscribe(() => this.loadChartData());
    }, delay * 1000);

    this.loadChartData();
    this.refreshSub = interval(this.refreshInterval * 1000).subscribe(() => this.loadChartData());
  }

  buildChart() {
    const lwcObj = lwc as any;
    const createChartFn = lwcObj.createChart ?? lwcObj.default?.createChart;
    this.chart = createChart(this.chartContainer.nativeElement, {
      width: this.chartContainer.nativeElement.offsetWidth,
      height: this.chartContainer.nativeElement.offsetHeight,
      layout: { background: { color: '#000D1B' }, textColor: '#d1d4dc' },
      grid: { vertLines: { color: '#001226' }, horzLines: { color: '#001226' } },
      rightPriceScale: { borderColor: '#001226' },
      timeScale: { borderColor: '#001226', timeVisible: true, secondsVisible: false},
    });
  }
}