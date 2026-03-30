export type ParcelStatus = 'RECEIVED' | 'SORTING' | 'SORTED' | 'DISPATCHED' | 'EXCEPTION';

export interface Parcel {
  id?: number;
  trackingNumber: string;
  origin: string;
  destination: string;
  postalCode: string;
  weightKg: number;
  status?: ParcelStatus;
  assignedBelt?: string;
  aiReasoning?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface SortingRule {
  id?: number;
  ruleCode: string;
  description: string;
  postalCodePattern: string;
  assignedBelt: string;
  priority: number;
  active: boolean;
}

export interface AiResponse {
  answer: string;
}

export interface AssistRequest {
  field: string;
  errorCode: string;
  value: string;
  formContext: string;
}

export interface AssistResponse {
  guidance: string;
}

export interface BatchResult {
  row: number;
  trackingNumber: string;
  status: 'OK' | 'ERROR';
  error?: string;
  aiGuidance?: string;
}

export interface FeedbackRequest {
  field: string;
  errorCode: string;
  corrected: boolean;
}
