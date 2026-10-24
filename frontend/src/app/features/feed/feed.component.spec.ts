import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FeedComponent } from './feed.component';
import { PostService } from '../../core/services/post.service';
import { PostSummary } from '../../core/models/post.model';

describe('FeedComponent', () => {
  let component: FeedComponent;
  let fixture: ComponentFixture<FeedComponent>;
  let postService: PostService;

  const mockPosts: PostSummary[] = [
    {
      id: 'post-1',
      title: 'IKEA Micke Desk',
      category: 'Furniture',
      condition: 'GOOD',
      price: 0,
      isFree: true,
      pickupLocation: 'Cary Hall 204',
      availableUntil: '2026-11-01T00:00:00Z',
      photoUrl: '/api/media/desk.jpg',
      interestCount: 2,
      createdAt: '2026-10-23T00:00:00Z',
      status: 'AVAILABLE'
    },
    {
      id: 'post-2',
      title: 'Gaming Monitor',
      category: 'Electronics',
      condition: 'LIKE_NEW',
      price: 65,
      isFree: false,
      pickupLocation: 'BARH Lobby',
      availableUntil: '2026-11-01T00:00:00Z',
      photoUrl: '/api/media/monitor.jpg',
      interestCount: 0,
      createdAt: '2026-10-23T00:00:00Z',
      status: 'AVAILABLE'
    }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeedComponent],
      providers: [
        PostService,
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    postService = TestBed.inject(PostService);
    spyOn(postService, 'loadFeed').and.callFake(() => {});

    fixture = TestBed.createComponent(FeedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and invoke initial loadFeed', () => {
    expect(component).toBeTruthy();
    expect(postService.loadFeed).toHaveBeenCalled();
  });

  it('should render the brand header and search input', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.brand-title')?.textContent).toContain('Campus Giveaway');
    expect(compiled.querySelector('.search-input')).toBeTruthy();
    expect(compiled.querySelector('.impact-banner')).toBeTruthy();
  });

  it('should render category filter chips including "All" and "Free only"', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const chips = compiled.querySelectorAll('.cat-chip');
    expect(chips.length).toBeGreaterThan(5);
    expect(chips[0].textContent).toContain('All');
    expect(chips[1].textContent).toContain('Free only');
  });

  it('should render 2-column items grid when posts are populated', () => {
    postService.posts.set(mockPosts);
    postService.loading.set(false);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const cards = compiled.querySelectorAll('app-item-card');
    expect(cards.length).toBe(2);
    expect(compiled.querySelector('.items-grid')).toBeTruthy();
  });

  it('should render empty state when postService returns 0 items', () => {
    postService.posts.set([]);
    postService.loading.set(false);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.empty-state')).toBeTruthy();
    expect(compiled.querySelector('.empty-title')?.textContent).toContain('No items found');
  });

  it('should update activeCategory signal on chip click', () => {
    component.onCategorySelect('Furniture');
    expect(component.activeCategory()).toBe('Furniture');
    expect(postService.loadFeed).toHaveBeenCalled();
  });

  it('should toggle freeOnly signal on free-chip click', () => {
    expect(component.freeOnly()).toBeFalse();
    component.toggleFreeOnly();
    expect(component.freeOnly()).toBeTrue();
    expect(postService.loadFeed).toHaveBeenCalled();
  });

  it('should render the Post Item FAB and bottom navigation', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.fab-post-btn')).toBeTruthy();
    expect(compiled.querySelector('app-bottom-nav')).toBeTruthy();
  });
});
