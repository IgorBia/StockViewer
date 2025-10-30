import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import {ChartComponent} from '../../shared/chart/chart.component';
import {AuthComponent} from '../../core/auth/auth.component';
import { ChartService } from '../../shared/chart/chart.service';
import { Subscription } from 'rxjs/internal/Subscription';

@Component({
  selector: 'app-public-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, HttpClientModule, RouterLink, ChartComponent, AuthComponent],
  templateUrl: './public-dashboard.component.html',
  styleUrls: ['./public-dashboard.component.scss']
})
export class PublicDashboardComponent implements OnInit, OnDestroy {
  searchQuery = '';
  selectedSymbol = 'BTCUSDC';
  showAuth: boolean | undefined;
  @Input() showAuthControl = true; 

  private sub?: Subscription;

  constructor(private chartService: ChartService) {}

  ngOnInit(): void {
    this.sub = this.chartService.currentSymbol$.subscribe(sym => {
      if (sym) {
        this.selectedSymbol = sym;
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  onSearch() {
    console.log('Wyszukano:', this.searchQuery);
    this.selectedSymbol = this.searchQuery;
    // TODO: logika pobierania danych wykresu
  }

  onLoginClick() {
    console.log('Przejście do logowania');
    // TODO: routing do /login (po dodaniu AuthModule)
  }
}
