import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HeaderComponent } from './header.component';
import { AuthService } from '../../../core/services/auth.service';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

const b64 = (o: object) => btoa(JSON.stringify(o)).replace(/=+$/, '').replace(/\+/g, '-').replace(/\//g, '_');

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let authService: AuthService;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [
        AuthService,
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should render brand logo and title', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.brand-title')?.textContent?.trim()).toBe('Campus Giveaway');
    expect(el.querySelector('.brand-badge')?.textContent?.trim()).toBe('RPI');
  });

  it('should show Sign In button when unauthenticated', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.sign-in-btn')).toBeTruthy();
    expect(el.querySelector('.user-pill')).toBeNull();
  });

  it('should display user pill with name and avatar when authenticated', () => {
    authService.setCredential('test-token:123:jane@rpi.edu:Jane Doe');
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.sign-in-btn')).toBeNull();
    const userPill = el.querySelector('.user-pill');
    expect(userPill).toBeTruthy();
    expect(el.querySelector('.user-name')?.textContent?.trim()).toBe('Jane');
    expect(el.querySelector('.avatar-placeholder')?.textContent?.trim()).toBe('J');
  });

  it('should display ADMIN badge when logged in as admin', () => {
    authService.setCredential(`${b64({ alg: 'HS256' })}.${b64({ iss: 'campus-giveaway', sub: 'admin-id', email: 'admin@campus-giveaway.test', name: 'John Pole', admin: true, exp: Math.floor(Date.now() / 1000) + 3600 })}.sig`);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.admin-pill-badge')?.textContent?.trim()).toBe('ADMIN');
  });

  it('should open auth modal when Sign In button is clicked', () => {
    spyOn(authService, 'openAuthModal').and.callThrough();
    const el: HTMLElement = fixture.nativeElement;
    const signInBtn = el.querySelector('.sign-in-btn') as HTMLButtonElement;
    expect(signInBtn).toBeTruthy();

    signInBtn.click();
    fixture.detectChanges();

    expect(authService.openAuthModal).toHaveBeenCalled();
    expect(authService.authModalOpen()).toBeTrue();
  });

  it('should not offer a demo login button', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.dev-demo-btn')).toBeNull();
  });

  it('should sign out and display sign in button when sign out icon is clicked', () => {
    authService.setCredential('test-token:456:alex@rpi.edu:Alex');
    fixture.detectChanges();

    const signOutBtn = fixture.nativeElement.querySelector('.sign-out-btn') as HTMLButtonElement;
    expect(signOutBtn).toBeTruthy();

    signOutBtn.click();
    fixture.detectChanges();

    expect(authService.isAuthenticated()).toBeFalse();
    expect(fixture.nativeElement.querySelector('.sign-in-btn')).toBeTruthy();
  });
});
