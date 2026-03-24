import { Routes } from '@angular/router';
import { DealerListComponent } from '../components/dealer-list/dealer-list';
import { DealerFormComponent } from '../components/dealer-form/dealer-form';
import { LoginComponent } from '../components/login/login';
import { HelloComponent } from '../components/hello/hello';
import { AuthGuard } from './auth.guard';
import { RoleGuard } from './role.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', component: DealerListComponent, canActivate: [AuthGuard, RoleGuard] },
  { path: 'add', component: DealerFormComponent, canActivate: [AuthGuard, RoleGuard] },
  { path: 'edit/:id', component: DealerFormComponent, canActivate: [AuthGuard, RoleGuard] },
  { path: 'hello', component: HelloComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: '/login' }
];