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
  solicitante: Usuario;
  responsable?: Usuario;
  observaciones?: string;
  justificacionPrioridad?: string;
}

export interface HistorialEntry {
  id: number;
  solicitudId: number;
  estadoAnterior: EstadoSolicitud | null;
  estadoNuevo: EstadoSolicitud;
  accion: string;
  observaciones?: string;
  usuario: Usuario;
  fecha: string;
}

export interface SugerenciaIA {
  tipoSugerido: TipoSolicitud;
  prioridadSugerida: Prioridad;
  resumen: string;
  confianza: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  nombre: string;
  rol: Rol;
  userId: number;
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

export interface CerrarRequest {
  observacion: string;
}

export interface ConfirmarSugerenciaIARequest {
  tipoConfirmado: TipoSolicitud;
  prioridadConfirmada: Prioridad;
  aplicar: boolean;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
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
