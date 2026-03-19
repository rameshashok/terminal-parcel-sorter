import { Routes } from '@angular/router';
import { SortingDashboardComponent } from './components/sorting-dashboard/sorting-dashboard.component';
import { ParcelListComponent } from './components/parcel-list/parcel-list.component';
import { ParcelFormComponent } from './components/parcel-form/parcel-form.component';
import { AiChatComponent } from './components/ai-chat/ai-chat.component';
import { RulesManagerComponent } from './components/rules-manager/rules-manager.component';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: SortingDashboardComponent },
  { path: 'parcels', component: ParcelListComponent },
  { path: 'register', component: ParcelFormComponent },
  { path: 'rules', component: RulesManagerComponent },
  { path: 'ai', component: AiChatComponent }
];
