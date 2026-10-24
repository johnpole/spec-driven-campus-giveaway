import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AuthModalComponent } from './auth-modal.component';
import { AuthService } from '../../../core/services/auth.service';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';

describe('AuthModalComponent', () => {
  let component: AuthModalComponent;
  let fixture: ComponentFixture<AuthModalComponent>;
  let authService: AuthService;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [AuthModalComponent],
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AuthModalComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should start on Sign In tab with username and password fields', () => {
    expect(component.activeTab()).toBe('signin');
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('#signin-username')).toBeTruthy();
    expect(el.querySelector('#signin-password')).toBeTruthy();
  });

  it('should switch to Register tab and display registration fields', () => {
    component.setTab('register');
    fixture.detectChanges();

    expect(component.activeTab()).toBe('register');
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('#reg-first-name')).toBeTruthy();
    expect(el.querySelector('#reg-email')).toBeTruthy();
    expect(el.querySelector('#reg-password')).toBeTruthy();
  });

  it('should call authService.login when submitting valid sign-in form', () => {
    spyOn(authService, 'login').and.returnValue(of({
      token: 'test-token',
      user: { id: 'u1', email: 'test@rpi.edu', name: 'Test' }
    }));

    component.signInForm.setValue({
      username: 'test@rpi.edu',
      password: 'password123'
    });

    component.onSubmitSignIn();
    expect(authService.login).toHaveBeenCalledWith('test@rpi.edu', 'password123');
  });

  it('should display error message on login failure', () => {
    spyOn(authService, 'login').and.returnValue(throwError(() => ({
      error: { message: 'Invalid username or password' }
    })));

    component.signInForm.setValue({
      username: 'test@rpi.edu',
      password: 'bad'
    });

    component.onSubmitSignIn();
    fixture.detectChanges();

    expect(component.errorMessage()).toBe('Invalid username or password');
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.error-banner')?.textContent).toContain('Invalid username or password');
  });

  it('should call authService.signInWithGoogle when Google button is clicked', () => {
    spyOn(authService, 'signInWithGoogle');
    component.signInWithGoogle();
    expect(authService.signInWithGoogle).toHaveBeenCalled();
  });
});
