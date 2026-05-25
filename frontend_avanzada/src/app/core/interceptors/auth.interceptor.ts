import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const toastService = inject(ToastService);

  const token = authService.getToken();

  const handled = token
    ? next(req.clone({ headers: req.headers.set('Authorization', `Bearer ${token}`) }))
    : next(req);

  return handled.pipe(
    catchError((error: HttpErrorResponse) => {
      const isAuthEndpoint = req.url.includes('/auth/login') || req.url.includes('/auth/registro');

      if (error.status === 401 && !isAuthEndpoint) {
        authService.logout();
        toastService.error('Tu sesión ha expirado. Por favor inicia sesión nuevamente.');
        router.navigate(['/login']);
      }

      return throwError(() => error);
    })
  );
};
