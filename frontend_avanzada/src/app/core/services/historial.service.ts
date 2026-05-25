import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HistorialEntry } from '../models/models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class HistorialService {
  private readonly API_URL = environment.apiUrl;

  constructor(private http: HttpClient) {}

  consultarPorSolicitud(solicitudId: number): Observable<HistorialEntry[]> {
    return this.http.get<HistorialEntry[]>(`${this.API_URL}/historial/${solicitudId}`);
  }
}
