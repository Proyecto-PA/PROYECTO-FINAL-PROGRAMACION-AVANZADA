import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MainLayoutComponent } from '../../../shared/components/main-layout/main-layout.component';
import { SolicitudService } from '../../../core/services/solicitud.service';
import { HistorialService } from '../../../core/services/historial.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { 
  Solicitud, 
  HistorialEntry, 
  SugerenciaIA, 
  TipoSolicitud, 
  Prioridad, 
  EstadoSolicitud,
  ApiError 
} from '../../../core/models/models';

@Component({
  selector: 'app-detalle-solicitud',
  standalone: true,
  imports: [CommonModule, FormsModule, MainLayoutComponent],
  templateUrl: './detalle-solicitud.component.html',
  styleUrl: './detalle-solicitud.component.css'
})
export class DetalleSolicitudComponent implements OnInit {
  solicitud: Solicitud | null = null;
  historial: HistorialEntry[] = [];
  sugerenciaIA: SugerenciaIA | null = null;
  
  isLoading = true;
  isLoadingAction = false;
  isLoadingIA = false;
  
  // Modales
  showClasificarModal = false;
  showAsignarModal = false;
  showCerrarModal = false;
  showAccionModal = false;
  accionModalTipo: 'iniciar' | 'atender' = 'iniciar';
  
  // Formularios de modales
  clasificarForm = {
    tipo: '' as TipoSolicitud | '',
    impactoAcademico: 3,
    observacion: ''
  };
  
  asignarForm = {
    responsableId: null as number | null,
    observacion: ''
  };
  
  cerrarForm = {
    observacion: ''
  };
  
  accionForm = {
    observacion: ''
  };

  // Sugerencia IA editable
  sugerenciaEditable = {
    tipo: '',
    prioridad: '' 
  };

  tipos: { value: TipoSolicitud; label: string }[] = [
    { value: 'REGISTRO_ASIGNATURAS', label: 'Registro de Asignaturas' },
    { value: 'HOMOLOGACION', label: 'Homologacion' },
    { value: 'CANCELACION_ASIGNATURAS', label: 'Cancelacion de Asignaturas' },
    { value: 'SOLICITUD_CUPOS', label: 'Solicitud de Cupos' },
    { value: 'CONSULTA_ACADEMICA', label: 'Consulta Academica' },
    { value: 'OTRO', label: 'Otro' }
  ];

  prioridades: Prioridad[] = ['CRITICA', 'ALTA', 'MEDIA', 'BAJA'];

