import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-auth-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="auth-modal-backdrop" (click)="close()">
      <div class="auth-modal-sheet" (click)="$event.stopPropagation()" role="dialog" aria-modal="true" aria-label="Sign in or Register">
        <div class="sheet-handle"></div>

        <div class="sheet-header">
          <div class="header-titles">
            <h2 class="auth-title">{{ activeTab() === 'signin' ? 'Sign In' : 'Create Account' }}</h2>
            <p class="auth-subtitle">
              {{ activeTab() === 'signin' ? 'Access your campus giveaways & inquiries' : 'Join the RPI Campus Giveaway community' }}
            </p>
          </div>
          <button type="button" class="close-btn" (click)="close()" aria-label="Close dialog">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>

        <!-- Segmented Tab Switcher -->
        <div class="tab-switcher" role="tablist">
          <button
            type="button"
            class="tab-btn"
            [class.active]="activeTab() === 'signin'"
            (click)="setTab('signin')"
            role="tab"
            [attr.aria-selected]="activeTab() === 'signin'">
            Sign In
          </button>
          <button
            type="button"
            class="tab-btn"
            [class.active]="activeTab() === 'register'"
            (click)="setTab('register')"
            role="tab"
            [attr.aria-selected]="activeTab() === 'register'">
            Register
          </button>
        </div>

        @if (errorMessage()) {
          <div class="error-banner" role="alert">
            <span class="material-symbols-outlined error-icon">error_outline</span>
            <span>{{ errorMessage() }}</span>
          </div>
        }

        <!-- SIGN IN FORM -->
        @if (activeTab() === 'signin') {
          <form [formGroup]="signInForm" (ngSubmit)="onSubmitSignIn()" class="auth-form" id="sign-in-form">
            <div class="form-group">
              <label for="signin-username" class="field-label">Email or Phone Number</label>
              <input
                id="signin-username"
                type="text"
                formControlName="username"
                class="field-input"
                placeholder="e.g. student@rpi.edu or 518-555-0199"
                autocomplete="username"
                required />
            </div>

            <div class="form-group">
              <label for="signin-password" class="field-label">Password</label>
              <input
                id="signin-password"
                type="password"
                formControlName="password"
                class="field-input"
                placeholder="Enter your password"
                autocomplete="current-password"
                required />
            </div>

            <button
              type="submit"
              class="primary-submit-btn"
              [disabled]="signInForm.invalid || loading()">
              @if (loading()) {
                <span class="btn-spinner"></span>
                <span>Signing in...</span>
              } @else {
                <span>Sign In</span>
              }
            </button>
          </form>
        }

        <!-- REGISTER FORM -->
        @if (activeTab() === 'register') {
          <form [formGroup]="registerForm" (ngSubmit)="onSubmitRegister()" class="auth-form" id="register-form">
            <div class="form-row-2">
              <div class="form-group">
                <label for="reg-first-name" class="field-label">First Name *</label>
                <input
                  id="reg-first-name"
                  type="text"
                  formControlName="firstName"
                  class="field-input"
                  placeholder="First name"
                  autocomplete="given-name"
                  required />
              </div>
              <div class="form-group">
                <label for="reg-last-name" class="field-label">Last Name</label>
                <input
                  id="reg-last-name"
                  type="text"
                  formControlName="lastName"
                  class="field-input"
                  placeholder="Last name"
                  autocomplete="family-name" />
              </div>
            </div>

            <div class="form-group">
              <label for="reg-email" class="field-label">Email Address *</label>
              <input
                id="reg-email"
                type="email"
                formControlName="email"
                class="field-input"
                placeholder="e.g. you@rpi.edu"
                autocomplete="email"
                required />
            </div>

            <div class="form-group">
              <label for="reg-phone" class="field-label">Phone Number (optional)</label>
              <input
                id="reg-phone"
                type="tel"
                formControlName="phoneNumber"
                class="field-input"
                placeholder="e.g. 518-276-6000"
                autocomplete="tel" />
            </div>

            <div class="form-group">
              <label for="reg-password" class="field-label">Password * (min 4 characters)</label>
              <input
                id="reg-password"
                type="password"
                formControlName="password"
                class="field-input"
                placeholder="Password"
                autocomplete="new-password"
                required />
            </div>

            <button
              type="submit"
              class="primary-submit-btn"
              [disabled]="registerForm.invalid || loading()">
              @if (loading()) {
                <span class="btn-spinner"></span>
                <span>Creating Account...</span>
              } @else {
                <span>Register Account</span>
              }
            </button>
          </form>
        }

        <!-- DIVIDER -->
        <div class="auth-divider">
          <span class="divider-line"></span>
          <span class="divider-text">OR</span>
          <span class="divider-line"></span>
        </div>

        <!-- GOOGLE SIGN IN BUTTON AT BOTTOM -->
        <button
          type="button"
          class="google-auth-btn"
          (click)="signInWithGoogle()"
          aria-label="Sign in with Google">
          <svg class="google-icon" viewBox="0 0 24 24" width="20" height="20">
            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
          </svg>
          <span>Sign in with Google</span>
        </button>

      </div>
    </div>
  `,
  styles: [`
    .auth-modal-backdrop {
      position: fixed;
      inset: 0;
      background: rgba(17, 24, 39, 0.6);
      backdrop-filter: blur(4px);
      z-index: 1000;
      display: flex;
      align-items: flex-end;
      justify-content: center;
      padding: 0;

      @media (min-width: 640px) {
        align-items: center;
        padding: 24px;
      }
    }

    .auth-modal-sheet {
      background: #ffffff;
      width: 100%;
      max-width: 440px;
      max-height: 90vh;
      overflow-y: auto;
      border-radius: 20px 20px 0 0;
      padding: 20px 24px 28px;
      box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.15);
      animation: slideUp 0.24s cubic-bezier(0.16, 1, 0.3, 1);

      @media (min-width: 640px) {
        border-radius: 20px;
        box-shadow: 0 12px 36px rgba(0, 0, 0, 0.2);
        animation: fadeIn 0.2s ease-out;
      }
    }

    @keyframes slideUp {
      from { transform: translateY(100%); }
      to { transform: translateY(0); }
    }

    @keyframes fadeIn {
      from { opacity: 0; transform: scale(0.96); }
      to { opacity: 1; transform: scale(1); }
    }

    .sheet-handle {
      width: 40px;
      height: 4px;
      background: #e5e7eb;
      border-radius: 2px;
      margin: 0 auto 16px;

      @media (min-width: 640px) {
        display: none;
      }
    }

    .sheet-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 18px;
    }

    .auth-title {
      font-size: 20px;
      font-weight: 700;
      color: #111827;
      margin: 0 0 4px;
    }

    .auth-subtitle {
      font-size: 13px;
      color: #6b7280;
      margin: 0;
    }

    .close-btn {
      background: transparent;
      border: none;
      color: #6b7280;
      cursor: pointer;
      padding: 4px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;

      &:hover {
        background: #f3f4f6;
        color: #111827;
      }
    }

    .tab-switcher {
      display: flex;
      background: #f3f4f6;
      border-radius: 10px;
      padding: 3px;
      margin-bottom: 20px;
    }

    .tab-btn {
      flex: 1;
      padding: 8px 16px;
      font-size: 14px;
      font-weight: 600;
      color: #4b5563;
      background: transparent;
      border: none;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.15s ease;
      min-height: 40px;

      &.active {
        background: #ffffff;
        color: #111827;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
      }
    }

    .error-banner {
      display: flex;
      align-items: center;
      gap: 8px;
      background: #fef2f2;
      border: 1px solid #fecaca;
      color: #dc2626;
      padding: 10px 14px;
      border-radius: 8px;
      font-size: 13px;
      margin-bottom: 16px;

      .error-icon {
        font-size: 18px;
      }
    }

    .auth-form {
      display: flex;
      flex-direction: column;
      gap: 14px;
    }

    .form-row-2 {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
    }

    .form-group {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    .field-label {
      font-size: 12px;
      font-weight: 600;
      color: #374151;
    }

    .field-input {
      padding: 10px 12px;
      border: 1px solid #d1d5db;
      border-radius: 8px;
      font-size: 14px;
      color: #111827;
      background: #ffffff;
      outline: none;
      transition: border-color 0.15s ease;
      min-height: 44px;

      &:focus {
        border-color: #e11d48;
        box-shadow: 0 0 0 3px rgba(225, 29, 72, 0.12);
      }
    }

    .primary-submit-btn {
      width: 100%;
      min-height: 48px;
      background: #e11d48;
      color: #ffffff;
      border: none;
      border-radius: 10px;
      font-size: 15px;
      font-weight: 600;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      transition: background 0.15s ease;
      margin-top: 4px;

      &:hover:not(:disabled) {
        background: #be123c;
      }

      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }
    }

    .auth-divider {
      display: flex;
      align-items: center;
      gap: 12px;
      margin: 18px 0;

      .divider-line {
        flex: 1;
        height: 1px;
        background: #e5e7eb;
      }

      .divider-text {
        font-size: 11px;
        font-weight: 700;
        color: #9ca3af;
        letter-spacing: 0.5px;
      }
    }

    .google-auth-btn {
      width: 100%;
      min-height: 48px;
      background: #ffffff;
      border: 1px solid #d1d5db;
      border-radius: 10px;
      color: #374151;
      font-size: 15px;
      font-weight: 600;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      transition: background 0.15s ease, border-color 0.15s ease;

      &:hover {
        background: #f9fafb;
        border-color: #9ca3af;
      }
    }


    .btn-spinner {
      width: 16px;
      height: 16px;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-top-color: #ffffff;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AuthModalComponent {
  readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly activeTab = signal<'signin' | 'register'>('signin');
  readonly loading = signal<boolean>(false);
  readonly errorMessage = signal<string | null>(null);

  readonly signInForm = this.fb.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]]
  });

  readonly registerForm = this.fb.group({
    firstName: ['', [Validators.required]],
    lastName: [''],
    email: ['', [Validators.required, Validators.email]],
    phoneNumber: [''],
    password: ['', [Validators.required, Validators.minLength(4)]]
  });

  setTab(tab: 'signin' | 'register'): void {
    this.activeTab.set(tab);
    this.errorMessage.set(null);
  }

  close(): void {
    this.authService.closeAuthModal();
  }

  onSubmitSignIn(): void {
    if (this.signInForm.invalid) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    const { username, password } = this.signInForm.value;
    this.authService.login(username!, password!).subscribe({
      next: () => {
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        const msg = err.error?.message || 'Invalid username or password';
        this.errorMessage.set(msg);
      }
    });
  }

  onSubmitRegister(): void {
    if (this.registerForm.invalid) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    const val = this.registerForm.value;
    this.authService.register({
      firstName: val.firstName!,
      lastName: val.lastName || undefined,
      email: val.email!,
      phoneNumber: val.phoneNumber || undefined,
      password: val.password!
    }).subscribe({
      next: () => {
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        const msg = err.error?.message || 'Registration failed. Email may already be in use.';
        this.errorMessage.set(msg);
      }
    });
  }

  signInWithGoogle(): void {
    this.authService.signInWithGoogle();
  }

}
