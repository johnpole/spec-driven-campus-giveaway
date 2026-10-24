import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import {
  Category,
  CreatePostRequest,
  CreatePostResponse,
  PostDetail,
  PostStatus,
  PostSummary,
  UpdateStatusRequest
} from '../models/post.model';

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/posts';

  // Signals for global feed state
  readonly posts = signal<PostSummary[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly selectedCategory = signal<Category | null>(null);
  readonly freeOnly = signal<boolean>(false);
  readonly searchQuery = signal<string>('');

  getFeed(category?: string | null, freeOnly?: boolean, query?: string): Observable<PostSummary[]> {
    let params = new HttpParams();
    if (category) {
      params = params.set('category', category);
    }
    if (freeOnly) {
      params = params.set('freeOnly', 'true');
    }
    if (query && query.trim().length > 0) {
      params = params.set('q', query.trim());
    }

    return this.http.get<PostSummary[]>(this.baseUrl, { params });
  }

  loadFeed(): void {
    this.loading.set(true);
    this.error.set(null);

    this.getFeed(this.selectedCategory(), this.freeOnly(), this.searchQuery()).subscribe({
      next: (items) => {
        this.posts.set(items);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.message || 'Failed to load posts');
        this.loading.set(false);
      }
    });
  }

  getPost(id: string): Observable<PostDetail> {
    return this.http.get<PostDetail>(`${this.baseUrl}/${id}`);
  }

  createPost(request: CreatePostRequest): Observable<CreatePostResponse> {
    return this.http.post<CreatePostResponse>(this.baseUrl, request).pipe(
      tap(() => this.loadFeed())
    );
  }

  updateStatus(id: string, status: PostStatus, editToken: string): Observable<PostDetail> {
    const headers = new HttpHeaders({
      'X-Edit-Token': editToken
    });
    const body: UpdateStatusRequest = { status };
    return this.http.patch<PostDetail>(`${this.baseUrl}/${id}/status`, body, { headers });
  }

  deletePost(id: string, editToken: string): Observable<void> {
    const headers = new HttpHeaders({
      'X-Edit-Token': editToken
    });
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { headers }).pipe(
      tap(() => this.loadFeed())
    );
  }
}
