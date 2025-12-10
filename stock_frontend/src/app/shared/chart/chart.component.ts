
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
  private syncingVisibleRange = false;
  private syncingInitialized = false;


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


// metoda do zakładania subskrypcji (wywołać tylko raz, po wczytaniu danych)
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

private almostEqual(a?: number | Time, b?: number | Time, eps = 1e-6): boolean {
  if (a == null || b == null) return false;
  const na = typeof a === 'number' ? a : (a as any).timestamp ?? NaN;
  const nb = typeof b === 'number' ? b : (b as any).timestamp ?? NaN;
  return Math.abs(na - nb) <= eps;
}

private almostEqualNumber(a?: number, b?: number, eps = 1e-6): boolean {
  if (a == null || b == null) return false;
  return Math.abs(a - b) <= eps;
}

private syncVisibleRange(fromChart: IChartApi, toChart: IChartApi | undefined, timeRange: IRange<Time> | null) {
  if (!toChart || !timeRange) return;
  if (this.syncingVisibleRange) return;

  try {
    const current = toChart.timeScale()?.getVisibleRange?.() as IRange<Time> | null;
    if (current && this.almostEqual(current.from as any, timeRange.from as any) && this.almostEqual(current.to as any, timeRange.to as any)) {
      return;
    }

    this.syncingVisibleRange = true;
    try {
      toChart.timeScale().setVisibleRange(timeRange as any);
    } finally {
      // zwolnij lock asynchronicznie, żeby callback po setVisibleRange mógł wykryć lock i nie echo-ować
      setTimeout(() => { this.syncingVisibleRange = false; }, 0);
    }
  } catch (e) {
    console.warn('[chart sync] setVisibleRange failed', e);
    this.syncingVisibleRange = false;
  }
}

private syncVisibleLogicalRange(fromChart: IChartApi, toChart: IChartApi | undefined, logicalRange: { from: number; to: number } | null) {
  if (!toChart || !logicalRange) return;
  if (this.syncingVisibleRange) return;

  const current = (toChart.timeScale() as any).getVisibleLogicalRange?.();
  const eps = 0.001; // tolerancja drobnych różnic

  if (current && Math.abs(current.from - logicalRange.from) <= eps && Math.abs(current.to - logicalRange.to) <= eps) {
    return;
  }

  this.syncingVisibleRange = true;
  try {
    (toChart.timeScale() as any).setVisibleLogicalRange({ from: logicalRange.from, to: logicalRange.to });
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
  // Utwórz MACD z IDENTYCZNYMI ustawieniami timeScale / rightPriceScale
  this.macdChart = createChart(el, {
    width: el.offsetWidth,
    height: el.offsetHeight,
    layout: { background: { color: '#000D1B' }, textColor: '#d1d4dc' },
    grid: { vertLines: { color: '#001226' }, horzLines: { color: '#001226' } },
    rightPriceScale: { visible: true, borderColor: '#001226' },
    timeScale: { borderColor: '#001226', timeVisible: true, secondsVisible: false, rightOffset: 0, barSpacing: 10 },
  });

  // upewnij się, że główny wykres ma te same opcje timeScale
  try {
    (this.chart as any)?.applyOptions?.({
      timeScale: { rightOffset: 0, barSpacing: 10 }
    });
  } catch (e) {
    // ignore
  }

  // subskrybuj synchronizację LOGICAL RANGE dopiero gdy oba wykresy są gotowe
  // if (this.chart && this.macdChart) {
  //   (this.chart.timeScale() as any).subscribeVisibleLogicalRangeChange((logicalRange: any) => {
  //     this.syncVisibleLogicalRange(this.chart, this.macdChart, logicalRange);
  //   });
  //   (this.macdChart.timeScale() as any).subscribeVisibleLogicalRangeChange((logicalRange: any) => {
  //     this.syncVisibleLogicalRange(this.macdChart, this.chart, logicalRange);
  //   });
  // }
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

  const ema9Data: Array<{time:number,value:number}> = [];
  const macdLineData: Array<{time:number,value:number}> = [];
  const macdSignalData: Array<{time:number,value:number}> = [];
  const macdHistogramData: Array<{time:number,value:number,color?:string}> = [];

  data.forEach(d => {
    const t = (d as any).timestamp ?? (d as any).openTime;
    const time = Math.floor(new Date(t).getTime() / 1000);

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
      layout: { background: { color: '#000D1B' }, textColor: '#d1d4dc' },
      grid: { vertLines: { color: '#001226' }, horzLines: { color: '#001226' } },
      // jawnie ustaw prawą skalę i timeScale, tak aby macd można było wyrównać po prawej
      rightPriceScale: { visible: true, borderColor: '#001226' },
      timeScale: { borderColor: '#001226', timeVisible: true, secondsVisible: false, rightOffset: 0, barSpacing: 10 },
    });
  }
}