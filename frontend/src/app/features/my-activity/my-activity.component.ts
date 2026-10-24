import { ChangeDetectionStrategy, Component, OnInit, inject, signal, effect, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { HeaderComponent } from '../../shared/components/header/header.component';
import { BottomNavComponent } from '../../shared/components/bottom-nav/bottom-nav.component';
import { UserInquiryItem, UserPostItem } from '../../core/models/user.model';

@Component({
  selector: 'app-my-activity',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe, HeaderComponent, BottomNavComponent],
  templateUrl: './my-activity.component.html',
  styleUrl: './my-activity.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MyActivityComponent implements OnInit {
  readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);

  readonly activeTab = signal<'posts' | 'requests'>('posts');
  readonly myPosts = signal<UserPostItem[]>([]);
  readonly myInterests = signal<UserInquiryItem[]>([]);

  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly actionLoading = signal<string | null>(null);

  constructor() {
    // Load (or reload) activity whenever the user signs in through the auth modal
    effect(() => {
      if (this.authService.isAuthenticated()) {
        untracked(() => this.loadData());
      }
    });
  }

  ngOnInit(): void {}

  switchTab(tab: 'posts' | 'requests'): void {
    this.activeTab.set(tab);
  }

  loadData(): void {
    this.loading.set(true);
    this.error.set(null);

    this.userService.getMyPosts().subscribe({
      next: (posts) => {
        this.myPosts.set(posts);
        this.loadInterests();
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set('Failed to load your posts. Please try again.');
      }
    });
  }

  private loadInterests(): void {
    this.userService.getMyInterests().subscribe({
      next: (interests) => {
        this.myInterests.set(interests);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  signIn(): void {
    this.authService.openAuthModal();
  }

  markAsTaken(post: UserPostItem): void {
    this.actionLoading.set(post.id);
    this.userService.updatePostStatus(post.id, 'TAKEN').subscribe({
      next: () => {
        this.actionLoading.set(null);
        // Optimistically update status
        this.myPosts.update((posts) =>
          posts.map((p) => (p.id === post.id ? { ...p, status: 'TAKEN' } : p))
        );
      },
      error: () => {
        this.actionLoading.set(null);
        alert('Could not update status. Please try again.');
      }
    });
  }

  confirmDelete(post: UserPostItem): void {
    if (!confirm(`Are you sure you want to delete "${post.title}"?`)) {
      return;
    }

    this.actionLoading.set(post.id);
    this.userService.deletePost(post.id).subscribe({
      next: () => {
        this.actionLoading.set(null);
        // Remove from local list
        this.myPosts.update((posts) => posts.filter((p) => p.id !== post.id));
      },
      error: () => {
        this.actionLoading.set(null);
        alert('Could not delete post. Please try again.');
      }
    });
  }
}
