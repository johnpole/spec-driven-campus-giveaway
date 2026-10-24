import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { PostService } from '../../core/services/post.service';
import { InterestService } from '../../core/services/interest.service';
import { AuthService } from '../../core/services/auth.service';
import { MediaService } from '../../core/services/media.service';
import { PostDetail } from '../../core/models/post.model';
import { CreateInterestRequest, InterestSummary } from '../../core/models/interest.model';
import { FlierModalComponent } from './flier-modal.component';

@Component({
  selector: 'app-item-detail',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, DecimalPipe, FlierModalComponent],
  templateUrl: './item-detail.component.html',
  styleUrl: './item-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ItemDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly postService = inject(PostService);
  private readonly interestService = inject(InterestService);
  readonly authService = inject(AuthService);
  private readonly mediaService = inject(MediaService);
  private readonly fb = inject(FormBuilder);

  readonly post = signal<PostDetail | null>(null);
  readonly interestSummary = signal<InterestSummary | null>(null);
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);

  readonly sheetOpen = signal<boolean>(false);
  readonly flierOpen = signal<boolean>(false);
  readonly isSubmitting = signal<boolean>(false);
  readonly sheetError = signal<string | null>(null);
  readonly toastMessage = signal<string | null>(null);

  readonly interestForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    contact: [
      '',
      [
        Validators.required,
        Validators.pattern('^([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|\\+[1-9]\\d{1,14})$')
      ]
    ],
    message: ['Hi! When can I pick this up?', [Validators.required, Validators.maxLength(500)]]
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error.set('Item ID not specified');
      this.loading.set(false);
      return;
    }
    this.loadItemData(id);
  }

  loadItemData(id: string): void {
    this.loading.set(true);
    this.error.set(null);

    this.postService.getPost(id).subscribe({
      next: (item) => {
        this.post.set(item);
        this.loading.set(false);
        this.loadInterests(id);
      },
      error: (err) => {
        this.error.set(err.status === 404 ? 'Item not found' : 'Failed to load item details');
        this.loading.set(false);
      }
    });
  }

  private loadInterests(id: string): void {
    this.interestService.getSummary(id).subscribe({
      next: (summary) => {
        this.interestSummary.set({
          ...summary,
          count: summary.count ?? summary.totalCount ?? 0,
          totalCount: summary.totalCount ?? summary.count ?? 0
        });
      },
      error: () => {
        // Non-blocking: fallback to post detail interestCount
      }
    });
  }

  openInterestSheet(): void {
    if (this.post()?.status === 'CLAIMED') {
      return;
    }
    this.sheetError.set(null);
    this.sheetOpen.set(true);
  }

  closeInterestSheet(): void {
    this.sheetOpen.set(false);
    this.sheetError.set(null);
  }

  openFlierModal(): void {
    this.flierOpen.set(true);
  }

  closeFlierModal(): void {
    this.flierOpen.set(false);
  }

  applyQuickSuggestion(text: string): void {
    const posterName = this.post()?.posterName || 'there';
    this.interestForm.patchValue({
      message: `Hi ${posterName}! ${text}. Can I pick this up?`
    });
  }

  submitInterest(): void {
    if (this.interestForm.invalid) {
      this.interestForm.markAllAsTouched();
      return;
    }

    const currentPost = this.post();
    if (!currentPost) return;

    this.isSubmitting.set(true);
    this.sheetError.set(null);

    const formVal = this.interestForm.value;
    const req: CreateInterestRequest = {
      name: formVal.name!.trim(),
      contact: formVal.contact!.trim(),
      message: formVal.message?.trim() || ''
    };

    this.interestService.createInterest(currentPost.id, req).subscribe({
      next: (summary) => {
        if (summary) {
          this.interestSummary.set({
            ...summary,
            count: summary.count ?? summary.totalCount ?? 0,
            totalCount: summary.totalCount ?? summary.count ?? 0
          });
        }
        this.isSubmitting.set(false);
        this.closeInterestSheet();
        this.showToast(`Note sent to ${currentPost.posterName}!`);
        this.interestService.getSummary(currentPost.id).subscribe({
          next: (s) => this.interestSummary.set({
            ...s,
            count: s.count ?? s.totalCount ?? 0,
            totalCount: s.totalCount ?? s.count ?? 0
          }),
          error: () => {}
        });
      },
      error: (err) => {
        this.isSubmitting.set(false);
        if (err.status === 409) {
          this.sheetError.set('This item has just been claimed or has expired.');
        } else {
          this.sheetError.set(err.error?.message || err.error?.error || 'Failed to send interest note. Please check your contact information.');
        }
      }
    });
  }

  shareListing(): void {
    const currentPost = this.post();
    if (!currentPost) return;

    const url = window.location.href;
    const shareData = {
      title: `${currentPost.title} on Campus Giveaway`,
      text: `Check out ${currentPost.title} (${currentPost.isFree ? 'FREE' : '$' + currentPost.price}) at ${currentPost.pickupLocation}!`,
      url
    };

    if (navigator.share) {
      navigator.share(shareData).catch(() => {});
    } else {
      navigator.clipboard?.writeText(url).then(() => {
        this.showToast('Listing link copied to clipboard!');
      }).catch(() => {
        this.showToast('Listing URL: ' + url);
      });
    }
  }

  adminMarkStatus(status: 'AVAILABLE' | 'TAKEN'): void {
    const item = this.post();
    if (!item) return;

    this.postService.updateStatus(item.id, status, '').subscribe({
      next: (updated) => {
        this.post.set(updated);
        this.showToast(`Admin: Status updated to ${status}`);
      },
      error: () => {
        this.showToast('Failed to update status');
      }
    });
  }

  adminDeletePost(): void {
    const item = this.post();
    if (!item) return;

    if (confirm(`Admin: Are you sure you want to delete "${item.title}"?`)) {
      this.postService.deletePost(item.id, '').subscribe({
        next: () => {
          this.router.navigate(['/']).then(() => {
            alert(`Admin: Item "${item.title}" successfully deleted.`);
          });
        },
        error: () => {
          this.showToast('Admin: Failed to delete item');
        }
      });
    }
  }

  adminToggleAi(): void {
    this.mediaService.toggleAutofill().subscribe({
      next: (res) => {
        this.showToast(`Admin: Gemini AI Vision is now ${res.enabled ? 'ENABLED' : 'DISABLED'}`);
      },
      error: () => {
        this.showToast('Admin: Failed to toggle Gemini AI feature flag');
      }
    });
  }

  showToast(message: string): void {
    this.toastMessage.set(message);
    setTimeout(() => {
      this.toastMessage.set(null);
    }, 4000);
  }

  dismissToast(): void {
    this.toastMessage.set(null);
  }

  formatCondition(cond?: string): string {
    switch (cond) {
      case 'LIKE_NEW': return 'Like new';
      case 'GOOD': return 'Good condition';
      case 'FAIR': return 'Fair condition';
      default: return cond || '';
    }
  }

  formatDate(iso?: string): string {
    if (!iso) return '';
    try {
      const d = new Date(iso);
      return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    } catch {
      return iso;
    }
  }

  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300" viewBox="0 0 400 300"><rect width="400" height="300" fill="%23f0ede8"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="%238a9099" font-family="sans-serif" font-size="16">Campus Giveaway Photo</text></svg>';
  }
}
