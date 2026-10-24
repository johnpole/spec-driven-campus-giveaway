import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  template: `
    <header class="app-header" role="banner">
      <div class="header-inner">
        <a routerLink="/" class="brand-link" aria-label="Campus Giveaway Home">
          <div class="brand-badge">RPI</div>
          <div class="brand-text">
            <h1 class="brand-title">Campus Giveaway</h1>
            <p class="brand-subtitle">Pass it on · Reduce waste</p>
          </div>
        </a>

        <div class="auth-section">
          @if (authService.isAuthenticated(); as isAuth) {
            <div class="user-pill" [attr.aria-label]="'Logged in as ' + (authService.currentUser()?.name || 'User')">
              @if (authService.currentUser()?.pictureUrl; as pic) {
                <img [src]="pic" [alt]="authService.currentUser()?.name || 'Avatar'" class="user-avatar" (error)="onAvatarError()" />
              } @else {
                <div class="avatar-placeholder">
                  {{ userInitial() }}
                </div>
              }
              <span class="user-name">{{ userFirstName() }}</span>
              @if (authService.currentUser()?.isAdmin) {
                <span class="admin-pill-badge">ADMIN</span>
              }
              <button
                type="button"
                class="sign-out-btn"
                (click)="signOut()"
                aria-label="Sign out">
                <span class="material-symbols-outlined icon-small">logout</span>
              </button>
            </div>
          } @else {
            <div class="auth-actions">
              <button
                type="button"
                class="sign-in-btn"
                (click)="openSignInModal()"
                id="header-sign-in-btn"
                aria-label="Sign in">
                <span class="material-symbols-outlined icon-small">account_circle</span>
                <span class="btn-text">Sign In</span>
              </button>
            </div>
          }
        </div>
      </div>
    </header>
  `,
  styles: [`
    .app-header {
      background: var(--color-surface, #ffffff);
      border-bottom: 1px solid var(--color-border-hairline, #e5e7eb);
      position: sticky;
      top: 0;
      z-index: 40;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
    }

    .header-inner {
      max-width: var(--app-max-width, 480px);
      margin: 0 auto;
      padding: 10px 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
    }

    .brand-link {
      display: flex;
      align-items: center;
      gap: 10px;
      text-decoration: none;
      color: inherit;
    }

    .brand-badge {
      background: #e11d48;
      color: #ffffff;
      font-weight: 800;
      font-size: 13px;
      padding: 4px 7px;
      border-radius: 6px;
      letter-spacing: 0.5px;
      line-height: 1;
    }

    .brand-title {
      font-size: 16px;
      font-weight: 700;
      margin: 0;
      color: #111827;
      line-height: 1.2;
    }

    .brand-subtitle {
      font-size: 11px;
      color: #6b7280;
      margin: 0;
    }

    .auth-section {
      display: flex;
      align-items: center;
    }

    .auth-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .sign-in-btn {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      background: #e11d48;
      border: 1px solid #be123c;
      color: #ffffff;
      padding: 6px 14px;
      border-radius: 20px;
      font-size: 13px;
      font-weight: 600;
      cursor: pointer;
      min-height: 38px;
      transition: all 0.15s ease;

      &:hover {
        background: #be123c;
      }

      &:active {
        transform: scale(0.98);
      }

      .icon-small {
        font-size: 18px;
      }
    }


    .user-pill {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: #f3f4f6;
      padding: 4px 6px 4px 8px;
      border-radius: 24px;
      font-size: 13px;
      color: #1f2937;
      border: 1px solid #e5e7eb;
    }

    .admin-pill-badge {
      background: #f59e0b;
      color: #ffffff;
      font-size: 10px;
      font-weight: 800;
      letter-spacing: 0.5px;
      padding: 2px 6px;
      border-radius: 4px;
      line-height: 1;
      text-transform: uppercase;
    }

    .user-avatar {
      width: 28px;
      height: 28px;
      border-radius: 50%;
      object-fit: cover;
    }

    .avatar-placeholder {
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: #e11d48;
      color: #ffffff;
      font-size: 12px;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      text-transform: uppercase;
    }

    .user-name {
      font-weight: 600;
      max-width: 100px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .sign-out-btn {
      background: transparent;
      border: none;
      color: #6b7280;
      cursor: pointer;
      padding: 2px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;

      &:hover {
        background: #e5e7eb;
        color: #111827;
      }

      .icon-small {
        font-size: 18px;
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HeaderComponent {
  readonly authService = inject(AuthService);
  private readonly avatarLoadFailed = signal<boolean>(false);

  userFirstName(): string {
    const user = this.authService.currentUser();
    if (!user) return '';
    return user.name.split(' ')[0] || user.name;
  }

  userInitial(): string {
    const user = this.authService.currentUser();
    if (!user || !user.name) return 'U';
    return user.name.charAt(0).toUpperCase();
  }

  onAvatarError(): void {
    this.avatarLoadFailed.set(true);
  }

  openSignInModal(): void {
    this.authService.openAuthModal();
  }


  signOut(): void {
    this.authService.signOut();
  }
}
