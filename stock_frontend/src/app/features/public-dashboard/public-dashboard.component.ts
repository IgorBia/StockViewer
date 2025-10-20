import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import {ChartComponent} from '../../shared/chart/chart.component';
import {AuthComponent} from '../../core/auth/auth.component';

@Component({
  selector: 'app-public-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, HttpClientModule, RouterLink, ChartComponent, AuthComponent],
  templateUrl: './public-dashboard.component.html',
  styleUrls: ['./public-dashboard.component.scss']
})
export class PublicDashboardComponent {
  searchQuery = '';
  selectedSymbol = '';
  showAuth: boolean | undefined;

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
