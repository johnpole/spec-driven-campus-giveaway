import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe, DecimalPipe } from '@angular/common';
import { PostService } from '../../core/services/post.service';
import { InterestService } from '../../core/services/interest.service';
import { BottomNavComponent } from '../../shared/components/bottom-nav/bottom-nav.component';
import { PostDetail, PostStatus } from '../../core/models/post.model';
import { InterestDetail } from '../../core/models/interest.model';

export interface SavedPostEntry {
  id: string;
  token: string;
}

@Component({
  selector: 'app-my-post',
  standalone: true,
  imports: [RouterLink, FormsModule, DatePipe, DecimalPipe, BottomNavComponent],
  templateUrl: './my-post.component.html',
  styleUrl: './my-post.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MyPostComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly postService = inject(PostService);
  private readonly interestService = inject(InterestService);

  readonly postId = signal<string | null>(null);
  readonly editToken = signal<string | null>(null);

  readonly post = signal<PostDetail | null>(null);
  readonly inquiries = signal<InterestDetail[]>([]);
  readonly savedPosts = signal<SavedPostEntry[]>([]);

  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly actionNotice = signal<string | null>(null);
  readonly isUpdatingStatus = signal<boolean>(false);
  readonly isDeleting = signal<boolean>(false);
  readonly showDeleteConfirm = signal<boolean>(false);

  // Manual entry fields if user navigates without URL params
  manualId = '';
  manualToken = '';

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      const id = params['id'];
      const token = params['token'];

      if (id && token) {
        this.postId.set(id);
        this.editToken.set(token);
        this.loadPostAndInquiries(id, token);
      } else {
        this.loadSavedPosts();
      }
    });
  }

  loadSavedPosts(): void {
    try {
      const ids: string[] = JSON.parse(localStorage.getItem('cg_my_post_ids') || '[]');
      const entries: SavedPostEntry[] = [];
      for (const id of ids) {
        const token = localStorage.getItem(`cg_token_${id}`);
        if (token) {
          entries.push({ id, token });
        }
      }
      this.savedPosts.set(entries);
      if (entries.length === 1) {
        // Auto-select single saved post
        this.selectSavedPost(entries[0]);
      }
    } catch {
      this.savedPosts.set([]);
    }
  }

  selectSavedPost(entry: SavedPostEntry): void {
    this.postId.set(entry.id);
    this.editToken.set(entry.token);
    this.router.navigate(['/my-posts'], {
      queryParams: { id: entry.id, token: entry.token }
    });
    this.loadPostAndInquiries(entry.id, entry.token);
  }

  loadPostAndInquiries(id: string, token: string): void {
    this.loading.set(true);
    this.error.set(null);
    this.actionNotice.set(null);

    this.postService.getPost(id).subscribe({
      next: (item) => {
        this.post.set(item);
        this.loadInquiries(id, token);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err.status === 404 ? 'Listing not found.' : 'Failed to load post.');
      }
    });
  }

  private loadInquiries(id: string, token: string): void {
    this.interestService.getInterests(id, token).subscribe({
      next: (list) => {
        this.inquiries.set(list);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        if (err.status === 403) {
          this.error.set('Invalid edit token. You do not have permission to manage this post.');
        } else {
          this.error.set('Failed to load inquiries for this item.');
        }
      }
    });
  }

  toggleClaimed(): void {
    const currentPost = this.post();
    const token = this.editToken();
    if (!currentPost || !token) return;

    const newStatus: PostStatus = currentPost.status === 'AVAILABLE' ? 'CLAIMED' : 'AVAILABLE';
    this.isUpdatingStatus.set(true);
    this.actionNotice.set(null);

    this.postService.updateStatus(currentPost.id, newStatus, token).subscribe({
      next: (updated) => {
        this.post.set(updated);
        this.isUpdatingStatus.set(false);
        this.actionNotice.set(
          newStatus === 'CLAIMED'
            ? 'Item marked as claimed! Further inquiries are disabled.'
            : 'Item marked as available again.'
        );
      },
      error: (err) => {
        this.isUpdatingStatus.set(false);
        this.error.set(err.error?.message || 'Failed to update post status.');
      }
    });
  }

  promptDelete(): void {
    this.showDeleteConfirm.set(true);
  }

  cancelDelete(): void {
    this.showDeleteConfirm.set(false);
  }

  confirmDelete(): void {
    const currentPost = this.post();
    const token = this.editToken();
    if (!currentPost || !token) return;

    this.isDeleting.set(true);
    this.error.set(null);

    this.postService.deletePost(currentPost.id, token).subscribe({
      next: () => {
        this.isDeleting.set(false);
        this.showDeleteConfirm.set(false);

        // Remove from local storage
        try {
          localStorage.removeItem(`cg_token_${currentPost.id}`);
          const existing: string[] = JSON.parse(localStorage.getItem('cg_my_post_ids') || '[]');
          const updated = existing.filter((id) => id !== currentPost.id);
          localStorage.setItem('cg_my_post_ids', JSON.stringify(updated));
        } catch {
          // ignore
        }

        this.post.set(null);
        this.inquiries.set([]);
        this.actionNotice.set('Listing has been permanently deleted.');
      },
      error: (err) => {
        this.isDeleting.set(false);
        this.showDeleteConfirm.set(false);
        this.error.set(err.error?.message || 'Failed to delete listing. Please check your token.');
      }
    });
  }

  submitManualAccess(): void {
    if (this.manualId.trim() && this.manualToken.trim()) {
      this.selectSavedPost({
        id: this.manualId.trim(),
        token: this.manualToken.trim()
      });
    }
  }

  getInitial(name?: string): string {
    return name && name.trim().length > 0 ? name.trim().charAt(0).toUpperCase() : '?';
  }

  formatContactLink(contact?: string): string {
    if (!contact) return '#';
    if (contact.includes('@')) {
      return `mailto:${contact}`;
    }
    return `tel:${contact}`;
  }
}
