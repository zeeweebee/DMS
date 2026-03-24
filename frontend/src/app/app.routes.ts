import { Routes } from '@angular/router';
import { DealerListComponent } from '../components/dealer-list/dealer-list';
import { DealerFormComponent } from '../components/dealer-form/dealer-form';
import { LoginComponent } from '../components/login/login';
import { HelloComponent } from '../components/hello/hello';
import { ModelListComponent } from '../components/model-list/model-list';
import { ModelFormComponent } from '../components/model-form/model-form';
import { StockListComponent } from '../components/stock-list/stock-list';
import { StockFormComponent } from '../components/stock-form/stock-form';
import { AuthGuard } from './auth.guard';
import { RoleGuard } from './role.guard';
import { StockGuard } from './stock.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', redirectTo: '/hello', pathMatch: 'full' },

  // Admin-only dealer routes
  { path: 'dealers', component: DealerListComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN'] } },
  { path: 'dealers/add', component: DealerFormComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN'] } },
  { path: 'dealers/edit/:id', component: DealerFormComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN'] } },

  // Model catalog — all authenticated
  { path: 'models', component: ModelListComponent, canActivate: [AuthGuard] },
  { path: 'models/add', component: ModelFormComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN'] } },
  { path: 'models/edit/:id', component: ModelFormComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN'] } },

  // Stock — ADMIN, DEALER, EMPLOYEE
  { path: 'stock', component: StockListComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN', 'DEALER', 'EMPLOYEE'] } },
  { path: 'stock/add', component: StockFormComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN', 'DEALER'] } },

  // Dashboard landing for authenticated users
  { path: 'hello', component: HelloComponent, canActivate: [AuthGuard] },

  { path: '**', redirectTo: '/login' }
];