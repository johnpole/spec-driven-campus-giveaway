import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { ItemDetailComponent } from './item-detail.component';
import { PostService } from '../../core/services/post.service';
import { InterestService } from '../../core/services/interest.service';
import { PostDetail } from '../../core/models/post.model';
import { InterestSummary } from '../../core/models/interest.model';

describe('ItemDetailComponent', () => {
  let component: ItemDetailComponent;
  let fixture: ComponentFixture<ItemDetailComponent>;
  let postService: PostService;
  let interestService: InterestService;

  const mockPost: PostDetail = {
    id: 'desk-123',
    title: 'IKEA Micke Desk',
    description: 'Clean study desk in Cary Hall',
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

  const mockSummary: InterestSummary = {
    count: 1,
    firstNames: ['Jordan']
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ItemDetailComponent],
      providers: [
        PostService,
        InterestService,
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => (key === 'id' ? 'desk-123' : null)
              }
            }
          }
        }
      ]
    }).compileComponents();

    postService = TestBed.inject(PostService);
    interestService = TestBed.inject(InterestService);

    spyOn(postService, 'getPost').and.returnValue(of(mockPost));
    spyOn(interestService, 'getSummary').and.returnValue(of(mockSummary));

    fixture = TestBed.createComponent(ItemDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load post detail and interest summary', () => {
    expect(component).toBeTruthy();
    expect(postService.getPost).toHaveBeenCalledWith('desk-123');
    expect(interestService.getSummary).toHaveBeenCalledWith('desk-123');
    expect(component.post()?.title).toBe('IKEA Micke Desk');
  });

  it('should render post metadata, price pill, and poster name', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.item-title')?.textContent).toContain('IKEA Micke Desk');
    expect(compiled.querySelector('.pill-price-free')?.textContent).toContain('FREE');
    expect(compiled.querySelector('.seller-name')?.textContent).toContain('Alex Rivera');
    expect(compiled.querySelector('.description-text')?.textContent).toContain('Clean study desk in Cary Hall');
  });

  it('should open the bottom sheet modal when clicking "I\'m interested"', () => {
    expect(component.sheetOpen()).toBeFalse();
    const btn = fixture.nativeElement.querySelector('#open-interest-modal') as HTMLButtonElement;
    btn.click();
    fixture.detectChanges();

    expect(component.sheetOpen()).toBeTrue();
  });

  it('should submit interest inquiry successfully and show toast', () => {
    const newSummary: InterestSummary = {
      count: 2,
      firstNames: ['Jordan', 'Taylor']
    };
    spyOn(interestService, 'createInterest').and.returnValue(of(newSummary));

    component.openInterestSheet();
    component.interestForm.setValue({
      name: 'Taylor Smith',
      contact: 'smitht@rpi.edu',
      message: 'Can I pick this up today?'
    });

    component.submitInterest();
    fixture.detectChanges();

    expect(interestService.createInterest).toHaveBeenCalledWith('desk-123', {
      name: 'Taylor Smith',
      contact: 'smitht@rpi.edu',
      message: 'Can I pick this up today?'
    });
    expect(component.sheetOpen()).toBeFalse();
    expect(component.toastMessage()).toContain('Note sent to Alex Rivera!');
  });

  it('should display conflict error in bottom sheet if item is claimed (409)', () => {
    spyOn(interestService, 'createInterest').and.returnValue(
      throwError(() => ({ status: 409, error: { message: 'Item already claimed' } }))
    );

    component.openInterestSheet();
    component.interestForm.setValue({
      name: 'Taylor Smith',
      contact: 'smitht@rpi.edu',
      message: 'Interested!'
    });

    component.submitInterest();
    fixture.detectChanges();

    expect(component.sheetError()).toContain('claimed or has expired');
    expect(component.sheetOpen()).toBeTrue();
  });

  it('should disable CTA button when item is claimed', () => {
    const claimedPost: PostDetail = { ...mockPost, status: 'CLAIMED' };
    component.post.set(claimedPost);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const claimedBtn = compiled.querySelector('.btn-claimed') as HTMLButtonElement;
    expect(claimedBtn).toBeTruthy();
    expect(claimedBtn.disabled).toBeTrue();
    expect(claimedBtn.textContent).toContain('Claimed');
  });
});
