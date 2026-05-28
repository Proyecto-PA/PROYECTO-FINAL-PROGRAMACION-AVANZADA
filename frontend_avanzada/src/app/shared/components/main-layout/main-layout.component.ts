import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { Rol } from '../../../core/models/models';

interface MenuItem {
  label: string;
  route: string;
  queryParams?: Record<string, string>;
  roles: Rol[];
}

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css'
})
export class MainLayoutComponent {
  isSidebarOpen = true;
  
  menuItems: MenuItem[] = [
    // ESTUDIANTE menu
    { label: 'Mis Solicitudes', route: '/solicitudes', roles: ['ESTUDIANTE'] },
    { label: 'Nueva Solicitud', route: '/solicitudes/nueva', roles: ['ESTUDIANTE'] },
    
    // DOCENTE menu
    { label: 'Todas las Solicitudes', route: '/solicitudes', roles: ['DOCENTE'] },
    {label: 'Nueva Solicitud', route: '/solicitudes/nueva', roles: ['DOCENTE'] },
    { label: 'Mis Acciones Pendientes', route: '/solicitudes', queryParams: { estado: 'EN_ATENCION' }, roles: ['DOCENTE'] },
    
    // ADMINISTRATIVO menu
    { label: 'Todas las Solicitudes', route: '/solicitudes', roles: ['ADMINISTRATIVO'] },
    { label: 'Pendientes de Clasificar', route: '/solicitudes', queryParams: { estado: 'REGISTRADA' }, roles: ['ADMINISTRATIVO'] },
    { label: 'Pendientes de Responsable', route: '/solicitudes', queryParams: { estado: 'CLASIFICADA', sinResponsable: 'true' }, roles: ['ADMINISTRATIVO'] },
    { label: 'Nueva Solicitud', route: '/solicitudes/nueva', roles: ['ADMINISTRATIVO'] }
  ];

  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  get filteredMenuItems(): MenuItem[] {
    const rol = this.authService.getRol();
    if (!rol) return [];
    return this.menuItems.filter(item => item.roles.includes(rol));
  }

  get userRol(): Rol | null {
    return this.authService.getRol();
  }

  get userName(): string | null {
    return this.authService.getNombre();
  }

  getRolBadgeClass(): string {
    switch (this.userRol) {
      case 'ADMINISTRATIVO':
        return 'badge-administrativo';
      case 'DOCENTE':
        return 'badge-docente';
      case 'ESTUDIANTE':
        return 'badge-estudiante';
      default:
        return '';
    }
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
