import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MediaUploadResponse } from '../models/media.model';

export interface AutoFillStatusResponse {
  enabled: boolean;
  remainingSeconds: number;
}

export interface AutoFillSuggestion {
  title: string;
  category: string;
  description: string;
  suggestedPrice: number;
  condition: string;
}

@Injectable({
  providedIn: 'root'
})
export class MediaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/media/upload';

  uploadPhoto(file: File): Observable<MediaUploadResponse> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    return this.http.post<MediaUploadResponse>(this.baseUrl, formData);
  }

  getAutofillStatus(): Observable<AutoFillStatusResponse> {
    return this.http.get<AutoFillStatusResponse>('/api/media/autofill/status');
  }

  toggleAutofill(enable?: boolean, minutes: number = 10): Observable<AutoFillStatusResponse> {
    const query = enable !== undefined ? `?enabled=${enable}&minutes=${minutes}` : '';
    return this.http.post<AutoFillStatusResponse>(
      `/api/media/autofill/toggle${query}`,
      {}
    );
  }

  /** Asks Gemini to analyse an already-uploaded photo and suggest listing details. */
  autoFill(photoUrl: string): Observable<AutoFillSuggestion> {
    return this.http.post<AutoFillSuggestion>('/api/media/autofill', { photoUrl });
  }
}
