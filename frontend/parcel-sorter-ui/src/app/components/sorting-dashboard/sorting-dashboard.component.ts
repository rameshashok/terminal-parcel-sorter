import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { Parcel, ParcelStatus } from '../../models/models';
import { ParcelService } from '../../services/parcel.service';

@Component({
  selector: 'app-sorting-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatGridListModule, MatIconModule],
  templateUrl: './sorting-dashboard.component.html'
})
export class SortingDashboardComponent implements OnInit {
  parcels: Parcel[] = [];
  belts: string[] = ['Belt-A', 'Belt-B', 'Belt-C', 'Belt-D', 'Belt-E', 'Belt-F'];

  constructor(private svc: ParcelService) {}

  ngOnInit() {
    this.svc.getParcels().subscribe(p => this.parcels = p);
  }

  beltParcels(belt: string): Parcel[] {
    return this.parcels.filter(p => p.assignedBelt === belt && p.status === 'SORTED');
  }

  countByStatus(status: ParcelStatus): number {
    return this.parcels.filter(p => p.status === status).length;
  }
}
