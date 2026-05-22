import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { publicGuard } from './core/guards/public.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'solicitudes',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent),
    canActivate: [publicGuard]
  },
  {
    path: 'registro',
    loadComponent: () => import('./pages/registro/registro.component').then(m => m.RegistroComponent),
    canActivate: [publicGuard]
  },
  {
    path: 'dashboard',
    redirectTo: 'solicitudes',
    pathMatch: 'full'
  },
  {
    path: 'solicitudes',
    loadComponent: () => import('./pages/solicitudes/lista-solicitud/lista-solicitudes.component').then(m => m.ListaSolicitudesComponent),
    canActivate: [authGuard]
  },
  {
    path: 'solicitudes/nueva',
    loadComponent: () => import('./pages/solicitudes/nueva-solicitud/nueva-solicitud.component').then(m => m.NuevaSolicitudComponent),
    canActivate: [authGuard]
  },
  {
    path: 'solicitudes/:id',
    loadComponent: () => import('./pages/solicitudes/detalle-solicitud/detalle-solicitud.component').then(m => m.DetalleSolicitudComponent),
    canActivate: [authGuard]
  },
  {
    path: '**',
    redirectTo: 'solicitudes'
  }
];
