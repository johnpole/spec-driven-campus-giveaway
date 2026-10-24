import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.idToken();

  if (!token || !req.url.startsWith('/api')) {
    return next(req);
  }

  const authed = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  return next(authed).pipe(
    catchError((err: unknown) => {
      // A stale or expired session must not break public pages: sign out and retry anonymously.
      if (err instanceof HttpErrorResponse && err.status === 401) {
        authService.signOut();
        return next(req);
      }
      return throwError(() => err);
    })
  );
};
