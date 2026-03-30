import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgModel } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Parcel } from '../../models/models';
import { ParcelService } from '../../services/parcel.service';

@Component({
  selector: 'app-parcel-form',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatFormFieldModule, MatInputModule, MatButtonModule, MatCardModule,
    MatIconModule, MatProgressSpinnerModule
  ],
  templateUrl: './parcel-form.component.html',
  styles: [`
    .form-grid { display: flex; flex-direction: column; gap: 8px; }
    .full-width { width: 100%; }
    .ai-assist-panel {
      display: flex; align-items: flex-start; gap: 10px;
      background: #e8f4fd; border-left: 3px solid #1976d2;
      border-radius: 4px; padding: 10px 14px; margin-top: 4px;
      font-size: 13px; color: #1a1a1a; line-height: 1.5;
    }
    .ai-assist-panel mat-icon { color: #1976d2; font-size: 18px; height: 18px; width: 18px; flex-shrink: 0; margin-top: 1px; }
    .assist-label { font-size: 11px; font-weight: 600; color: #1976d2; display: block; margin-bottom: 2px; }
  `]
})
export class ParcelFormComponent {
  @Output() created = new EventEmitter<void>();

  parcel: Partial<Parcel> = { weightKg: 1 };

  assistGuidance = '';
  assistLoading = false;
  assistField = '';

  constructor(private svc: ParcelService) {}

  onFieldBlur(fieldName: string, control: NgModel) {
    if (control.invalid && control.dirty) {
      const errorCode = Object.keys(control.errors || {})[0] || 'invalid';
      this.fetchGuidance(fieldName, errorCode, String(control.value ?? ''));
    }
  }

  private fetchGuidance(field: string, errorCode: string, value: string) {
    this.assistField = field;
    this.assistLoading = true;
    this.assistGuidance = '';

    const formContext = `trackingNumber="${this.parcel.trackingNumber || ''}", ` +
      `origin="${this.parcel.origin || ''}", destination="${this.parcel.destination || ''}", ` +
      `postalCode="${this.parcel.postalCode || ''}", weightKg="${this.parcel.weightKg || ''}"`;

    this.svc.getAssistance({ field, errorCode, value, formContext }).subscribe({
      next: r => { this.assistGuidance = r.guidance; this.assistLoading = false; },
      error: () => { this.assistGuidance = this.fallback(field, errorCode); this.assistLoading = false; }
    });
  }

  private fallback(field: string, errorCode: string): string {
    if (errorCode === 'required') return `"${field}" is required — please fill it in.`;
    if (field === 'weightKg') return 'Weight must be a number greater than 0 kg.';
    if (field === 'postalCode') return 'Postal code must start with a digit between 1 and 6.';
    return 'Please check this field and try again.';
  }

  submit() {
    this.svc.createParcel(this.parcel as Parcel).subscribe({
      next: () => {
        this.parcel = { weightKg: 1 };
        this.assistGuidance = '';
        this.assistField = '';
        this.created.emit();
      },
      error: (e) => {
        const msg = e?.error?.message || e?.message || 'unknown error';
        this.fetchGuidance('form', 'server_error', msg);
      }
    });
  }
}
