import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpClient } from '@angular/common/http';
import { authInterceptor } from '../interceptors/auth.interceptor';

/** Builds an app session token shaped like the backend's (signature is not checked client-side). */
function appToken(claims: Record<string, unknown>): string {
  const b64 = (o: object) => btoa(JSON.stringify(o)).replace(/=+$/, '').replace(/\+/g, '-').replace(/\//g, '_');
  const exp = Math.floor(Date.now() / 1000) + 3600;
  return `${b64({ alg: 'HS256', typ: 'JWT' })}.${b64({ iss: 'campus-giveaway', exp, ...claims })}.sig`;
}

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    localStorage.clear();
    httpMock.verify();
  });

  it('should initialize with null user and false isAuthenticated', () => {
    expect(service.currentUser()).toBeNull();
    expect(service.isAuthenticated()).toBeFalse();
    expect(service.idToken()).toBeNull();
  });

  it('should accept and parse deterministic test token', () => {
    const success = service.setCredential('test-token:usr123:student@rpi.edu:John Student');
    expect(success).toBeTrue();
    expect(service.isAuthenticated()).toBeTrue();

    const user = service.currentUser();
    expect(user).toBeTruthy();
    expect(user?.id).toBe('usr123');
    expect(user?.email).toBe('student@rpi.edu');
    expect(user?.name).toBe('John Student');
    expect(localStorage.getItem('campus_giveaway_token')).toBe('test-token:usr123:student@rpi.edu:John Student');
  });

  it('should accept and decode a valid mock JWT token', () => {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const futureExp = Math.floor(Date.now() / 1000) + 3600;
    const payload = btoa(JSON.stringify({
      sub: 'google-sub-456',
      email: 'alex@example.com',
      name: 'Alex Rivera',
      picture: 'https://example.com/avatar.jpg',
      exp: futureExp
    }));
    const mockJwt = `${header}.${payload}.sig`;

    const success = service.setCredential(mockJwt);
    expect(success).toBeTrue();
    expect(service.isAuthenticated()).toBeTrue();

    const user = service.currentUser();
    expect(user?.id).toBe('google-sub-456');
    expect(user?.email).toBe('alex@example.com');
    expect(user?.name).toBe('Alex Rivera');
    expect(user?.pictureUrl).toBe('https://example.com/avatar.jpg');
  });

  it('should reject expired JWT token', () => {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const pastExp = Math.floor(Date.now() / 1000) - 3600;
    const payload = btoa(JSON.stringify({
      sub: 'google-sub-789',
      email: 'expired@example.com',
      exp: pastExp
    }));
    const expiredJwt = `${header}.${payload}.sig`;

    const success = service.setCredential(expiredJwt);
    expect(success).toBeFalse();
    expect(service.isAuthenticated()).toBeFalse();
    expect(service.currentUser()).toBeNull();
  });

  it('should sign out and clear signals and localStorage', () => {
    service.setCredential('test-token:123:user@rpi.edu:User');
    expect(service.isAuthenticated()).toBeTrue();

    service.signOut();
    expect(service.currentUser()).toBeNull();
    expect(service.isAuthenticated()).toBeFalse();
    expect(service.idToken()).toBeNull();
    expect(localStorage.getItem('campus_giveaway_token')).toBeNull();
  });

  it('should inject Authorization Bearer header via authInterceptor for /api calls', () => {
    service.setCredential('test-token:999:auth@rpi.edu:Authenticated Student');

    http.get('/api/posts').subscribe();

    const req = httpMock.expectOne('/api/posts');
    expect(req.request.headers.get('Authorization')).toBe('Bearer test-token:999:auth@rpi.edu:Authenticated Student');
    req.flush([]);
  });

  it('should not add Authorization header if user is not authenticated', () => {
    http.get('/api/posts').subscribe();

    const req = httpMock.expectOne('/api/posts');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush([]);
  });

  it('should get and set custom Google client ID in localStorage', () => {
    expect(service.getClientId()).toContain('apps.googleusercontent.com');

    service.setClientId('custom-client-id-123.apps.googleusercontent.com');
    expect(service.getClientId()).toBe('custom-client-id-123.apps.googleusercontent.com');

    service.setClientId('');
    expect(service.getClientId()).toContain('apps.googleusercontent.com');
  });

  it('should take the admin flag only from the signed admin claim, never from the email', () => {
    service.setCredential(appToken({ sub: 'adm-1', email: 'admin@campus-giveaway.test', name: 'John Pole', admin: true }));
    expect(service.isAdmin()).toBeTrue();

    service.setCredential(appToken({ sub: 'u-9', email: 'admin@campus-giveaway.test', name: 'Impostor', admin: false }));
    expect(service.isAdmin()).toBeFalse();
  });

  it('should perform credential registration and store session token', () => {
    service.register({
      firstName: 'Jane',
      lastName: 'Doe',
      email: 'jane@rpi.edu',
      password: 'password123'
    }).subscribe();

    const req = httpMock.expectOne('/api/auth/register');
    expect(req.request.method).toBe('POST');
    req.flush({
      token: appToken({ sub: 'u123', email: 'jane@rpi.edu', name: 'Jane Doe', admin: false }),
      user: {
        id: 'u123',
        email: 'jane@rpi.edu',
        name: 'Jane Doe',
        isAdmin: false
      }
    });

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.currentUser()?.email).toBe('jane@rpi.edu');
    expect(service.isAdmin()).toBeFalse();
  });

  it('should perform credential login and recognize admin role', () => {
    service.login('admin@campus-giveaway.test', 'adminpass').subscribe();

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush({
      token: appToken({ sub: 'admin-id', email: 'admin@campus-giveaway.test', name: 'John Pole', admin: true }),
      user: {
        id: 'admin-id',
        email: 'admin@campus-giveaway.test',
        name: 'John Pole',
        isAdmin: true
      }
    });

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.isAdmin()).toBeTrue();
  });
});
