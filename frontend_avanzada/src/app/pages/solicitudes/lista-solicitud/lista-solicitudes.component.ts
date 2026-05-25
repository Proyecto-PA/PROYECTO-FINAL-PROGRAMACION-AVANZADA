import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MainLayoutComponent } from '../../../shared/components/main-layout/main-layout.component';
import { SolicitudService } from '../../../core/services/solicitud.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import {
  Solicitud,
  EstadoSolicitud,
  TipoSolicitud,
  Prioridad,
  FiltrosSolicitud
} from '../../../core/models/models';

@Component({
  selector: 'app-lista-solicitudes',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, MainLayoutComponent],
  templateUrl: './lista-solicitudes.component.html',
  styleUrl: './lista-solicitudes.component.css'
})
export class ListaSolicitudesComponent implements OnInit {
  solicitudes: Solicitud[] = [];
  isLoading = false;
  isLoadingCancel = false;
  showCancelarModal = false;
  solicitudACancelar: Solicitud | null = null;
  
  // Paginacion
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  // Filtros
  filtros: FiltrosSolicitud = {};
  
  estados: EstadoSolicitud[] = ['REGISTRADA', 'CLASIFICADA', 'EN_ATENCION', 'ATENDIDA', 'CERRADA', 'CANCELADA'];
  tipos: TipoSolicitud[] = ['REGISTRO_ASIGNATURAS', 'HOMOLOGACION', 'CANCELACION_ASIGNATURAS', 'SOLICITUD_CUPOS', 'CONSULTA_ACADEMICA', 'OTRO'];
  prioridades: Prioridad[] = ['CRITICA', 'ALTA', 'MEDIA', 'BAJA'];

  constructor(
    private solicitudService: SolicitudService,
    public authService: AuthService,
    private toastService: ToastService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['estado']) {
        this.filtros.estado = params['estado'] as EstadoSolicitud;
      }
      this.cargarSolicitudes();
    });
  }

  cargarSolicitudes(): void {
    this.isLoading = true;
    
    this.solicitudService.consultar(this.filtros, this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.solicitudes = response.contenido;
        this.totalElements = response.totalElementos;
        this.totalPages = response.totalPaginas;
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.toastService.httpError(error, 'Error al cargar las solicitudes');
      }
    });
  }

  aplicarFiltros(): void {
    this.currentPage = 0;
    this.cargarSolicitudes();
  }

  limpiarFiltros(): void {
    this.filtros = {};
    this.currentPage = 0;
    this.cargarSolicitudes();
  }

  cambiarPagina(pagina: number): void {
    if (pagina >= 0 && pagina < this.totalPages) {
      this.currentPage = pagina;
      this.cargarSolicitudes();
    }
  }

  verDetalle(id: number): void {
    this.router.navigate(['/solicitudes', id]);
  }

  cancelarSolicitud(solicitud: Solicitud): void {
    this.solicitudACancelar = solicitud;
    this.showCancelarModal = true;
  }

  confirmarCancelar(): void {
    if (!this.solicitudACancelar) return;

    this.isLoadingCancel = true;

    this.solicitudService.cancelar(this.solicitudACancelar.id).subscribe({
      next: () => {
        this.toastService.success('Solicitud cancelada exitosamente');
        this.cerrarCancelarModal();
        this.cargarSolicitudes();
      },
      error: (error) => {
        this.isLoadingCancel = false;
        this.toastService.httpError(error, 'Error al cancelar la solicitud');
      }
    });
  }

  cerrarCancelarModal(): void {
    this.showCancelarModal = false;
    this.solicitudACancelar = null;
    this.isLoadingCancel = false;
  }

  getEstadoBadgeClass(estado: EstadoSolicitud): string {
    const classes: Record<EstadoSolicitud, string> = {
      'REGISTRADA': 'badge-registrada',
      'CLASIFICADA': 'badge-clasificada',
      'EN_ATENCION': 'badge-en-atencion',
      'ATENDIDA': 'badge-atendida',
      'CERRADA': 'badge-cerrada',
      'CANCELADA': 'badge-cancelada'
    };
    return classes[estado] || '';
  }

  getPrioridadBadgeClass(prioridad: Prioridad | undefined): string {
    if (!prioridad) return '';
    const classes: Record<Prioridad, string> = {
      'CRITICA': 'badge-critica',
      'ALTA': 'badge-alta',
      'MEDIA': 'badge-media',
      'BAJA': 'badge-baja'
    };
    return classes[prioridad] || '';
  }

  getRowClass(solicitud: Solicitud): string {
    if (this.authService.isAdministrativo()) {
      if (solicitud.estado === 'REGISTRADA') {
        return 'row-registrada';
      }
      if (solicitud.estado === 'CLASIFICADA' && !solicitud.responsable) {
        return 'row-clasificada-sin-responsable';
      }
    }
    
    if (this.authService.isDocente()) {
      if (solicitud.estado === 'CLASIFICADA' && solicitud.responsable) {
        return 'row-clasificada-con-responsable';
      }
    }
    
    return '';
  }

  formatTipo(tipo: TipoSolicitud): string {
    const labels: Record<TipoSolicitud, string> = {
      'REGISTRO_ASIGNATURAS': 'Registro Asignaturas',
      'HOMOLOGACION': 'Homologacion',
      'CANCELACION_ASIGNATURAS': 'Cancelacion Asignaturas',
      'SOLICITUD_CUPOS': 'Solicitud Cupos',
      'CONSULTA_ACADEMICA': 'Consulta Academica',
      'OTRO': 'Otro'
    };
    return labels[tipo] || tipo;
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  canCancel(solicitud: Solicitud): boolean {
    if (solicitud.estado !== 'REGISTRADA') return false;
    
    if (this.authService.isEstudiante()) {
      const userId = this.authService.getUserId();
      return solicitud.solicitante.id === userId;
    }
    
    return this.authService.isAdministrativo();
  }

  get showAdminFilters(): boolean {
    return this.authService.isAdministrativo();
  }

  get showAllColumns(): boolean {
    return !this.authService.isEstudiante();
  }
}
