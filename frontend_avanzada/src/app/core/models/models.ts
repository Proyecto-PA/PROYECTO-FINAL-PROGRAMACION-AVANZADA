export type Rol = 'ESTUDIANTE' | 'DOCENTE' | 'ADMINISTRATIVO';

export type TipoSolicitud = 
  | 'REGISTRO_ASIGNATURAS' 
  | 'HOMOLOGACION' 
  | 'CANCELACION_ASIGNATURAS' 
  | 'SOLICITUD_CUPOS' 
  | 'CONSULTA_ACADEMICA' 
  | 'OTRO';

export type CanalOrigen = 'CSU' | 'CORREO' | 'SAC' | 'TELEFONO' | 'PRESENCIAL';

export type EstadoSolicitud = 
  | 'REGISTRADA' 
  | 'CLASIFICADA' 
  | 'EN_ATENCION' 
  | 'ATENDIDA' 
  | 'CERRADA' 
  | 'CANCELADA';

export type Prioridad = 'CRITICA' | 'ALTA' | 'MEDIA' | 'BAJA';

export interface Usuario {
  id: number;
  nombre: string;
  email: string;
  rol: Rol;
  activo: boolean;
}

export interface Solicitud {
  id: number;
  tipo: TipoSolicitud;
  descripcion: string;
  canalOrigen: CanalOrigen;
  estado: EstadoSolicitud;
  prioridad?: Prioridad;
  impactoAcademico?: number;
  fechaRegistro: string;
  fechaLimite?: string;
  solicitante: UsuarioResumen;
  responsable?: UsuarioResumen;
  observaciones?: string;
  justificacionPrioridad?: string;
}

export interface UsuarioResumen {
  id: number;
  nombre: string;
  email: string;
  rol: Rol;
}

export interface HistorialEntry {
  id: number;
  solicitudId: number;
  estadoAnterior: EstadoSolicitud | null;
  estadoNuevo: EstadoSolicitud;
  accionRealizada: string;
  observaciones?: string;
  usuarioResumen: UsuarioResumen;
  fechaAccion: string;
}

export interface SugerenciaIA {
  tipoSugerido: TipoSolicitud;
  prioridadSugerida: Prioridad;
  resumen: string;
  confirmada: boolean;
  fechaSugerencia: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  nombre: string;
  email: string;
  rol: Rol;
  usuarioId: number;
}

export interface RegistroRequest {
  nombre: string;
  email: string;
  password: string;
  rol: Rol;
}

export interface NuevaSolicitudRequest {
  tipo: TipoSolicitud;
  descripcion: string;
  canalOrigen: CanalOrigen;
  fechaLimite?: string;
}

export interface ClasificarRequest {
  tipo: TipoSolicitud;
  impactoAcademico: number;
  observacion?: string;
}

export interface AsignarResponsableRequest {
  responsableId: number;
  observacion?: string;
}

export interface AccionRequest {
  observacion?: string;
}

export interface CerrarSolicitudRequest {
  observacion: string;
}

export interface CambiarEstadoRequest {
  observacion?: string;
}

export interface ConfirmarSugerenciaIARequest {
  aplicar: boolean;
  tipoAjustado?: TipoSolicitud;
  prioridadAjustada?: Prioridad;
}

export interface PaginatedResponse<T> {
  contenido: T[];
  totalElementos: number;
  totalPaginas: number;
  paginaActual: number;
}

export interface FiltrosSolicitud {
  estado?: EstadoSolicitud;
  tipo?: TipoSolicitud;
  prioridad?: Prioridad;
  responsableId?: number;
  solicitanteId?: number;
}

export interface ApiError {
  mensaje: string;
  codigo?: string;
  timestamp?: string;
}
