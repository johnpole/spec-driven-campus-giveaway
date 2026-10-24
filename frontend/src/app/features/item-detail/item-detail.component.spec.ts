import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { ItemDetailComponent } from './item-detail.component';
import { PostService } from '../../core/services/post.service';
import { AuthService } from '../../core/services/auth.service';
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
    localStorage.clear();
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

  function signIn(): void {
    TestBed.inject(AuthService).setCredential('test-token:u-7:taylor@rpi.edu:Taylor Smith');
    fixture.detectChanges();
  }

  it('asks visitors to sign in instead of opening the request form', () => {
    const btn = fixture.nativeElement.querySelector('#open-interest-modal') as HTMLButtonElement;
    expect(btn.textContent).toContain('Sign in to request pickup');
    btn.click();
    expect(component.sheetOpen()).toBeFalse();
    expect(TestBed.inject(AuthService).authModalOpen()).toBeTrue();
  });

  it('prefills name, email and pickup place for signed-in students', () => {
    signIn();
    component.openInterestSheet();
    expect(component.sheetOpen()).toBeTrue();
    expect(component.interestForm.value.name).toBe('Taylor Smith');
    expect(component.interestForm.value.contact).toBe('taylor@rpi.edu');
    expect(component.interestForm.value.pickupPlace).toBe(mockPost.pickupLocation);
    expect(component.interestForm.value.pickupAt).toBeTruthy();
  });

  it('sends a pickup request with time and place, then shows "Request already sent"', () => {
    signIn();
    spyOn(interestService, 'createInterest').and.returnValue(of({ count: 2, firstNames: ['Jordan', 'Taylor'] }));
    component.openInterestSheet();
    component.interestForm.patchValue({ pickupAt: '2030-05-01T17:00', pickupPlace: 'Union lobby', message: 'Can I pick this up?' });

    component.submitInterest();
    fixture.detectChanges();

    const req = (interestService.createInterest as jasmine.Spy).calls.mostRecent().args[1];
    expect(req.name).toBe('Taylor Smith');
    expect(req.pickupPlace).toBe('Union lobby');
    expect(new Date(req.pickupAt).getTime()).toBe(new Date('2030-05-01T17:00').getTime());
    expect(component.sheetOpen()).toBeFalse();
    expect(fixture.nativeElement.querySelector('#request-already-sent')?.textContent).toContain('Request already sent');
    expect(fixture.nativeElement.querySelector('#open-interest-modal')).toBeNull();
  });

  it('shows the owner a link to their requests instead of "I\'m interested"', () => {
    component.post.set({ ...mockPost, ownedByViewer: true });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('#open-interest-modal')).toBeNull();
    expect(fixture.nativeElement.querySelector('#manage-own-listing')).toBeTruthy();
  });

  it('shows a conflict error in the sheet if the item was just claimed (409)', () => {
    signIn();
    spyOn(interestService, 'createInterest').and.returnValue(
      throwError(() => ({ status: 409, error: { error: 'Item is already claimed and no longer available' } }))
    );
    component.openInterestSheet();
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
