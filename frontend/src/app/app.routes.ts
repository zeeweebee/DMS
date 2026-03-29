import { Routes } from '@angular/router';
import { DealerListComponent }   from '../components/dealer-list/dealer-list';
import { DealerFormComponent }   from '../components/dealer-form/dealer-form';
import { LoginComponent }        from '../components/login/login';
import { HelloComponent }        from '../components/hello/hello';
import { ModelListComponent }    from '../components/model-list/model-list';
import { ModelFormComponent }    from '../components/model-form/model-form';
import { StockListComponent }    from '../components/stock-list/stock-list';
import { StockFormComponent }    from '../components/stock-form/stock-form';
import { EnquiryListComponent }  from '../components/enquiry-list/enquiry-list';
import { EnquiryFormComponent }  from '../components/enquiry-form/enquiry-form';
import { BookingListComponent }  from '../components/booking-list/booking-list';
import { BookingFormComponent }  from '../components/booking-form/booking-form';
import { SaleListComponent }     from '../components/sale-list/sale-list';
import { SaleFormComponent }     from '../components/sale-form/sale-form';
import { AuthGuard }             from './auth.guard';
import { RoleGuard }             from './role.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', redirectTo: '/hello', pathMatch: 'full' },

  // Dealers — ADMIN only
  { path: 'dealers',          component: DealerListComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN'] } },
  { path: 'dealers/add',      component: DealerFormComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN'] } },
  { path: 'dealers/edit/:id', component: DealerFormComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN'] } },

  // Models — all authenticated
  { path: 'models',           component: ModelListComponent, canActivate: [AuthGuard] },
  { path: 'models/add',       component: ModelFormComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN'] } },
  { path: 'models/edit/:id',  component: ModelFormComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN'] } },

  // Stock — ADMIN + DEALER
  { path: 'stock',            component: StockListComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN', 'DEALER'] } },
  { path: 'stock/add',        component: StockFormComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN', 'DEALER'] } },

  // Enquiries — ADMIN + DEALER
  { path: 'enquiries',        component: EnquiryListComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN', 'DEALER'] } },
  { path: 'enquiries/add',    component: EnquiryFormComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN', 'DEALER'] } },

  // Bookings — ADMIN + DEALER
  { path: 'bookings',         component: BookingListComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN', 'DEALER'] } },
  { path: 'bookings/add',     component: BookingFormComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN', 'DEALER'] } },

  // Sales — ADMIN + DEALER
  { path: 'sales',            component: SaleListComponent,    canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN', 'DEALER'] } },
  { path: 'sales/new',        component: SaleFormComponent,    canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN', 'DEALER'] } },

  // Dashboard
  { path: 'hello', component: HelloComponent, canActivate: [AuthGuard] },

  { path: '**', redirectTo: '/login' }
];
