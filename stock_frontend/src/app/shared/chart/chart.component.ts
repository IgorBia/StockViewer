
import { Component, AfterViewInit, ElementRef, ViewChild, Input, OnDestroy } from '@angular/core';
import { ChartService } from './chart.service';
import { interval, Subscription } from 'rxjs';
import * as lwc from 'lightweight-charts';
import {CandlestickSeriesOptions, createChart, DeepPartial, IChartApi, IRange, ISeriesApi, Time} from 'lightweight-charts';
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
  @ViewChild('macdContainer') macdContainer!: ElementRef; 
  
  private refreshInterval = 60*15;
  private chart!: IChartApi; 
  private macdChart!: IChartApi; 
  
  private candleSeries!: ISeriesApi<'Candlestick'>;
  private ema9Series?: ISeriesApi<'Line'>;
  
  private macdLineSeries?: ISeriesApi<'Line'>;
  private macdSignalSeries?: ISeriesApi<'Line'>;
  private macdHistogramSeries?: ISeriesApi<'Histogram'>;
  
  chartService: ChartService;
  private refreshSub?: Subscription;
  private timeoutId?: number;
  private syncingVisibleRange = false;
  private syncingInitialized = false;

  constructor(chartService: ChartService) {
    this.chartService = chartService;
  }

  private timezoneOffsetSeconds = -new Date().getTimezoneOffset() * 60; // e.g. CET => 3600

  private parseToSeconds(value: any): number {
    try {
      if (value == null) return Math.floor(Date.now() / 1000);
      if (typeof value === 'number') {
        // if large number assume milliseconds
        return value > 1e12 ? Math.floor(value / 1000) : Math.floor(value);
      }
      if (typeof value === 'string') {
        const ms = Date.parse(value);
        if (!isNaN(ms)) return Math.floor(ms / 1000);
        const msZ = Date.parse(value + 'Z');
        if (!isNaN(msZ)) return Math.floor(msZ / 1000);
        return Math.floor(Date.now() / 1000);
      }
      if (value instanceof Date) return Math.floor(value.getTime() / 1000);
      return Math.floor(Date.now() / 1000);
    } catch (e) {
      return Math.floor(Date.now() / 1000);
    }
  }

  ngAfterViewInit() {
    this.buildChart();
    this.buildMACDChart(); 
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

  ngOnChanges() {
    this.loadAndListen();
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


private setupLogicalSync() {
  if (this.syncingInitialized) return;
  if (!this.chart || !this.macdChart) return;

  const chartTS = (this.chart.timeScale() as any);
  const macdTS = (this.macdChart.timeScale() as any);

  chartTS.subscribeVisibleLogicalRangeChange((logicalRange: any) => {
    this.syncVisibleLogicalRange(this.chart, this.macdChart, logicalRange);
  });
  macdTS.subscribeVisibleLogicalRangeChange((logicalRange: any) => {
    this.syncVisibleLogicalRange(this.macdChart, this.chart, logicalRange);
  });

  this.syncingInitialized = true;
}

private syncVisibleLogicalRange(fromChart: IChartApi, toChart: IChartApi | undefined, logicalRange: { from: number; to: number } | null) {
  if (!toChart || !logicalRange) return;
  if (this.syncingVisibleRange) return;

  const current = (toChart.timeScale() as any).getVisibleLogicalRange?.();
  const eps = 0.001; 

  if (current && Math.abs(current.from - logicalRange.from) <= eps && Math.abs(current.to - logicalRange.to) <= eps) {
    return;
  }

  this.syncingVisibleRange = true;
  const offset = -2; 

  const rawFrom = Number(logicalRange.from);
  const rawTo = Number(logicalRange.to);
  if (!isFinite(rawFrom) || !isFinite(rawTo)) {
    console.warn('[chart] invalid logicalRange', logicalRange);
    return;
  }

  const safeFrom = Math.round(rawFrom) + offset;
  const safeTo = Math.round(rawTo) + offset;

  if (safeTo <= safeFrom) {
    console.warn('[chart] invalid shifted logical range', { safeFrom, safeTo, offset });
    return;
  }

  try {
    const ts = (toChart.timeScale() as any);
    ts.setVisibleLogicalRange({ from: safeFrom, to: safeTo });
  } catch (err) {
    console.error('[chart] setVisibleLogicalRange (shifted) failed', err, { safeFrom, safeTo });
  } finally {
      setTimeout(() => { this.syncingVisibleRange = false; }, 0);
  }
}

buildMACDChart() {
  if (!this.macdContainer) {
    console.error('[macdChart] container ref not found');
    return;
  }

  const el = this.macdContainer.nativeElement as HTMLElement;
  this.macdChart = createChart(el, {
    width: el.offsetWidth,
    height: el.offsetHeight,
    layout: { background: { color: '#000D1B' }, textColor: '#d1d4dc' },
    grid: { vertLines: { color: '#001226' }, horzLines: { color: '#001226' } },
    rightPriceScale: { visible: true, borderColor: '#001226' },
    timeScale: { visible: false, borderColor: '#001226', timeVisible: true, secondsVisible: false, rightOffset: -10, barSpacing: 10 },
  });
  this.macdChart.applyOptions({
  handleScroll: {
    mouseWheel: false,       // wyłącza przewijanie kółkiem myszy
    pressedMouseMove: false, // wyłącza panning (przeciąganie myszką)
    horzTouchDrag: false     // wyłącza przesuwanie na dotykowych urządzeniach (opcjonalnie)
  },
  handleScale: {
    axisPressedMouseMove: false, // wyłącza skalowanie/przeciąganie osi wciśniętym przyciskiem
    mouseWheel: false,           // wyłącza zoom kółkiem myszy
    pinch: false                 // wyłącza pinch-to-zoom
  }
});
}

  buildSeries() {
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
        let time = this.parseToSeconds(t);
        // adjust by local timezone offset so chart axis shows local time when library formats in UTC
        time = time + this.timezoneOffsetSeconds;
        return {
          time,
          open: Number((d as any).open),
          high: Number((d as any).high),
          low: Number((d as any).low),
          close: Number((d as any).close),
        };
      });

    this.candleSeries.setData(candlesticks as any);

    const ema9Data: Array<{time:number,value:number}> = [];
    const macdLineData: Array<{time:number,value:number}> = [];
    const macdSignalData: Array<{time:number,value:number}> = [];
    const macdHistogramData: Array<{time:number,value:number,color?:string}> = [];
    

    data.forEach(d => {
    const t = (d as any).timestamp ?? (d as any).openTime;
    let time = this.parseToSeconds(t);
    time = time + this.timezoneOffsetSeconds;

      let ema9Value: number | null = null;
      let macdValue: number | null = null;
      let signalValue: number | null = null;

      if (d.indicators && Array.isArray(d.indicators)) {
        d.indicators.forEach((indicator: any) => {
          if (indicator.name === 'EMA9' && indicator.value != null) ema9Value = Number(indicator.value);
          if (indicator.name === 'MACD' && indicator.value != null) macdValue = Number(indicator.value);
          if (indicator.name === 'MACD_SIGNAL' && indicator.value != null) signalValue = Number(indicator.value);
        });
      }

      if (ema9Value !== null) {
        ema9Data.push({ time, value: ema9Value });
      }
      if (macdValue !== null) {
        macdLineData.push({ time, value: macdValue });
      }
      if (signalValue !== null) {
        macdSignalData.push({ time, value: signalValue });
      }
      if (macdValue !== null && signalValue !== null) {
        const histValue = macdValue - signalValue;
        macdHistogramData.push({ time, value: histValue, color: histValue >= 0 ? '#26a69a' : '#ef5350' });
      }
    });

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

      this.setupLogicalSync();

      try {
        if (this.chart && this.chart.timeScale) {
          this.chart.timeScale().fitContent();
        }
        if (this.macdChart && (this.macdChart as any).timeScale) {
          (this.macdChart as any).timeScale().fitContent();
        }
      } catch (e) {
        console.warn('[chart] fitContent failed', e);
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
      layout: { 
        background: {
          color: '#000D1B' 
        },
        textColor: '#d1d4dc' 
      },
      grid: { 
        vertLines: { 
          color: '#001226' 
        }, 
        horzLines: { 
          color: '#001226'
        } 
      },
      rightPriceScale: { 
        visible: true, 
        borderColor: '#001226' 
      },
      timeScale: {
        visible: true, 
        borderColor: '#001226', 
        timeVisible: true, 
        secondsVisible: false, 
        rightOffset: 0, 
        barSpacing: 10 
      },
    });

    // Format time axis labels in the user's local timezone
    try {
      this.chart.applyOptions({
        timeScale: {
          // lightweight-charts will call this with unix seconds (number) or business day
          tickMarkFormatter: (time: any) => {
            try {
              // if time is an object like {year, month, day}
              if (typeof time === 'object' && time !== null && time.year) {
                const d = new Date(Date.UTC(time.year, (time.month ?? 1) - 1, time.day ?? 1));
                return d.toLocaleDateString();
              }
              const secs = typeof time === 'number' ? time : Number(time);
              if (!isFinite(secs)) return '';
              const d = new Date(secs * 1000);
              // show only time for intraday, date for daily
              const hours = d.getHours();
              const minutes = d.getMinutes();
              return d.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' }); // am pm format
            } catch (e) {
              return '';
            }
          }
        }
      });
    } catch (e) {
      // older lwc versions may not support tickMarkFormatter — ignore
      console.debug('[chart] tickMarkFormatter not applied', e);
    }
  }
}