import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PostService } from '../../core/services/post.service';
import { CATEGORIES, Category, PostSummary } from '../../core/models/post.model';
import { ItemCardComponent } from '../../shared/components/item-card/item-card.component';
import { BottomNavComponent } from '../../shared/components/bottom-nav/bottom-nav.component';
import { HeaderComponent } from '../../shared/components/header/header.component';
import { AuthService } from '../../core/services/auth.service';
import { FeedSort, ViewPreferencesService } from '../../core/services/view-preferences.service';

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [RouterLink, FormsModule, ItemCardComponent, BottomNavComponent, HeaderComponent],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FeedComponent implements OnInit {
  protected readonly postService = inject(PostService);
  readonly authService = inject(AuthService);
  readonly view = inject(ViewPreferencesService);
  readonly viewPanelOpen = signal(false);

  readonly categories = CATEGORIES;
  readonly activeCategory = signal<Category | null>(null);
  readonly freeOnly = signal<boolean>(false);
  readonly searchQuery = signal<string>('');

  /** The feed in the order the student picked. */
  readonly sortedPosts = computed(() => {
    const posts = [...this.postService.posts()];
    switch (this.view.sort()) {
      case 'free-first':
        return posts.sort((a, b) => Number(b.isFree) - Number(a.isFree));
      case 'price-low':
        return posts.sort((a, b) => a.price - b.price);
      default:
        return posts.sort((a, b) => (b.createdAt ?? '').localeCompare(a.createdAt ?? ''));
    }
  });

  readonly freeCount = computed(() => this.postService.posts().filter(p => p.isFree).length);

  setSort(value: string): void {
    this.view.sort.set(value as FeedSort);
  }

  ngOnInit(): void {
    this.refreshFeed();
  }

  onCategorySelect(category: Category | null): void {
    this.activeCategory.set(category);
    this.refreshFeed();
  }

  toggleFreeOnly(): void {
    this.freeOnly.update(v => !v);
    this.refreshFeed();
  }

  onSearchChange(query: string): void {
    this.searchQuery.set(query);
    this.refreshFeed();
  }

  clearSearch(): void {
    this.searchQuery.set('');
    this.refreshFeed();
  }

  resetFilters(): void {
    this.activeCategory.set(null);
    this.freeOnly.set(false);
    this.searchQuery.set('');
    this.refreshFeed();
  }

  private refreshFeed(): void {
    this.postService.selectedCategory.set(this.activeCategory());
    this.postService.freeOnly.set(this.freeOnly());
    this.postService.searchQuery.set(this.searchQuery());
    this.postService.loadFeed();
  }
}
