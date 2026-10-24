import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateInterestRequest, InterestDetail, InterestSummary } from '../models/interest.model';

@Injectable({
  providedIn: 'root'
})
export class InterestService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/posts';

  createInterest(postId: string, request: CreateInterestRequest): Observable<InterestSummary> {
    return this.http.post<InterestSummary>(`${this.baseUrl}/${postId}/interests`, request);
  }

  getSummary(postId: string): Observable<InterestSummary> {
    return this.http.get<InterestSummary>(`${this.baseUrl}/${postId}/interests/summary`);
  }

  getInterests(postId: string, editToken: string): Observable<InterestDetail[]> {
    const headers = new HttpHeaders({
      'X-Edit-Token': editToken
    });
    return this.http.get<InterestDetail[]>(`${this.baseUrl}/${postId}/interests`, { headers });
  }
}
