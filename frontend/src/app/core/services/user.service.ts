import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserInquiryItem, UserPostItem } from '../models/user.model';
import { PostDetail } from '../models/post.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly http = inject(HttpClient);

  getMyPosts(): Observable<UserPostItem[]> {
    return this.http.get<UserPostItem[]>('/api/users/me/posts');
  }

  getMyInterests(): Observable<UserInquiryItem[]> {
    return this.http.get<UserInquiryItem[]>('/api/users/me/interests');
  }

  updatePostStatus(id: string, status: string): Observable<PostDetail> {
    return this.http.put<PostDetail>(`/api/posts/${id}/status`, { status });
  }

  deletePost(id: string): Observable<void> {
    return this.http.delete<void>(`/api/posts/${id}`);
  }
}
