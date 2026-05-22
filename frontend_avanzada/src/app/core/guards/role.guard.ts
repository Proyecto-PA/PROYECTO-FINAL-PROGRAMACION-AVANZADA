import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Rol } from '../models/models';

export const roleGuard = (allowedRoles: Rol[]): CanActivateFn => {
  return (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const userRole = authService.getRol();

    if (userRole && allowedRoles.includes(userRole)) {
      return true;
    }

    router.navigate(['/solicitudes']);
    return false;
  };
};
