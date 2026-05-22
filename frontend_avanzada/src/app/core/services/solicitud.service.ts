import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Solicitud,
  NuevaSolicitudRequest,
  ClasificarRequest,
  AsignarResponsableRequest,
  CambiarEstadoRequest,
  CerrarSolicitudRequest,
  ConfirmarSugerenciaIARequest,
  SugerenciaIA,
  PaginatedResponse,
  FiltrosSolicitud
} from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class SolicitudService {
  private readonly API_URL = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  registrar(data: NuevaSolicitudRequest): Observable<Solicitud> {
    return this.http.post<Solicitud>(`${this.API_URL}/solicitudes`, data);
  }

  obtenerPorId(id: number): Observable<Solicitud> {
    return this.http.get<Solicitud>(`${this.API_URL}/solicitudes/${id}`);
  }

  consultar(filtros: FiltrosSolicitud = {}, page: number = 0, size: number = 10): Observable<PaginatedResponse<Solicitud>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (filtros.estado) {
      params = params.set('estado', filtros.estado);
    }
    if (filtros.tipo) {
      params = params.set('tipo', filtros.tipo);
    }
    if (filtros.prioridad) {
      params = params.set('prioridad', filtros.prioridad);
    }
    if (filtros.responsableId) {
      params = params.set('responsableId', filtros.responsableId.toString());
    }
    if (filtros.solicitanteId) {
      params = params.set('solicitanteId', filtros.solicitanteId.toString());
    }

    return this.http.get<PaginatedResponse<Solicitud>>(`${this.API_URL}/solicitudes`, { params });
  }

  clasificar(id: number, data: ClasificarRequest): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.API_URL}/solicitudes/${id}/clasificar`, data);
  }

  priorizar(id: number): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.API_URL}/solicitudes/${id}/priorizar`, {});
  }

  asignarResponsable(id: number, data: AsignarResponsableRequest): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.API_URL}/solicitudes/${id}/responsable`, data);
  }

  iniciarAtencion(id: number, data?: CambiarEstadoRequest): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.API_URL}/solicitudes/${id}/iniciar-atencion`, data || {});
  }

  marcarAtendida(id: number, data?: CambiarEstadoRequest): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.API_URL}/solicitudes/${id}/marcar-atendida`, data || {});
  }

  cerrar(id: number, data: CerrarSolicitudRequest): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.API_URL}/solicitudes/${id}/cerrar`, data);
  }

  cancelar(id: number, data?: CambiarEstadoRequest): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.API_URL}/solicitudes/${id}/cancelar`, data || {});
  }

  obtenerSugerenciaIA(id: number): Observable<SugerenciaIA> {
    return this.http.get<SugerenciaIA>(`${this.API_URL}/solicitudes/${id}/sugerencia-ia`);
  }

  confirmarSugerenciaIA(id: number, data: ConfirmarSugerenciaIARequest): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.API_URL}/solicitudes/${id}/sugerencia-ia/confirmar`, data);
  }
}
