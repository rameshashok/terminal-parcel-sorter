import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AiResponse, AssistRequest, AssistResponse, BatchResult, FeedbackRequest, Parcel, SortingRule } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ParcelService {
  private base = '/api';

  constructor(private http: HttpClient) {}

  getParcels(): Observable<Parcel[]> {
    return this.http.get<Parcel[]>(`${this.base}/parcels`);
  }

  createParcel(parcel: Parcel): Observable<Parcel> {
    return this.http.post<Parcel>(`${this.base}/parcels`, parcel);
  }

  sortParcel(id: number): Observable<Parcel> {
    return this.http.post<Parcel>(`${this.base}/parcels/${id}/sort`, {});
  }

  updateStatus(id: number, status: string): Observable<Parcel> {
    return this.http.put<Parcel>(`${this.base}/parcels/${id}/status`, { status });
  }

  deleteParcel(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/parcels/${id}`);
  }

  getRules(): Observable<SortingRule[]> {
    return this.http.get<SortingRule[]>(`${this.base}/rules`);
  }

  createRule(rule: SortingRule): Observable<SortingRule> {
    return this.http.post<SortingRule>(`${this.base}/rules`, rule);
  }

  updateRule(id: number, rule: SortingRule): Observable<SortingRule> {
    return this.http.put<SortingRule>(`${this.base}/rules/${id}`, rule);
  }

  deleteRule(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/rules/${id}`);
  }

  askAi(question: string): Observable<AiResponse> {
    return this.http.post<AiResponse>(`${this.base}/ai/ask`, { question });
  }

  getAssistance(request: AssistRequest): Observable<AssistResponse> {
    return this.http.post<AssistResponse>(`${this.base}/ai/assist`, request);
  }

  uploadBatch(parcels: Parcel[]): Observable<BatchResult[]> {
    return this.http.post<BatchResult[]>(`${this.base}/parcels/batch`, parcels);
  }

  sendFeedback(request: FeedbackRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/ai/feedback`, request);
  }
}
