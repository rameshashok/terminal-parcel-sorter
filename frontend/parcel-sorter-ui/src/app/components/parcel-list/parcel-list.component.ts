import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatExpansionModule } from '@angular/material/expansion';
import { Parcel } from '../../models/models';
import { ParcelService } from '../../services/parcel.service';

@Component({
  selector: 'app-parcel-list',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule, MatChipsModule, MatIconModule, MatSnackBarModule, MatTooltipModule, MatExpansionModule],
  templateUrl: './parcel-list.component.html',
  styles: [`
    .exception-panel {
      background: #fff3e0; border-left: 3px solid #e65100;
      border-radius: 4px; padding: 8px 12px; margin-top: 4px;
      font-size: 12px; color: #333; line-height: 1.5;
    }
    .exception-panel mat-icon { color: #e65100; font-size: 16px; vertical-align: middle; }
  `]
})
export class ParcelListComponent implements OnInit {
  parcels: Parcel[] = [];
  expandedParcelId: number | null = null;
  displayedColumns = ['trackingNumber', 'origin', 'destination', 'postalCode', 'weightKg', 'status', 'assignedBelt', 'actions'];

  constructor(private svc: ParcelService, private snack: MatSnackBar) {}

  ngOnInit() { this.load(); }

  load() {
    this.svc.getParcels().subscribe(p => this.parcels = p);
  }

  sort(parcel: Parcel) {
    this.svc.sortParcel(parcel.id!).subscribe({
      next: () => { this.snack.open('Parcel sorted!', 'OK', { duration: 2000 }); this.load(); },
      error: () => this.snack.open('Sort failed', 'OK', { duration: 2000 })
    });
  }

  dispatch(parcel: Parcel) {
    this.svc.updateStatus(parcel.id!, 'DISPATCHED').subscribe(() => this.load());
  }

  delete(parcel: Parcel) {
    this.svc.deleteParcel(parcel.id!).subscribe(() => this.load());
  }

  toggleReasoning(parcel: Parcel) {
    this.expandedParcelId = this.expandedParcelId === parcel.id ? null : (parcel.id ?? null);
  }

  statusColor(status?: string): string {
    const map: Record<string, string> = {
      RECEIVED: 'primary', SORTING: 'accent', SORTED: 'warn',
      DISPATCHED: '', EXCEPTION: 'warn'
    };
    return map[status ?? ''] ?? '';
  }
}
