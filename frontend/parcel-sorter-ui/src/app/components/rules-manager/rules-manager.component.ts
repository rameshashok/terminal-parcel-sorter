import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { SortingRule } from '../../models/models';
import { ParcelService } from '../../services/parcel.service';

@Component({
  selector: 'app-rules-manager',
  standalone: true,
  imports: [CommonModule, FormsModule, MatTableModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatCheckboxModule, MatCardModule, MatIconModule],
  templateUrl: './rules-manager.component.html'
})
export class RulesManagerComponent implements OnInit {
  rules: SortingRule[] = [];
  newRule: Partial<SortingRule> = { active: true, priority: 10 };
  displayedColumns = ['ruleCode', 'postalCodePattern', 'assignedBelt', 'priority', 'active', 'actions'];

  constructor(private svc: ParcelService) {}

  ngOnInit() { this.load(); }

  load() { this.svc.getRules().subscribe(r => this.rules = r); }

  create() {
    this.svc.createRule(this.newRule as SortingRule).subscribe(() => {
      this.newRule = { active: true, priority: 10 };
      this.load();
    });
  }

  delete(rule: SortingRule) {
    this.svc.deleteRule(rule.id!).subscribe(() => this.load());
  }
}
