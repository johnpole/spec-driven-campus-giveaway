import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  input,
  output,
  signal
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { InterestService } from '../../core/services/interest.service';
import { PostDetail } from '../../core/models/post.model';
import { CreateInterestRequest, InterestSummary } from '../../core/models/interest.model';

@Component({
  selector: 'app-interest-modal',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div
      class="sheet-backdrop"
      [class.open]="isOpen()"
      (click)="onBackdropClick()"
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-sheet-title">
      <div
        class="sheet-panel"
        [class.open]="isOpen()"
        (click)="$event.stopPropagation()">
        <!-- Handle -->
        <div class="sheet-handle"></div>

        <!-- Sheet Header -->
        <div class="sheet-header">
          <div>
            <h3 class="sheet-title" id="modal-sheet-title">Express Interest</h3>
            <p class="sheet-subtitle">
              Send a direct handoff note to {{ post()?.posterName || 'seller' }}
            </p>
          </div>
          <button
            type="button"
            class="icon-close-btn"
            (click)="closeModal()"
            aria-label="Close sheet">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>

        @if (sheetError()) {
          <div class="sheet-error-banner" role="alert">
            <span class="material-symbols-outlined">error</span>
            <span>{{ sheetError() }}</span>
          </div>
        }

        @if (!authService.isAuthenticated()) {
          <div class="modal-auth-prompt">
            <div class="auth-icon-circle">
              <span class="material-symbols-outlined">lock</span>
            </div>
            <h4 class="auth-prompt-title">Sign in to contact seller</h4>
            <p class="auth-prompt-desc">
              To keep RPI Campus Giveaway safe and spam-free, sign in with your Google account to send pickup notes.
            </p>
            <div class="auth-buttons">
              <button
                type="button"
                class="sign-in-primary-btn"
                (click)="signIn()">
                Sign in or create account
              </button>
            </div>
          </div>
        } @else {
          <!-- Authenticated User Identity Pill -->
          <div class="verified-buyer-pill">
            <div class="buyer-avatar-badge">
              <span class="material-symbols-outlined verified-icon">verified_user</span>
            </div>
            <div class="buyer-info">
              <span class="buyer-name">{{ authService.currentUser()?.name }}</span>
              <span class="buyer-email">{{ authService.currentUser()?.email }}</span>
            </div>
            <span class="verified-tag">Google Verified</span>
          </div>

          <!-- Streamlined Message Form -->
          <form [formGroup]="interestForm" (ngSubmit)="submitInterest()" class="streamlined-form">
            <div class="form-group">
              <label class="form-label" for="pickup-note">
                Pickup message for {{ post()?.posterName || 'the seller' }}
              </label>
              <textarea
                id="pickup-note"
                formControlName="message"
                class="form-textarea"
                rows="3"
                placeholder="e.g. Can meet at Commons tomorrow noon!">
              </textarea>
              @if (interestForm.get('message')?.invalid && interestForm.get('message')?.touched) {
                <span class="field-error">Please enter a pickup message (max 500 chars).</span>
              }
            </div>

            <!-- Quick Suggestion Chips -->
            <div class="suggestions-row">
              <button type="button" class="sugg-chip" (click)="applyQuickSuggestion('Tonight at 7pm')">
                ⚡️ Tonight at 7pm
              </button>
              <button type="button" class="sugg-chip" (click)="applyQuickSuggestion('Can pick up right now')">
                📦 Can pick up now
              </button>
              <button type="button" class="sugg-chip" (click)="applyQuickSuggestion('I have a car / transport')">
                🚗 Have transport!
              </button>
            </div>

            <button
              type="submit"
              class="submit-btn"
              [disabled]="isSubmitting()">
              @if (isSubmitting()) {
                <span>Sending note...</span>
              } @else {
                <span class="material-symbols-outlined">send</span>
                <span>Send pickup note</span>
              }
            </button>
          </form>
        }
      </div>
    </div>
  `,
  styles: [`
    .sheet-backdrop {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.45);
      backdrop-filter: blur(4px);
      -webkit-backdrop-filter: blur(4px);
      z-index: 100;
      opacity: 0;
      visibility: hidden;
      transition: opacity 0.25s ease, visibility 0.25s ease;
      display: flex;
      align-items: flex-end;
      justify-content: center;

      &.open {
        opacity: 1;
        visibility: visible;
      }
    }

    .sheet-panel {
      background: #ffffff;
      width: 100%;
      max-width: var(--app-max-width, 480px);
      border-radius: 20px 20px 0 0;
      padding: 16px 20px 28px;
      transform: translateY(100%);
      transition: transform 0.28s cubic-bezier(0.16, 1, 0.3, 1);
      box-shadow: 0 -8px 30px rgba(0, 0, 0, 0.15);
      max-height: 85vh;
      overflow-y: auto;

      &.open {
        transform: translateY(0);
      }
    }

    .sheet-handle {
      width: 36px;
      height: 4px;
      background: #e5e7eb;
      border-radius: 2px;
      margin: 0 auto 12px;
    }

    .sheet-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      margin-bottom: 16px;

      .sheet-title {
        font-size: 18px;
        font-weight: 700;
        color: #111827;
        margin: 0 0 2px 0;
      }

      .sheet-subtitle {
        font-size: 13px;
        color: #6b7280;
        margin: 0;
      }

      .icon-close-btn {
        background: #f3f4f6;
        border: none;
        border-radius: 50%;
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #4b5563;
        cursor: pointer;

        &:hover {
          background: #e5e7eb;
        }
      }
    }

    .sheet-error-banner {
      background: #fee2e2;
      border: 1px solid #fecaca;
      color: #991b1b;
      padding: 10px 14px;
      border-radius: 8px;
      font-size: 13px;
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 16px;
    }

    .modal-auth-prompt {
      text-align: center;
      padding: 16px 8px;

      .auth-icon-circle {
        width: 48px;
        height: 48px;
        background: #fee2e2;
        color: #e11d48;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 12px;
      }

      .auth-prompt-title {
        font-size: 17px;
        font-weight: 700;
        color: #111827;
        margin: 0 0 6px 0;
      }

      .auth-prompt-desc {
        font-size: 13px;
        color: #6b7280;
        margin: 0 0 20px 0;
        line-height: 1.4;
      }

      .auth-buttons {
        display: flex;
        flex-direction: column;
        gap: 8px;

        .sign-in-primary-btn {
          background: #e11d48;
          color: #ffffff;
          font-weight: 600;
          font-size: 15px;
          padding: 12px;
          border-radius: 12px;
          border: none;
          cursor: pointer;
          min-height: 48px;
        }

        .sign-in-demo-btn {
          background: #f3f4f6;
          color: #374151;
          font-weight: 600;
          font-size: 14px;
          padding: 10px;
          border-radius: 12px;
          border: 1px solid #d1d5db;
          cursor: pointer;
          min-height: 44px;
        }
      }
    }

    .verified-buyer-pill {
      display: flex;
      align-items: center;
      gap: 10px;
      background: #f0fdf4;
      border: 1px solid #bbf7d0;
      border-radius: 12px;
      padding: 10px 12px;
      margin-bottom: 16px;

      .buyer-avatar-badge {
        color: #16a34a;
        display: flex;
        align-items: center;
      }

      .buyer-info {
        flex: 1;
        min-width: 0;

        .buyer-name {
          font-weight: 700;
          font-size: 13px;
          color: #166534;
          display: block;
        }

        .buyer-email {
          font-size: 11px;
          color: #15803d;
          display: block;
        }
      }

      .verified-tag {
        font-size: 10px;
        font-weight: 700;
        background: #dcfce7;
        color: #15803d;
        padding: 3px 6px;
        border-radius: 6px;
        text-transform: uppercase;
      }
    }

    .streamlined-form {
      display: flex;
      flex-direction: column;
      gap: 14px;

      .form-group {
        display: flex;
        flex-direction: column;
        gap: 6px;

        .form-label {
          font-size: 13px;
          font-weight: 600;
          color: #374151;
        }

        .form-textarea {
          border: 1.5px solid #d1d5db;
          border-radius: 10px;
          padding: 10px 12px;
          font-family: inherit;
          font-size: 14px;
          resize: vertical;
          min-height: 80px;

          &:focus {
            outline: none;
            border-color: #e11d48;
            box-shadow: 0 0 0 3px rgba(225, 29, 72, 0.15);
          }
        }

        .field-error {
          font-size: 12px;
          color: #dc2626;
        }
      }

      .suggestions-row {
        display: flex;
        gap: 6px;
        flex-wrap: wrap;

        .sugg-chip {
          background: #f3f4f6;
          border: 1px solid #e5e7eb;
          border-radius: 16px;
          padding: 6px 10px;
          font-size: 12px;
          color: #374151;
          cursor: pointer;
          min-height: 32px;

          &:hover {
            background: #e5e7eb;
          }
        }
      }

      .submit-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        background: #e11d48;
        color: #ffffff;
        font-size: 15px;
        font-weight: 700;
        padding: 12px 18px;
        border-radius: 12px;
        border: none;
        cursor: pointer;
        min-height: 48px;

        &:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }

        &:hover:not(:disabled) {
          background: #be123c;
        }
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InterestModalComponent implements OnInit {
  readonly authService = inject(AuthService);
  private readonly interestService = inject(InterestService);
  private readonly fb = inject(FormBuilder);

  readonly isOpen = input<boolean>(false);
  readonly post = input<PostDetail | null>(null);

  readonly closed = output<void>();
  readonly submitted = output<InterestSummary>();

  readonly isSubmitting = signal<boolean>(false);
  readonly sheetError = signal<string | null>(null);

  readonly interestForm = this.fb.group({
    message: ['Hi! When can I pick this up?', [Validators.required, Validators.maxLength(500)]]
  });

  ngOnInit(): void {}

  onBackdropClick(): void {
    this.closeModal();
  }

  closeModal(): void {
    this.sheetError.set(null);
    this.closed.emit();
  }

  signIn(): void {
    this.authService.openAuthModal();
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

    const user = this.authService.currentUser();
    if (!user) {
      this.sheetError.set('Please sign in before sending a note.');
      return;
    }

    this.isSubmitting.set(true);
    this.sheetError.set(null);

    const message = this.interestForm.value.message?.trim() || '';
    const req: CreateInterestRequest = {
      name: user.name,
      contact: user.email,
      message
    };

    this.interestService.createInterest(currentPost.id, req).subscribe({
      next: (summary) => {
        this.isSubmitting.set(false);
        this.submitted.emit(summary);
        this.closeModal();
      },
      error: (err) => {
        this.isSubmitting.set(false);
        if (err.status === 409) {
          this.sheetError.set('This item has just been claimed or has expired.');
        } else if (err.status === 400 && err.error?.message?.includes('own post')) {
          this.sheetError.set('You cannot request an item you posted yourself.');
        } else {
          this.sheetError.set(err.error?.message || 'Failed to send pickup note. Please try again.');
        }
      }
    });
  }
}
