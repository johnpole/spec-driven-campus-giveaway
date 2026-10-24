import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { PostSummary } from '../../../core/models/post.model';
import { FeedLayout } from '../../../core/services/view-preferences.service';

const CATEGORY_ICONS: Record<string, string> = {
  Furniture: 'chair',
  Electronics: 'devices',
  Textbooks: 'menu_book',
  Kitchen: 'blender',
  'Dorm essentials': 'bed',
  Clothing: 'checkroom',
  Bikes: 'pedal_bike',
  Other: 'category'
};

const DAY_MS = 24 * 60 * 60 * 1000;

@Component({
  selector: 'app-item-card',
  standalone: true,
  imports: [RouterLink, DecimalPipe],
  host: {
    '[class.is-list]': "layout === 'list'",
    '[class.is-compact]': 'compact'
  },
  template: `
    <a class="item-card" [routerLink]="['/items', post.id]" [attr.aria-label]="ariaLabel">
      <div class="image-container">
        <img [src]="post.photoUrl" [alt]="post.title" class="card-img" loading="lazy" (error)="onImageError($event)" />

        <div class="price-pill-wrapper">
          @if (post.isFree) {
            <span class="pill-price-free">FREE</span>
          } @else {
            <span class="pill-price-paid">\${{ post.price | number: '1.0-0' }}</span>
          }
        </div>

        @if (isNew) {
          <span class="new-badge">Just posted</span>
        }
      </div>

      <div class="content-container">
        <h3 class="card-title" [title]="post.title">{{ post.title }}</h3>

        <span class="location-label">
          <span class="material-symbols-outlined pin-icon" aria-hidden="true">location_on</span>
          <span class="location-text">{{ post.pickupLocation }}</span>
        </span>

        <div class="tags-row">
          <span class="tag">
            <span class="material-symbols-outlined tag-icon" aria-hidden="true">{{ categoryIcon }}</span>
            {{ post.category }}
          </span>
          <span class="tag">{{ formatCondition(post.condition) }}</span>
          @if (post.interestCount > 0) {
            <span class="tag tag-hot">
              <span class="material-symbols-outlined tag-icon" aria-hidden="true">local_fire_department</span>
              {{ post.interestCount }} interested
            </span>
          }
        </div>
      </div>
    </a>
  `,
  styles: [`
    :host { display: block; min-width: 0; }

    .item-card {
      display: flex;
      flex-direction: column;
      height: 100%;
      background: var(--color-surface);
      border: 1px solid var(--color-border-hairline);
      border-radius: 16px;
      overflow: hidden;
      box-shadow: var(--shadow-card);
      color: inherit;
      text-decoration: none;
      transition: transform 0.15s ease, box-shadow 0.15s ease;

      &:hover { transform: translateY(-2px); box-shadow: var(--shadow-floating); }
      &:active { transform: scale(0.98); }
      &:focus-visible { outline: 3px solid var(--color-primary); outline-offset: 2px; }
    }

    .image-container {
      position: relative;
      aspect-ratio: 1 / 1;
      background: var(--color-canvas);
      overflow: hidden;
    }

    .card-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }

    .item-card:hover .card-img { transform: scale(1.04); }

    .price-pill-wrapper { position: absolute; top: 8px; left: 8px; }

    .pill-price-free, .pill-price-paid {
      display: inline-block;
      padding: 4px 10px;
      border-radius: var(--radius-pill);
      font: 700 12px/16px var(--font-display);
      color: var(--color-surface);
      box-shadow: var(--shadow-card);
    }

    .pill-price-free { background: var(--color-tertiary-green); }
    .pill-price-paid { background: var(--color-secondary); }

    .new-badge {
      position: absolute;
      top: 8px;
      right: 8px;
      padding: 3px 8px;
      border-radius: var(--radius-pill);
      background: var(--color-primary);
      color: var(--color-surface);
      font: 700 10px/14px var(--font-body);
      letter-spacing: 0.02em;
    }

    .content-container {
      display: flex;
      flex-direction: column;
      gap: 6px;
      flex: 1;
      min-width: 0;
      padding: 10px 12px 12px;
    }

    .card-title {
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      margin: 0;
      font: 600 14px/19px var(--font-display);
      color: var(--color-secondary);
    }

    .location-label {
      display: flex;
      align-items: center;
      gap: 2px;
      min-width: 0;
      color: var(--color-secondary-muted);
      font-size: 12px;
    }

    .pin-icon { font-size: 14px; color: var(--color-primary); }
    .location-text { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

    .tags-row { display: flex; flex-wrap: wrap; gap: 4px; margin-top: auto; }

    .tag {
      display: inline-flex;
      align-items: center;
      gap: 2px;
      padding: 2px 7px;
      border-radius: 6px;
      background: var(--color-canvas);
      color: var(--color-secondary-muted);
      font-size: 11px;
      font-weight: 600;
    }

    .tag-icon { font-size: 13px; }
    .tag-hot { background: var(--color-primary-soft); color: var(--color-primary); }

    /* List layout: photo on the left, details on the right */
    :host(.is-list) .item-card { flex-direction: row; }
    :host(.is-list) .image-container { width: 112px; flex-shrink: 0; aspect-ratio: auto; }
    :host(.is-list) .content-container { padding: 12px 14px; }
    :host(.is-list) .card-title { font-size: 15px; line-height: 20px; }
    :host(.is-list) .new-badge { display: none; }

    /* Compact density: smaller cards, fewer details */
    :host(.is-compact) .content-container { padding: 8px; gap: 4px; }
    :host(.is-compact) .card-title { font-size: 12px; line-height: 16px; }
    :host(.is-compact) .tags-row, :host(.is-compact) .new-badge { display: none; }
    :host(.is-compact) .pill-price-free, :host(.is-compact) .pill-price-paid { padding: 2px 7px; font-size: 10px; }
    :host(.is-compact.is-list) .image-container { width: 72px; }

    @media (prefers-reduced-motion: reduce) {
      .item-card, .card-img { transition: none; }
      .item-card:hover, .item-card:hover .card-img { transform: none; }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ItemCardComponent {
  @Input({ required: true }) post!: PostSummary;
  @Input() layout: FeedLayout = 'grid';
  @Input() compact = false;

  get categoryIcon(): string {
    return CATEGORY_ICONS[this.post.category] ?? 'category';
  }

  get isNew(): boolean {
    const created = Date.parse(this.post.createdAt);
    return !Number.isNaN(created) && Date.now() - created < DAY_MS;
  }

  get ariaLabel(): string {
    const price = this.post.isFree ? 'free' : `$${this.post.price}`;
    return `${this.post.title}, ${price}, pickup at ${this.post.pickupLocation}`;
  }

  formatCondition(cond: string): string {
    switch (cond) {
      case 'LIKE_NEW': return 'Like new';
      case 'GOOD': return 'Good';
      case 'FAIR': return 'Fair';
      default: return cond;
    }
  }

  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200" viewBox="0 0 200 200"><rect width="200" height="200" fill="%23f0ede8"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="%238a9099" font-family="sans-serif" font-size="14">Campus Item</text></svg>';
  }
}
