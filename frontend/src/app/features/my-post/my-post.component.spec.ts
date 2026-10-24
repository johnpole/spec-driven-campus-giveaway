import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { MyPostComponent } from './my-post.component';
import { PostService } from '../../core/services/post.service';
import { InterestService } from '../../core/services/interest.service';
import { PostDetail } from '../../core/models/post.model';
import { InterestDetail } from '../../core/models/interest.model';

describe('MyPostComponent', () => {
  let component: MyPostComponent;
  let fixture: ComponentFixture<MyPostComponent>;
  let postService: PostService;
  let interestService: InterestService;

  const mockPost: PostDetail = {
    id: 'my-item-1',
    title: 'Study Desk',
    description: 'Clean IKEA desk',
    category: 'Furniture',
    condition: 'GOOD',
    price: 0,
    isFree: true,
    pickupLocation: 'Cary Hall 204',
    availableUntil: '2026-11-01T00:00:00Z',
    photoUrl: '/api/media/desk.jpg',
    status: 'AVAILABLE',
    posterName: 'Alex Rivera',
    interestCount: 1,
    createdAt: '2026-10-23T00:00:00Z'
  };

  const mockInquiries: InterestDetail[] = [
    {
      id: 'inq-1',
      name: 'Jordan Chen',
      contact: 'chenj@rpi.edu',
      message: 'Can I pick this up tonight around 7?',
      createdAt: '2026-10-23T01:30:00Z'
    }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyPostComponent],
      providers: [
        PostService,
        InterestService,
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({ id: 'my-item-1', token: 'valid-edit-token' })
          }
        }
      ]
    }).compileComponents();

    postService = TestBed.inject(PostService);
    interestService = TestBed.inject(InterestService);

    spyOn(postService, 'getPost').and.returnValue(of(mockPost));
    spyOn(interestService, 'getInterests').and.returnValue(of(mockInquiries));

    fixture = TestBed.createComponent(MyPostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load post details and inquiries with edit token', () => {
    expect(component).toBeTruthy();
    expect(postService.getPost).toHaveBeenCalledWith('my-item-1');
    expect(interestService.getInterests).toHaveBeenCalledWith('my-item-1', 'valid-edit-token');
    expect(component.post()?.title).toBe('Study Desk');
    expect(component.inquiries().length).toBe(1);
  });

  it('should render inquiry details including visitor name, contact link, and note', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.visitor-name')?.textContent).toContain('Jordan Chen');
    expect(compiled.querySelector('.contact-link')?.getAttribute('href')).toBe('mailto:chenj@rpi.edu');
    expect(compiled.querySelector('.inquiry-message')?.textContent).toContain('Can I pick this up tonight');
  });

  it('should toggle post status to CLAIMED via postService', () => {
    const updatedPost: PostDetail = { ...mockPost, status: 'CLAIMED' };
    spyOn(postService, 'updateStatus').and.returnValue(of(updatedPost));

    const toggleBtn = fixture.nativeElement.querySelector('#toggle-claimed-btn') as HTMLButtonElement;
    toggleBtn.click();
    fixture.detectChanges();

    expect(postService.updateStatus).toHaveBeenCalledWith('my-item-1', 'CLAIMED', 'valid-edit-token');
    expect(component.post()?.status).toBe('CLAIMED');
    expect(component.actionNotice()).toContain('marked as claimed');
  });

  it('should prompt confirmation and delete post via postService', () => {
    spyOn(postService, 'deletePost').and.returnValue(of(undefined));

    const deleteBtn = fixture.nativeElement.querySelector('#delete-post-btn') as HTMLButtonElement;
    deleteBtn.click();
    fixture.detectChanges();

    expect(component.showDeleteConfirm()).toBeTrue();

    const confirmBtn = fixture.nativeElement.querySelector('#confirm-delete-btn') as HTMLButtonElement;
    confirmBtn.click();
    fixture.detectChanges();

    expect(postService.deletePost).toHaveBeenCalledWith('my-item-1', 'valid-edit-token');
    expect(component.post()).toBeNull();
    expect(component.actionNotice()).toContain('permanently deleted');
  });

  it('should display error message when token is rejected (403)', () => {
    interestService.getInterests = jasmine.createSpy().and.returnValue(
      throwError(() => ({ status: 403, error: { message: 'Invalid token' } }))
    );

    component.loadPostAndInquiries('my-item-1', 'bad-token');
    fixture.detectChanges();

    expect(component.error()).toContain('Invalid edit token');
  });
});
