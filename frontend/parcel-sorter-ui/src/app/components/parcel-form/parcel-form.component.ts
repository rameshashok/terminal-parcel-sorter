import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { Parcel } from '../../models/models';
import { ParcelService } from '../../services/parcel.service';

@Component({
  selector: 'app-parcel-form',
  standalone: true,
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatCardModule],
  templateUrl: './parcel-form.component.html'
})
export class ParcelFormComponent {
  @Output() created = new EventEmitter<void>();

  parcel: Partial<Parcel> = { weightKg: 1 };

  constructor(private svc: ParcelService) {}

  submit() {
    this.svc.createParcel(this.parcel as Parcel).subscribe(() => {
      this.parcel = { weightKg: 1 };
      this.created.emit();
    });
  }
}
