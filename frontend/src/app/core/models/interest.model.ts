export interface CreateInterestRequest {
  name: string;
  contact: string;
  visitorName?: string;
  visitorContact?: string;
  message: string;
  /** ISO time the requester proposes to pick up. */
  pickupAt?: string;
  pickupPlace?: string;
}

export interface InterestSummary {
  count?: number;
  totalCount?: number;
  firstNames: string[];
}

export interface InterestDetail {
  id: string;
  name: string;
  contact: string;
  message: string;
  createdAt: string;
}
