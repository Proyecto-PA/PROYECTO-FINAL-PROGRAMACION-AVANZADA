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
  EstadoSolicitud
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
  iaErrorMessage = '';

  showClasificarModal = false;
  showAsignarModal = false;
  showCerrarModal = false;
  showAccionModal = false;
  showCancelarModal = false;

  accionModalTipo: 'iniciar' | 'atender' = 'iniciar';

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

  sugerenciaEditable = {
    tipo: '' as TipoSolicitud | '',
    prioridad: '' as Prioridad | ''
  };

  tipos: { value: TipoSolicitud; label: string }[] = [
    { value: 'REGISTRO_ASIGNATURAS', label: 'Registro de Asignaturas' },
    { value: 'HOMOLOGACION', label: 'Homologación' },
    { value: 'CANCELACION_ASIGNATURAS', label: 'Cancelación de Asignaturas' },
    { value: 'SOLICITUD_CUPOS', label: 'Solicitud de Cupos' },
    { value: 'CONSULTA_ACADEMICA', label: 'Consulta Académica' },
    { value: 'OTRO', label: 'Otro' }
  ];

  prioridades: Prioridad[] = ['CRITICA', 'ALTA', 'MEDIA', 'BAJA'];

  impactoDescripciones = [
    { nivel: 1, descripcion: 'Impacto mínimo - No afecta el avance académico' },
    { nivel: 2, descripcion: 'Impacto bajo - Afecta levemente el progreso' },
    { nivel: 3, descripcion: 'Impacto medio - Afecta moderadamente el semestre' },
    { nivel: 4, descripcion: 'Impacto alto - Puede retrasar significativamente' },
    { nivel: 5, descripcion: 'Impacto crítico - Riesgo de pérdida de semestre' }
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
      this.cargarSolicitud(Number(id));
    } else {
      this.toastService.error('No se encontró el identificador de la solicitud');
      this.router.navigate(['/solicitudes']);
    }
  }

  cargarSolicitud(id: number): void {
    this.isLoading = true;
    this.iaErrorMessage = '';

    this.solicitudService.obtenerPorId(id).subscribe({
      next: (solicitud) => {
        this.solicitud = solicitud;
        this.cargarHistorial(id);
      },
      error: (error) => {
        this.isLoading = false;
        this.toastService.httpError(error, 'Error al cargar la solicitud');
        this.router.navigate(['/solicitudes']);
      }
    });
  }

  cargarHistorial(solicitudId: number): void {
    this.historialService.consultarPorSolicitud(solicitudId).subscribe({
      next: (historial) => {
        this.historial = historial || [];
        this.isLoading = false;
      },
      error: () => {
        this.historial = [];
        this.isLoading = false;
      }
    });
  }

  actualizarDatos(): void {
    if (!this.solicitud) return;

    const id = this.solicitud.id;
    this.sugerenciaIA = null;
    this.iaErrorMessage = '';
    this.cargarSolicitud(id);
  }

  verHistorial(): void {
    const historialElement = document.getElementById('historial-solicitud');

    if (historialElement) {
      historialElement.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
      });
    }
  }

  volver(): void {
    this.router.navigate(['/solicitudes']);
  }

  cancelarSolicitud(): void {
    if (!this.solicitud || !this.canCancel) return;

    this.showCancelarModal = true;
  }

  confirmarCancelar(): void {
    if (!this.solicitud || !this.canCancel) return;

    this.isLoadingAction = true;

    this.solicitudService.cancelar(this.solicitud.id).subscribe({
      next: () => {
        this.toastService.success('Solicitud cancelada exitosamente');
        this.showCancelarModal = false;
        this.cargarSolicitud(this.solicitud!.id);
        this.isLoadingAction = false;
      },
      error: (error) => {
        this.isLoadingAction = false;
        this.toastService.httpError(error, 'Error al cancelar la solicitud');
      }
    });
  }

  abrirClasificarModal(): void {
    if (!this.solicitud || !this.canClasificar) return;

    this.clasificarForm = {
      tipo: this.solicitud.tipo || '',
      impactoAcademico: this.solicitud.impactoAcademico || 3,
      observacion: ''
    };

    this.showClasificarModal = true;
  }

  confirmarClasificar(): void {
    if (!this.solicitud || !this.clasificarForm.tipo || !this.canClasificar) return;

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
        this.toastService.httpError(error, 'Error al clasificar la solicitud');
      }
    });
  }

  recalcularPrioridad(): void {
    if (!this.solicitud || !this.canPriorizar) return;

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
        this.toastService.httpError(error, 'Error al recalcular la prioridad');
      }
    });
  }

  abrirAsignarModal(): void {
    if (!this.solicitud || !this.canAsignarResponsable) return;

    this.asignarForm = {
      responsableId: null,
      observacion: ''
    };

    this.showAsignarModal = true;
  }

  confirmarAsignar(): void {
    if (!this.solicitud || !this.asignarForm.responsableId || !this.canAsignarResponsable) return;

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
        this.toastService.httpError(error, 'Error al asignar responsable');
      }
    });
  }

  abrirIniciarAtencionModal(): void {
    if (!this.solicitud || !this.canIniciarAtencion) return;

    this.accionModalTipo = 'iniciar';
    this.accionForm = { observacion: '' };
    this.showAccionModal = true;
  }

  abrirMarcarAtendidaModal(): void {
    if (!this.solicitud || !this.canMarcarAtendida) return;

    this.accionModalTipo = 'atender';
    this.accionForm = { observacion: '' };
    this.showAccionModal = true;
  }

  confirmarAccion(): void {
    if (!this.solicitud) return;

    if (this.accionModalTipo === 'iniciar' && !this.canIniciarAtencion) return;
    if (this.accionModalTipo === 'atender' && !this.canMarcarAtendida) return;

    this.isLoadingAction = true;

    const observable = this.accionModalTipo === 'iniciar'
      ? this.solicitudService.iniciarAtencion(this.solicitud.id, {
          observacion: this.accionForm.observacion || undefined
        })
      : this.solicitudService.marcarAtendida(this.solicitud.id, {
          observacion: this.accionForm.observacion || undefined
        });

    observable.subscribe({
      next: () => {
        const mensaje = this.accionModalTipo === 'iniciar'
          ? 'Atención iniciada exitosamente'
          : 'Solicitud marcada como atendida';

        this.toastService.success(mensaje);
        this.showAccionModal = false;
        this.cargarSolicitud(this.solicitud!.id);
        this.isLoadingAction = false;
      },
      error: (error) => {
        this.isLoadingAction = false;
        this.toastService.httpError(error, 'Error al realizar la acción');
      }
    });
  }

  abrirCerrarModal(): void {
    if (!this.solicitud || !this.canCerrar) return;

    this.cerrarForm = { observacion: '' };
    this.showCerrarModal = true;
  }

  confirmarCerrar(): void {
    if (!this.solicitud || !this.canCerrar || this.cerrarForm.observacion.trim().length < 10) return;

    this.isLoadingAction = true;

    this.solicitudService.cerrar(this.solicitud.id, {
      observacion: this.cerrarForm.observacion.trim()
    }).subscribe({
      next: () => {
        this.toastService.success('Solicitud cerrada exitosamente');
        this.showCerrarModal = false;
        this.cargarSolicitud(this.solicitud!.id);
        this.isLoadingAction = false;
      },
      error: (error) => {
        this.isLoadingAction = false;
        this.toastService.httpError(error, 'Error al cerrar la solicitud');
      }
    });
  }

  consultarSugerenciaIA(): void {
    if (!this.solicitud) return;

    if (!this.canConsultarIA) {
      this.toastService.error('Solo el rol ADMINISTRATIVO puede consultar sugerencias generadas por IA.');
      return;
    }

    this.isLoadingIA = true;
    this.iaErrorMessage = '';
    this.sugerenciaIA = null;

    this.solicitudService.obtenerSugerenciaIA(this.solicitud.id).subscribe({
      next: (sugerencia) => {
        const sugerenciaNormalizada = this.normalizarSugerenciaIA(sugerencia);

        if (!sugerenciaNormalizada) {
          this.iaErrorMessage = 'El backend respondió, pero no entregó una sugerencia válida.';
          this.isLoadingIA = false;
          return;
        }

        this.sugerenciaIA = sugerenciaNormalizada;
        this.sugerenciaEditable = {
          tipo: sugerenciaNormalizada.tipoSugerido,
          prioridad: sugerenciaNormalizada.prioridadSugerida
        };

        this.toastService.success('Sugerencia IA consultada exitosamente');
        this.isLoadingIA = false;
      },
      error: (error) => {
        this.isLoadingIA = false;
        this.iaErrorMessage = this.toastService.httpError(
          error,
          'No fue posible consultar la sugerencia IA. La solicitud puede seguir gestionándose manualmente.'
        );
      }
    });
  }

  aplicarSugerenciaIA(): void {
    if (
      !this.solicitud ||
      !this.canConfirmarIA ||
      !this.sugerenciaEditable.tipo ||
      !this.sugerenciaEditable.prioridad
    ) {
      return;
    }

    this.isLoadingIA = true;

    this.solicitudService.confirmarSugerenciaIA(this.solicitud.id, {
      aplicar: true,
      tipoAjustado: this.sugerenciaEditable.tipo as TipoSolicitud,
      prioridadAjustada: this.sugerenciaEditable.prioridad as Prioridad
    }).subscribe({
      next: () => {
        this.toastService.success('Sugerencia IA aplicada exitosamente');
        this.sugerenciaIA = null;
        this.iaErrorMessage = '';
        this.cargarSolicitud(this.solicitud!.id);
        this.isLoadingIA = false;
      },
      error: (error) => {
        this.isLoadingIA = false;
        this.toastService.httpError(error, 'Error al aplicar la sugerencia IA');
      }
    });
  }

  descartarSugerenciaIA(): void {
    if (!this.solicitud || !this.canConfirmarIA) return;

    this.isLoadingIA = true;

    this.solicitudService.confirmarSugerenciaIA(this.solicitud.id, {
      aplicar: false,
      tipoAjustado: this.solicitud.tipo,
      prioridadAjustada: this.solicitud.prioridad || 'MEDIA'
    }).subscribe({
      next: () => {
        this.toastService.success('Sugerencia IA descartada');
        this.sugerenciaIA = null;
        this.iaErrorMessage = '';
        this.cargarHistorial(this.solicitud!.id);
        this.isLoadingIA = false;
      },
      error: (error) => {
        this.isLoadingIA = false;
        this.toastService.httpError(error, 'Error al descartar la sugerencia IA');
      }
    });
  }

  limpiarSugerenciaIA(): void {
    this.sugerenciaIA = null;
    this.iaErrorMessage = '';
    this.sugerenciaEditable = {
      tipo: '',
      prioridad: ''
    };
  }

  private normalizarSugerenciaIA(sugerencia: SugerenciaIA | null | undefined): SugerenciaIA | null {
    if (!sugerencia || !sugerencia.tipoSugerido || !sugerencia.prioridadSugerida) {
      return null;
    }

    return {
      tipoSugerido: sugerencia.tipoSugerido as TipoSolicitud,
      prioridadSugerida: sugerencia.prioridadSugerida as Prioridad,
      resumen: sugerencia.resumen || 'El backend no envió resumen para esta sugerencia.',
      confirmada: Boolean(sugerencia.confirmada),
      fechaSugerencia: sugerencia.fechaSugerencia
    };
  }

  getEstadoBadgeClass(estado: EstadoSolicitud): string {
    const classes: Record<EstadoSolicitud, string> = {
      REGISTRADA: 'badge-registrada',
      CLASIFICADA: 'badge-clasificada',
      EN_ATENCION: 'badge-en-atencion',
      ATENDIDA: 'badge-atendida',
      CERRADA: 'badge-cerrada',
      CANCELADA: 'badge-cancelada'
    };

    return classes[estado] || '';
  }

  getPrioridadBadgeClass(prioridad: Prioridad | undefined): string {
    if (!prioridad) return '';

    const classes: Record<Prioridad, string> = {
      CRITICA: 'badge-critica',
      ALTA: 'badge-alta',
      MEDIA: 'badge-media',
      BAJA: 'badge-baja'
    };

    return classes[prioridad] || '';
  }

  formatTipo(tipo: TipoSolicitud): string {
    const labels: Record<TipoSolicitud, string> = {
      REGISTRO_ASIGNATURAS: 'Registro de Asignaturas',
      HOMOLOGACION: 'Homologación',
      CANCELACION_ASIGNATURAS: 'Cancelación de Asignaturas',
      SOLICITUD_CUPOS: 'Solicitud de Cupos',
      CONSULTA_ACADEMICA: 'Consulta Académica',
      OTRO: 'Otro'
    };

    return labels[tipo] || tipo;
  }

  formatEstado(estado: EstadoSolicitud): string {
    const labels: Record<EstadoSolicitud, string> = {
      REGISTRADA: 'Registrada',
      CLASIFICADA: 'Clasificada',
      EN_ATENCION: 'En atención',
      ATENDIDA: 'Atendida',
      CERRADA: 'Cerrada',
      CANCELADA: 'Cancelada'
    };

    return labels[estado] || estado;
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return '-';

    return new Date(dateString).toLocaleDateString('es-CO', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatCanalOrigen(canal: string): string {
    const labels: Record<string, string> = {
      CSU: 'CSU',
      CORREO: 'Correo Electrónico',
      SAC: 'SAC',
      TELEFONO: 'Teléfono',
      PRESENCIAL: 'Presencial'
    };

    return labels[canal] || canal;
  }

  getImpactoDescripcion(nivel: number): string {
    return this.impactoDescripciones.find(i => i.nivel === nivel)?.descripcion || '';
  }

  get rolActual(): string {
    return this.authService.getRol() || 'SIN_ROL';
  }

  get isAutenticado(): boolean {
    return this.authService.isAuthenticated();
  }

  get isAdministrativo(): boolean {
    return this.authService.isAdministrativo();
  }

  get isDocente(): boolean {
    return this.authService.isDocente();
  }

  get isEstudiante(): boolean {
    return this.authService.isEstudiante();
  }

  get puedeAtenderSolicitud(): boolean {
    return this.isDocente || this.isAdministrativo;
  }

  get puedeGestionarAdministrativamente(): boolean {
    return this.isAdministrativo;
  }

  get isEstadoTerminal(): boolean {
    return this.solicitud?.estado === 'CERRADA'
      || this.solicitud?.estado === 'CANCELADA';
  }

  get canCancel(): boolean {
    return !!this.solicitud
      && this.isAutenticado
      && !this.isEstadoTerminal;
  }

  get canClasificar(): boolean {
    return !!this.solicitud
      && this.isAdministrativo
      && this.solicitud.estado === 'REGISTRADA';
  }

  get canPriorizar(): boolean {
    return !!this.solicitud
      && this.isAdministrativo
      && this.solicitud.estado === 'CLASIFICADA';
  }

  get canAsignarResponsable(): boolean {
    return !!this.solicitud
      && this.isAdministrativo
      && !this.isEstadoTerminal;
  }

  get canIniciarAtencion(): boolean {
    return !!this.solicitud
      && this.puedeAtenderSolicitud
      && this.solicitud.estado === 'CLASIFICADA';
  }

  get canMarcarAtendida(): boolean {
    return !!this.solicitud
      && this.puedeAtenderSolicitud
      && this.solicitud.estado === 'EN_ATENCION';
  }

  get canCerrar(): boolean {
    return !!this.solicitud
      && this.puedeAtenderSolicitud
      && this.solicitud.estado === 'ATENDIDA';
  }

  get canVerPanelIA(): boolean {
    return this.isAdministrativo;
  }

  get canConsultarIA(): boolean {
    return !!this.solicitud
      && this.isAdministrativo
      && !this.isEstadoTerminal;
  }

  get canConfirmarIA(): boolean {
    return !!this.solicitud
      && !!this.sugerenciaIA
      && this.isAdministrativo
      && !this.isEstadoTerminal;
  }

  get hasMainActions(): boolean {
    return this.canClasificar
      || this.canPriorizar
      || this.canAsignarResponsable
      || this.canIniciarAtencion
      || this.canMarcarAtendida
      || this.canCerrar
      || this.canCancel;
  }

  get mensajeSinAcciones(): string {
    if (!this.solicitud) {
      return 'No hay acciones disponibles.';
    }

    if (this.isEstadoTerminal) {
      return 'La solicitud se encuentra en un estado terminal. No se permiten nuevas acciones.';
    }

    if (this.isEstudiante) {
      return 'Como estudiante puedes registrar, consultar, ver el detalle y cancelar solicitudes cuando el estado lo permita.';
    }

    if (this.isDocente) {
      if (this.solicitud.estado === 'REGISTRADA') {
        return 'La solicitud aún debe ser clasificada por un usuario administrativo.';
      }

      if (this.solicitud.estado === 'CLASIFICADA') {
        return 'Como docente puedes iniciar la atención de esta solicitud.';
      }

      return 'Como docente puedes cambiar el estado de la solicitud cuando el flujo lo permita.';
    }

    if (this.isAdministrativo) {
      return 'Como administrativo tienes control completo del flujo, pero esta solicitud no tiene acciones disponibles en el estado actual.';
    }

    return 'No hay acciones principales disponibles para tu rol en el estado actual de la solicitud.';
  }
}