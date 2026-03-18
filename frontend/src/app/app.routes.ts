import { Routes } from '@angular/router';
import { DealerListComponent } from '../components/dealer-list/dealer-list';
import { DealerFormComponent } from '../components/dealer-form/dealer-form';

export const routes: Routes = [
  { path: '', component: DealerListComponent },
  { path: 'add', component: DealerFormComponent },
  { path: 'edit/:id', component: DealerFormComponent }
];