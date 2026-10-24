import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-bottom-nav',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="bottom-nav" aria-label="Bottom navigation">
      <a
        routerLink="/"
        routerLinkActive="active"
        [routerLinkActiveOptions]="{ exact: true }"
        class="nav-tab"
        aria-label="Browse items">
        <span class="material-symbols-outlined nav-icon">explore</span>
        <span class="nav-label">Browse</span>
      </a>

      <a
        routerLink="/post"
        routerLinkActive="active"
        class="nav-tab nav-tab-post"
        aria-label="Post an item">
        <div class="post-icon-wrapper">
          <span class="material-symbols-outlined nav-icon-plus">add</span>
        </div>
        <span class="nav-label">Post</span>
      </a>

      <a
        routerLink="/my-posts"
        routerLinkActive="active"
        class="nav-tab"
        aria-label="My posted items">
        @if (authService.isAuthenticated(); as isAuth) {
          @if (authService.currentUser()?.pictureUrl; as pic) {
            <img [src]="pic" alt="Avatar" class="nav-avatar" />
          } @else {
            <div class="nav-avatar-initial">{{ userInitial() }}</div>
          }
        } @else {
          <span class="material-symbols-outlined nav-icon">inventory_2</span>
        }
        <span class="nav-label">My items</span>
      </a>
    </nav>
  `,
  styles: [`
    .bottom-nav {
      position: fixed;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 100%;
      max-width: var(--app-max-width, 480px);
      height: 64px;
      padding-bottom: env(safe-area-inset-bottom, 0px);
      background: rgba(255, 255, 255, 0.92);
      backdrop-filter: blur(16px);
      -webkit-backdrop-filter: blur(16px);
      border-top: 1px solid var(--color-border-hairline);
      display: flex;
      align-items: center;
      justify-content: space-around;
      z-index: 50;
      box-shadow: 0 -4px 20px -2px rgba(31, 35, 40, 0.06);
    }

    .nav-tab {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-width: 64px;
      min-height: 48px;
      color: var(--color-secondary-muted);
      text-decoration: none;
      transition: color 0.15s ease;

      &.active {
        color: var(--color-primary);

        .nav-label {
          font-weight: 700;
        }

        .nav-icon {
          font-variation-settings: 'FILL' 1;
        }

        .nav-avatar-initial {
          border-color: var(--color-primary);
        }
      }
    }

    .nav-icon {
      font-size: 24px;
    }

    .nav-avatar {
      width: 24px;
      height: 24px;
      border-radius: 50%;
      object-fit: cover;
      border: 1.5px solid transparent;
    }

    .nav-avatar-initial {
      width: 24px;
      height: 24px;
      border-radius: 50%;
      background: #e11d48;
      color: #ffffff;
      font-size: 11px;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .nav-label {
      font-family: var(--font-body);
      font-size: 11px;
      font-weight: 500;
      margin-top: 2px;
    }

    .post-icon-wrapper {
      width: 34px;
      height: 34px;
      border-radius: 12px;
      background-color: var(--color-primary);
      color: #FFFFFF;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 2px 8px rgba(214, 0, 28, 0.3);
      transition: transform 0.1s ease;

      &:active {
        transform: scale(0.95);
      }
    }

    .nav-icon-plus {
      font-size: 22px;
      font-weight: 700;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BottomNavComponent {
  readonly authService = inject(AuthService);

  userInitial(): string {
    const user = this.authService.currentUser();
    if (!user || !user.name) return 'U';
    return user.name.charAt(0).toUpperCase();
  }
}
