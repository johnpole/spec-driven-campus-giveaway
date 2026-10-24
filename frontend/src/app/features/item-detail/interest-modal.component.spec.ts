import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { InterestModalComponent } from './interest-modal.component';
import { AuthService } from '../../core/services/auth.service';
import { InterestService } from '../../core/services/interest.service';
import { of, throwError } from 'rxjs';
import { ComponentRef } from '@angular/core';
import { PostDetail } from '../../core/models/post.model';

describe('InterestModalComponent', () => {
  let component: InterestModalComponent;
  let componentRef: ComponentRef<InterestModalComponent>;
  let fixture: ComponentFixture<InterestModalComponent>;
  let authService: AuthService;
  let interestService: jasmine.SpyObj<InterestService>;

  const mockPost: PostDetail = {
    id: 'post-100',
    title: 'Comfortable Desk Chair',
    description: 'Black mesh ergonomic chair',
    category: 'Furniture',
    condition: 'GOOD',
    price: 0,
    isFree: true,
    pickupLocation: 'Barton Hall',
    availableUntil: '2026-10-30T12:00:00Z',
    photoUrl: 'https://example.com/chair.jpg',
    status: 'AVAILABLE',
    posterName: 'Alex Rivera',
    interestCount: 2,
    createdAt: '2026-10-23T10:00:00Z'
  };

  beforeEach(async () => {
    localStorage.clear();
    const interestSpy = jasmine.createSpyObj('InterestService', ['createInterest']);
    interestSpy.createInterest.and.returnValue(of({ count: 3, totalCount: 3, firstNames: ['Test'] }));

    await TestBed.configureTestingModule({
      imports: [InterestModalComponent],
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: InterestService, useValue: interestSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(InterestModalComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    authService = TestBed.inject(AuthService);
    interestService = TestBed.inject(InterestService) as jasmine.SpyObj<InterestService>;

    componentRef.setInput('isOpen', true);
    componentRef.setInput('post', mockPost);
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should render sign-in prompt when unauthenticated', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.modal-auth-prompt')).toBeTruthy();
    expect(el.querySelector('.verified-buyer-pill')).toBeNull();
    expect(el.querySelector('#pickup-note')).toBeNull();
  });

  it('should display verified pill and pickup note field when authenticated', () => {
    authService.setCredential('test-token:usr99:charlie@rpi.edu:Charlie Brown');
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.modal-auth-prompt')).toBeNull();
    expect(el.querySelector('.verified-buyer-pill')).toBeTruthy();
    expect(el.querySelector('.buyer-name')?.textContent).toContain('Charlie Brown');
    expect(el.querySelector('.buyer-email')?.textContent).toContain('charlie@rpi.edu');
    expect(el.querySelector('#pickup-note')).toBeTruthy();
  });

  it('should submit pickup note and emit submitted event', () => {
    authService.setCredential('test-token:usr99:charlie@rpi.edu:Charlie Brown');
    fixture.detectChanges();

    spyOn(component.submitted, 'emit');
    spyOn(component.closed, 'emit');

    component.interestForm.patchValue({ message: 'Can pick up tomorrow at 3pm!' });
    component.submitInterest();

    expect(interestService.createInterest).toHaveBeenCalledWith('post-100', {
      name: 'Charlie Brown',
      contact: 'charlie@rpi.edu',
      message: 'Can pick up tomorrow at 3pm!'
    });
    expect(component.submitted.emit).toHaveBeenCalled();
    expect(component.closed.emit).toHaveBeenCalled();
  });

  it('should display error banner if request fails', () => {
    authService.setCredential('test-token:usr99:charlie@rpi.edu:Charlie Brown');
    fixture.detectChanges();

    interestService.createInterest.and.returnValue(
      throwError(() => ({ status: 400, error: { message: 'You cannot request an item you posted yourself.' } }))
    );

    component.interestForm.patchValue({ message: 'Picking it up!' });
    component.submitInterest();
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(component.sheetError()).toContain('cannot request an item you posted yourself');
    expect(el.querySelector('.sheet-error-banner')?.textContent).toContain('cannot request an item you posted yourself');
  });
});
