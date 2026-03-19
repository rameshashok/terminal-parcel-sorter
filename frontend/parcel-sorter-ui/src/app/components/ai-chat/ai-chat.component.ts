import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { ParcelService } from '../../services/parcel.service';

interface Message { role: 'user' | 'ai'; text: string; }

@Component({
  selector: 'app-ai-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatCardModule, MatProgressSpinnerModule, MatIconModule],
  templateUrl: './ai-chat.component.html'
})
export class AiChatComponent {
  messages: Message[] = [];
  question = '';
  loading = false;

  constructor(private svc: ParcelService) {}

  ask() {
    if (!this.question.trim()) return;
    this.messages.push({ role: 'user', text: this.question });
    this.loading = true;
    const q = this.question;
    this.question = '';
    this.svc.askAi(q).subscribe({
      next: r => { this.messages.push({ role: 'ai', text: r.answer }); this.loading = false; },
      error: (e) => { this.messages.push({ role: 'ai', text: 'Error: ' + (e?.error?.message || e?.message || JSON.stringify(e)) }); this.loading = false; }
    });
  }
}
