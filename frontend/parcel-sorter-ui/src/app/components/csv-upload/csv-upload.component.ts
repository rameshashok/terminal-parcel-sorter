import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { ParcelService } from '../../services/parcel.service';
import { BatchResult, Parcel } from '../../models/models';

@Component({
  selector: 'app-csv-upload',
  standalone: true,
  imports: [
    CommonModule, MatCardModule, MatButtonModule, MatIconModule,
    MatTableModule, MatProgressBarModule, MatChipsModule
  ],
  templateUrl: './csv-upload.component.html',
  styles: [`
    .drop-zone {
      border: 2px dashed #1976d2; border-radius: 8px; padding: 40px;
      text-align: center; cursor: pointer; background: #f5f9ff;
      transition: background 0.2s;
    }
    .drop-zone:hover { background: #e3f0ff; }
    .drop-zone mat-icon { font-size: 48px; height: 48px; width: 48px; color: #1976d2; }
    .preview-table { margin-top: 16px; width: 100%; }
    .result-ok { color: #388e3c; font-weight: 500; }
    .result-error { color: #d32f2f; font-weight: 500; }
    .ai-guidance {
      font-size: 12px; color: #555; background: #fff8e1;
      border-left: 3px solid #f9a825; padding: 6px 10px; margin-top: 4px;
      border-radius: 4px;
    }
    .sample-hint { font-size: 12px; color: #888; margin-top: 8px; }
    .actions { display: flex; gap: 12px; margin-top: 16px; align-items: center; }
  `]
})
export class CsvUploadComponent {
  preview: Parcel[] = [];
  results: BatchResult[] = [];
  uploading = false;
  fileName = '';
  displayedPreviewCols = ['trackingNumber', 'origin', 'destination', 'postalCode', 'weightKg'];
  displayedResultCols = ['row', 'trackingNumber', 'status', 'error'];

  constructor(private svc: ParcelService) {}

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    const file = input.files[0];
    this.fileName = file.name;
    this.results = [];

    const reader = new FileReader();
    reader.onload = (e) => {
      const text = e.target?.result as string;
      this.preview = this.parseCsv(text);
    };
    reader.readAsText(file);
  }

  private parseCsv(text: string): Parcel[] {
    const lines = text.trim().split('\n').map(l => l.trim()).filter(l => l);
    if (lines.length < 2) return [];
    // skip header row
    return lines.slice(1).map(line => {
      const [trackingNumber, origin, destination, postalCode, weightKgStr] = line.split(',').map(c => c.trim());
      return { trackingNumber, origin, destination, postalCode, weightKg: parseFloat(weightKgStr) || 0 };
    });
  }

  upload() {
    if (!this.preview.length) return;
    this.uploading = true;
    this.results = [];
    this.svc.uploadBatch(this.preview).subscribe({
      next: r => { this.results = r; this.uploading = false; },
      error: () => { this.uploading = false; }
    });
  }

  reset() {
    this.preview = [];
    this.results = [];
    this.fileName = '';
  }

  get successCount() { return this.results.filter(r => r.status === 'OK').length; }
  get errorCount() { return this.results.filter(r => r.status === 'ERROR').length; }
}
