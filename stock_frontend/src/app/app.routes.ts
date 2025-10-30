import { Routes } from '@angular/router';
import { PublicDashboardComponent } from './features/public-dashboard/public-dashboard.component';
import { AuthComponent } from './core/auth/auth.component';
import { UserDashboardComponent } from './features/user-dashboard/user-dashboard.component';
import { AuthGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', component: PublicDashboardComponent },
  { path: 'login', component: AuthComponent }
  ,{ path: 'dashboard', component: UserDashboardComponent, canActivate: [AuthGuard] }
];