import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { UserService } from '../../../core/services/user.service';
import { InterestReply } from '../../../core/models/user.model';

/** The pickup plan (when + where) and the back-and-forth between poster and requester. */
@Component({
  selector: 'app-pickup-thread',
  standalone: true,
  imports: [DatePipe],
  template: `
    <div class="pickup-plan">
      <span class="plan-item">
        <span class="material-symbols-outlined" aria-hidden="true">schedule</span>
        {{ pickupAt() ? (pickupAt() | date: 'EEE, MMM d · h:mm a') : 'Time not set' }}
      </span>
      <span class="plan-item">
        <span class="material-symbols-outlined" aria-hidden="true">location_on</span>
        {{ pickupPlace() || 'Place not set' }}
      </span>
    </div>

    @if (firstMessage()) {
      <p class="bubble bubble-buyer"><strong>{{ requesterName() }}:</strong> {{ firstMessage() }}</p>
    }
    @for (r of thread(); track r.id) {
      <p class="bubble" [class.bubble-mine]="r.authorRole === myRole()" [class.bubble-buyer]="r.authorRole === 'BUYER'">
        <strong>{{ r.authorRole === myRole() ? 'You' : r.authorName }}:</strong> {{ r.message }}
        <span class="bubble-time">{{ r.createdAt | date: 'MMM d, h:mm a' }}</span>
      </p>
    }

    <form class="reply-row" (submit)="send($event)">
      <label class="visually-hidden" [for]="'reply-' + interestId()">Reply</label>
      <input
        [id]="'reply-' + interestId()"
        class="reply-input"
        type="text"
        maxlength="500"
        [placeholder]="myRole() === 'SELLER' ? 'Reply to confirm the pickup…' : 'Reply to the poster…'"
        [value]="draft()"
        (input)="draft.set($any($event.target).value)"
        (keydown.enter)="send($event)" />
      <button type="submit" class="reply-btn" [disabled]="!draft().trim() || sending()" aria-label="Send reply">
        <span class="material-symbols-outlined" aria-hidden="true">send</span>
      </button>
    </form>
    @if (error()) {
      <p class="reply-error" role="alert">{{ error() }}</p>
    }
  `,
  styles: [`
    :host { display: flex; flex-direction: column; gap: 8px; margin-top: 8px; }
    .pickup-plan { display: flex; flex-wrap: wrap; gap: 6px; }
    .plan-item {
      display: inline-flex; align-items: center; gap: 4px; padding: 4px 10px;
      border-radius: var(--radius-pill); background: var(--color-primary-soft); color: var(--color-secondary);
      font-size: 13px; font-weight: 600;
      .material-symbols-outlined { font-size: 16px; color: var(--color-primary); }
    }
    .bubble {
      margin: 0; max-width: 92%; padding: 8px 12px; border-radius: 14px;
      background: var(--color-canvas); border: 1px solid var(--color-border-hairline);
      color: var(--color-secondary); font-size: 14px; line-height: 1.4;
    }
    .bubble-mine { align-self: flex-end; background: var(--color-tertiary-soft); border-color: var(--color-tertiary-border); }
    .bubble-time { display: block; margin-top: 2px; color: var(--color-secondary-muted); font-size: 11px; }
    .reply-row { display: flex; gap: 6px; }
    .reply-input {
      flex: 1; min-width: 0; min-height: 44px; padding: 0 12px;
      border: 1px solid var(--color-border-hairline); border-radius: var(--radius-pill);
      background: var(--color-surface); color: var(--color-secondary); font: 400 15px/20px var(--font-body);
      &:focus { outline: 2px solid var(--color-primary); outline-offset: 1px; }
    }
    .reply-btn {
      display: grid; place-items: center; width: 44px; height: 44px; border: 0; border-radius: var(--radius-pill);
      background: var(--color-primary); color: var(--color-surface); cursor: pointer;
      &:disabled { opacity: 0.4; cursor: not-allowed; }
    }
    .reply-error { margin: 0; color: var(--color-status-red); font-size: 13px; }
    .visually-hidden { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0 0 0 0); }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PickupThreadComponent {
  private readonly userService = inject(UserService);

  readonly interestId = input.required<string>();
  readonly myRole = input.required<'SELLER' | 'BUYER'>();
  readonly requesterName = input<string>('Requester');
  readonly firstMessage = input<string | null>(null);
  readonly pickupAt = input<string | null>(null);
  readonly pickupPlace = input<string | null>(null);
  readonly replies = input<InterestReply[]>([]);

  readonly sent = signal<InterestReply[]>([]);
  readonly draft = signal('');
  readonly sending = signal(false);
  readonly error = signal<string | null>(null);

  thread(): InterestReply[] {
    return [...this.replies(), ...this.sent()];
  }

  send(event: Event): void {
    event.preventDefault();
    const message = this.draft().trim();
    if (!message) return;
    this.sending.set(true);
    this.error.set(null);
    this.userService.reply(this.interestId(), message).subscribe({
      next: (reply) => {
        this.sent.update((list) => [...list, reply]);
        this.draft.set('');
        this.sending.set(false);
      },
      error: () => {
        this.sending.set(false);
        this.error.set('Reply failed. Please try again.');
      }
    });
  }
}
