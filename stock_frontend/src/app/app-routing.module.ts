import { Routes } from '@angular/router';
import { PublicDashboardComponent } from './features/public-dashboard/public-dashboard.component';
import { AuthComponent } from './core/auth/auth.component';

export const routes: Routes = [
  { path: '', component: PublicDashboardComponent },
  { path: 'login', component: AuthComponent }
];