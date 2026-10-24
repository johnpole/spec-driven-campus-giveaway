import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthResponse, UserProfile } from '../models/user.model';

declare const google: any;

const TOKEN_STORAGE_KEY = 'campus_giveaway_token';
const CLIENT_ID_STORAGE_KEY = 'campus_giveaway_client_id';
const DEFAULT_GOOGLE_CLIENT_ID = '775547836512-89bj4b05013a1l37sctmi7ech0ike9gj.apps.googleusercontent.com';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly _currentUser = signal<UserProfile | null>(null);
  private readonly _idToken = signal<string | null>(null);
  readonly authModalOpen = signal<boolean>(false);

  readonly currentUser = this._currentUser.asReadonly();
  readonly idToken = this._idToken.asReadonly();
  readonly isAuthenticated = computed(() => this._currentUser() !== null);
  readonly isAdmin = computed(() => !!this._currentUser()?.isAdmin);

  constructor() {
    this.restoreSession();
  }

  openAuthModal(): void {
    this.authModalOpen.set(true);
  }

  closeAuthModal(): void {
    this.authModalOpen.set(false);
  }

  getClientId(): string {
    if (typeof window !== 'undefined' && window.localStorage) {
      const customId = localStorage.getItem(CLIENT_ID_STORAGE_KEY);
      if (customId && customId.trim()) return customId.trim();
    }
    return DEFAULT_GOOGLE_CLIENT_ID;
  }

  setClientId(clientId: string): void {
    if (typeof window !== 'undefined' && window.localStorage) {
      if (clientId && clientId.trim()) {
        localStorage.setItem(CLIENT_ID_STORAGE_KEY, clientId.trim());
      } else {
        localStorage.removeItem(CLIENT_ID_STORAGE_KEY);
      }
    }
  }

  /**
   * Restores existing authentication from localStorage or OAuth URL redirect hash.
   */
  private restoreSession(): void {
    if (typeof window === 'undefined') {
      return;
    }

    // Check if returning from Google OAuth redirect with #id_token=...
    if (window.location && window.location.hash) {
      const hashParams = new URLSearchParams(window.location.hash.substring(1));
      const idTokenFromHash = hashParams.get('id_token');
      if (idTokenFromHash && this.setCredential(idTokenFromHash)) {
        window.history.replaceState(null, '', window.location.pathname);
        return;
      }
    }

    if (window.localStorage) {
      try {
        const storedToken = localStorage.getItem(TOKEN_STORAGE_KEY);
        if (storedToken) {
          this.setCredential(storedToken);
        }
      } catch {
        this.clearSession();
      }
    }
  }

  /**
   * Register with simple credentials: first name, last name, email, phone, simple password.
   */
  register(payload: {
    firstName: string;
    lastName?: string;
    email: string;
    phoneNumber?: string;
    password: string;
  }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/register', payload).pipe(
      tap((res) => {
        if (res && res.token) {
          this.setCredential(res.token);
          this.closeAuthModal();
        }
      })
    );
  }

  /**
   * Sign in with username (email or phone) and password.
   */
  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', { username, password }).pipe(
      tap((res) => {
        if (res && res.token) {
          this.setCredential(res.token);
          this.closeAuthModal();
        }
      })
    );
  }

  /**
   * Sets the active credential token (Google JWT ID Token or compact session token).
   */
  setCredential(token: string): boolean {
    const user = this.parseToken(token);
    if (!user) {
      return false;
    }

    this._idToken.set(token);
    this._currentUser.set(user);
    if (this.isGoogleToken(token)) {
      this.refreshProfile();
    }

    if (typeof window !== 'undefined' && window.localStorage) {
      try {
        localStorage.setItem(TOKEN_STORAGE_KEY, token);
      } catch {
        // Ignore localStorage write failure in restricted iframe/browser
      }
    }
    return true;
  }

  /**
   * Signs out the user, clearing memory signals and persistent storage.
   */
  signOut(): void {
    this.clearSession();
    if (typeof google !== 'undefined' && google.accounts?.id?.disableAutoSelect) {
      google.accounts.id.disableAutoSelect();
    }
  }

  /**
   * Triggers the Google Sign-in flow.
   */
  signInWithGoogle(): void {
    if (typeof window === 'undefined') return;

    if (typeof google !== 'undefined' && google.accounts?.id) {
      google.accounts.id.initialize({
        client_id: this.getClientId(),
        callback: (response: { credential: string }) => {
          if (response && response.credential) {
            this.setCredential(response.credential);
            this.closeAuthModal();
          }
        },
        auto_select: false,
        cancel_on_tap_outside: true
      });

      // Trigger prompt popup
      google.accounts.id.prompt((notification: any) => {
        if (notification.isNotDisplayed() || notification.isSkippedMoment()) {
          console.warn('GIS One Tap prompt not displayed, redirecting to Google OAuth page:', notification);
          this.redirectToGoogleOAuth();
        }
      });
    } else {
      // SDK not yet loaded or blocked: redirect to Google OAuth login page
      this.redirectToGoogleOAuth();
    }
  }

  /**
   * Redirects browser to Google's standard OAuth2 sign-in endpoint (accounts.google.com).
   */
  redirectToGoogleOAuth(): void {
    if (typeof window === 'undefined' || !window.location) return;

    const clientId = this.getClientId();
    const redirectUri = window.location.origin;
    const scope = encodeURIComponent('openid profile email');
    const nonce = 'cg_' + Date.now();
    const authUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${encodeURIComponent(clientId)}&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=id_token&scope=${scope}&nonce=${nonce}`;

    window.location.href = authUrl;
  }

  /**
   * Initializes Google Identity Services (GIS) on a container element.
   */
  initGoogleSignIn(containerId: string, buttonText: 'signin_with' | 'signup_with' | 'continue_with' = 'signin_with'): void {
    if (typeof window === 'undefined') return;

    const checkAndInit = () => {
      if (typeof google !== 'undefined' && google.accounts?.id) {
        google.accounts.id.initialize({
          client_id: this.getClientId(),
          callback: (response: { credential: string }) => {
            if (response && response.credential) {
              this.setCredential(response.credential);
              this.closeAuthModal();
            }
          },
          auto_select: false,
          cancel_on_tap_outside: true
        });

        const el = document.getElementById(containerId);
        if (el) {
          google.accounts.id.renderButton(el, {
            theme: 'outline',
            size: 'medium',
            type: 'standard',
            shape: 'pill',
            text: buttonText,
            logo_alignment: 'left'
          });
        }
      }
    };

    checkAndInit();
    if (typeof google === 'undefined') {
      setTimeout(checkAndInit, 500);
      setTimeout(checkAndInit, 1500);
    }
  }

  private clearSession(): void {
    this._idToken.set(null);
    this._currentUser.set(null);
    if (typeof window !== 'undefined' && window.localStorage) {
      try {
        localStorage.removeItem(TOKEN_STORAGE_KEY);
      } catch {
        // Ignore storage error
      }
    }
  }

  private isGoogleToken(token: string): boolean {
    try {
      const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const iss = JSON.parse(atob(payload + '==='.slice((payload.length + 3) % 4))).iss;
      return typeof iss === 'string' && iss.includes('accounts.google.com');
    } catch {
      return false;
    }
  }

  /** Asks the server who we are, so the admin flag for Google sign-ins comes from the backend. */
  refreshProfile(): void {
    this.http.get<{ isAdmin?: boolean; admin?: boolean }>('/api/auth/me').subscribe({
      next: (me) => {
        const user = this._currentUser();
        if (user) {
          this._currentUser.set({ ...user, isAdmin: !!(me.isAdmin ?? me.admin) });
        }
      },
      error: () => {
        // A stale session is cleared by the auth interceptor.
      }
    });
  }

  /**
   * Parses either custom credential session token or Google JWT into UserProfile.
   */
  parseToken(token: string): UserProfile | null {
    if (!token || typeof token !== 'string') {
      return null;
    }

    // 1. Deterministic test token (unit tests only; the server rejects these outside tests): test-token:<id>:<email>:<name>
    if (token.startsWith('test-token:')) {
      const parts = token.split(':');
      if (parts.length >= 4) {
        const email = parts[2];
        return {
          id: parts[1],
          email: email,
          name: parts.slice(3).join(':'),
          pictureUrl: undefined,
          isAdmin: false
        };
      }
    }

    // 2. JWT: app session token (email/password) or Google ID token
    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        return null;
      }

      let payloadBase64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      while (payloadBase64.length % 4 !== 0) {
        payloadBase64 += '=';
      }

      const decodedJson = atob(payloadBase64);
      const claims = JSON.parse(decodedJson);

      const sub = claims.sub || claims.user_id || claims.id;
      const email = claims.email;
      if (!sub || !email) {
        return null;
      }

      if (claims.exp && claims.exp * 1000 < Date.now()) {
        return null; // Expired
      }

      return {
        id: String(sub),
        email: String(email),
        name: claims.name || claims.email?.split('@')[0] || 'User',
        pictureUrl: claims.picture,
        // App session tokens carry a server-signed admin claim; Google tokens are confirmed via /api/auth/me.
        isAdmin: claims.admin === true
      };
    } catch {
      return null;
    }
  }
}