  impactoDescripciones = [
    { nivel: 1, descripcion: 'Impacto minimo - No afecta el avance academico' },
    { nivel: 2, descripcion: 'Impacto bajo - Afecta levemente el progreso' },
    { nivel: 3, descripcion: 'Impacto medio - Afecta moderadamente el semestre' },
    { nivel: 4, descripcion: 'Impacto alto - Puede retrasar significativamente' },
    { nivel: 5, descripcion: 'Impacto critico - Riesgo de perdida de semestre' }
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private solicitudService: SolicitudService,
    private historialService: HistorialService,
    public authService: AuthService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.cargarSolicitud(parseInt(id));
    }
  }

  cargarSolicitud(id: number): void {
    this.isLoading = true;
    
    this.solicitudService.obtenerPorId(id).subscribe({
      next: (solicitud) => {
        this.solicitud = solicitud;
        this.cargarHistorial(id);
      },
      error: (error) => {
        this.isLoading = false;
        const apiError = error.error as ApiError;
        this.toastService.error(apiError?.mensaje || 'Error al cargar la solicitud');
        this.router.navigate(['/solicitudes']);
      }
    });
  }

  cargarHistorial(solicitudId: number): void {
    this.historialService.consultarPorSolicitud(solicitudId).subscribe({
      next: (historial) => {
        this.historial = historial;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  // Acciones
  cancelarSolicitud(): void {
    if (!this.solicitud) return;
    
    if (confirm('Estas seguro de que deseas cancelar esta solicitud?')) {
      this.isLoadingAction = true;
      this.solicitudService.cancelar(this.solicitud.id).subscribe({
        next: () => {
          this.toastService.success('Solicitud cancelada exitosamente');
          this.cargarSolicitud(this.solicitud!.id);
          this.isLoadingAction = false;
        },
        error: (error) => {
          this.isLoadingAction = false;
          const apiError = error.error as ApiError;
          this.toastService.error(apiError?.mensaje || 'Error al cancelar');
        }
      });
    }
  }

  // Clasificar
  abrirClasificarModal(): void {
    this.clasificarForm = {
      tipo: this.solicitud?.tipo || '',
      impactoAcademico: 3,
      observacion: ''
    };
    this.showClasificarModal = true;
  }

  confirmarClasificar(): void {
    if (!this.solicitud || !this.clasificarForm.tipo) return;
    
    this.isLoadingAction = true;
    this.solicitudService.clasificar(this.solicitud.id, {
      tipo: this.clasificarForm.tipo as TipoSolicitud,
      impactoAcademico: this.clasificarForm.impactoAcademico,
      observacion: this.clasificarForm.observacion || undefined
    }).subscribe({
      next: () => {
        this.toastService.success('Solicitud clasificada exitosamente');
        this.showClasificarModal = false;
        this.cargarSolicitud(this.solicitud!.id);
        this.isLoadingAction = false;
      },
      error: (error) => {
        this.isLoadingAction = false;
        const apiError = error.error as ApiError;
        this.toastService.error(apiError?.mensaje || 'Error al clasificar');
      }
    });
  }

  // Priorizar
  recalcularPrioridad(): void {
    if (!this.solicitud) return;
    
    this.isLoadingAction = true;
    this.solicitudService.priorizar(this.solicitud.id).subscribe({
      next: (solicitud) => {
        this.solicitud = solicitud;
        this.toastService.success('Prioridad recalculada exitosamente');
        this.cargarHistorial(solicitud.id);
        this.isLoadingAction = false;
      },
      error: (error) => {
        this.isLoadingAction = false;
        const apiError = error.error as ApiError;
        this.toastService.error(apiError?.mensaje || 'Error al priorizar');
      }
    });
  }

  // Asignar responsable
  abrirAsignarModal(): void {
    this.asignarForm = { responsableId: null, observacion: '' };
    this.showAsignarModal = true;
  }

  confirmarAsignar(): void {
    if (!this.solicitud || !this.asignarForm.responsableId) return;
    
    this.isLoadingAction = true;
    this.solicitudService.asignarResponsable(this.solicitud.id, {
      responsableId: this.asignarForm.responsableId,
      observacion: this.asignarForm.observacion || undefined
    }).subscribe({
      next: () => {
        this.toastService.success('Responsable asignado exitosamente');
        this.showAsignarModal = false;
        this.cargarSolicitud(this.solicitud!.id);
        this.isLoadingAction = false;
      },
      error: (error) => {
        this.isLoadingAction = false;
        const apiError = error.error as ApiError;
        this.toastService.error(apiError?.mensaje || 'Error al asignar responsable');
      }
    });
  }

  // Iniciar atencion
  abrirIniciarAtencionModal(): void {
    this.accionModalTipo = 'iniciar';
    this.accionForm = { observacion: '' };
    this.showAccionModal = true;
  }

  // Marcar atendida
  abrirMarcarAtendidaModal(): void {
    this.accionModalTipo = 'atender';
    this.accionForm = { observacion: '' };
    this.showAccionModal = true;
  }

  confirmarAccion(): void {
    if (!this.solicitud) return;
    
    this.isLoadingAction = true;
    const observable = this.accionModalTipo === 'iniciar'
      ? this.solicitudService.iniciarAtencion(this.solicitud.id, { observacion: this.accionForm.observacion || undefined })
      : this.solicitudService.marcarAtendida(this.solicitud.id, { observacion: this.accionForm.observacion || undefined });
    
    observable.subscribe({
      next: () => {
        const mensaje = this.accionModalTipo === 'iniciar' 
          ? 'Atencion iniciada exitosamente' 
          : 'Solicitud marcada como atendida';
        this.toastService.success(mensaje);
        this.showAccionModal = false;
        this.cargarSolicitud(this.solicitud!.id);
        this.isLoadingAction = false;
      },
      error: (error) => {
        this.isLoadingAction = false;
        const apiError = error.error as ApiError;
        this.toastService.error(apiError?.mensaje || 'Error al realizar la accion');
      }
    });
  }

  // Cerrar
  abrirCerrarModal(): void {
    this.cerrarForm = { observacion: '' };
    this.showCerrarModal = true;
  }

  confirmarCerrar(): void {
    if (!this.solicitud || this.cerrarForm.observacion.length < 10) return;
    
    this.isLoadingAction = true;
    this.solicitudService.cerrar(this.solicitud.id, {
      observacion: this.cerrarForm.observacion
    }).subscribe({
      next: () => {
        this.toastService.success('Solicitud cerrada exitosamente');
        this.showCerrarModal = false;
        this.cargarSolicitud(this.solicitud!.id);
        this.isLoadingAction = false;
      },
      error: (error) => {
        this.isLoadingAction = false;
        const apiError = error.error as ApiError;
        this.toastService.error(apiError?.mensaje || 'Error al cerrar');
      }
    });
  }

  // Sugerencia IA
  consultarSugerenciaIA(): void {
    if (!this.solicitud) return;
    
    this.isLoadingIA = true;
    this.solicitudService.obtenerSugerenciaIA(this.solicitud.id).subscribe({
      next: (sugerencia) => {
        this.sugerenciaIA = sugerencia;
        this.sugerenciaEditable = {
          tipo: sugerencia.tipoSugerido,
          prioridad: sugerencia.prioridadSugerida
        };
        this.isLoadingIA = false;
      },
      error: (error) => {
        this.isLoadingIA = false;
        const apiError = error.error as ApiError;
        this.toastService.error(apiError?.mensaje || 'Error al consultar sugerencia IA');
      }
    });
  }

  aplicarSugerenciaIA(): void {
    if (!this.solicitud || !this.sugerenciaEditable.tipo || !this.sugerenciaEditable.prioridad) return;
    
    this.isLoadingIA = true;
    this.solicitudService.confirmarSugerenciaIA(this.solicitud.id, {
      tipoAjustado: this.sugerenciaEditable.tipo as TipoSolicitud,
      prioridadAjustada: this.sugerenciaEditable.prioridad as Prioridad,
      aplicar: true
    }).subscribe({
      next: () => {
        this.toastService.success('Sugerencia IA aplicada exitosamente');
        this.sugerenciaIA = null;
        this.cargarSolicitud(this.solicitud!.id);
        this.isLoadingIA = false;
      },
      error: (error) => {
        this.isLoadingIA = false;
        const apiError = error.error as ApiError;
        this.toastService.error(apiError?.mensaje || 'Error al aplicar sugerencia');
      }
    });
  }

  descartarSugerenciaIA(): void {
    if (!this.solicitud) return;
    
    this.isLoadingIA = true;
    this.solicitudService.confirmarSugerenciaIA(this.solicitud.id, {
      tipoAjustado: this.solicitud.tipo,
      prioridadAjustada: this.solicitud.prioridad || 'MEDIA',
      aplicar: false
    }).subscribe({
      next: () => {
        this.toastService.success('Sugerencia descartada');
        this.sugerenciaIA = null;
        this.isLoadingIA = false;
      },
      error: (error) => {
        this.isLoadingIA = false;
        const apiError = error.error as ApiError;
        this.toastService.error(apiError?.mensaje || 'Error al descartar sugerencia');
      }
    });
  }

  // Helpers
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

  formatTipo(tipo: TipoSolicitud): string {
    const labels: Record<TipoSolicitud, string> = {
      'REGISTRO_ASIGNATURAS': 'Registro de Asignaturas',
      'HOMOLOGACION': 'Homologacion',
      'CANCELACION_ASIGNATURAS': 'Cancelacion de Asignaturas',
      'SOLICITUD_CUPOS': 'Solicitud de Cupos',
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
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatCanalOrigen(canal: string): string {
    const labels: Record<string, string> = {
      'CSU': 'CSU',
      'CORREO': 'Correo Electronico',
      'SAC': 'SAC',
      'TELEFONO': 'Telefono',
      'PRESENCIAL': 'Presencial'
    };
    return labels[canal] || canal;
  }

  getImpactoDescripcion(nivel: number): string {
    return this.impactoDescripciones.find(i => i.nivel === nivel)?.descripcion || '';
  }

  volver(): void {
    this.router.navigate(['/solicitudes']);
  }

  // Permisos condicionales
  get canCancel(): boolean {
    if (!this.solicitud || this.solicitud.estado !== 'REGISTRADA') return false;
    
    if (this.authService.isEstudiante()) {
      return this.solicitud.solicitante.id === this.authService.getUserId();
    }
    
    return this.authService.isAdministrativo();
  }

  get canClasificar(): boolean {
    return this.solicitud?.estado === 'REGISTRADA' && this.authService.isAdministrativo();
  }

  get canPriorizar(): boolean {
    return this.solicitud?.estado === 'CLASIFICADA' && this.authService.isAdministrativo();
  }

  get canAsignarResponsable(): boolean {
    return this.solicitud?.estado === 'CLASIFICADA' && this.authService.isAdministrativo();
  }

  get canIniciarAtencion(): boolean {
    return this.solicitud?.estado === 'CLASIFICADA' 
      && !!this.solicitud?.responsable 
      && (this.authService.isDocente() || this.authService.isAdministrativo());
  }

  get canMarcarAtendida(): boolean {
    return this.solicitud?.estado === 'EN_ATENCION' 
      && (this.authService.isDocente() || this.authService.isAdministrativo());
  }

  get canCerrar(): boolean {
    return this.solicitud?.estado === 'ATENDIDA' 
      && (this.authService.isDocente() || this.authService.isAdministrativo());
  }

  get canConfirmarIA(): boolean {
    return this.authService.isDocente() || this.authService.isAdministrativo();
  }

  get isEstadoTerminal(): boolean {
    return this.solicitud?.estado === 'CERRADA' || this.solicitud?.estado === 'CANCELADA';
  }
}
