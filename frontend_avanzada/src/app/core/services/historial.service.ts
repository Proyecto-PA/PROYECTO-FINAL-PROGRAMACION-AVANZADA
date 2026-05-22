import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HistorialEntry } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class HistorialService {
  private readonly API_URL = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  consultarPorSolicitud(solicitudId: number): Observable<HistorialEntry[]> {
    return this.http.get<HistorialEntry[]>(`${this.API_URL}/historial/${solicitudId}`);
  }
}
