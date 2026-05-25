import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { LoginRequest, LoginResponse, RegistroRequest, Rol } from '../models/models';
import { environment } from '../../../environments/environment';

interface JwtPayload {
  rol: Rol;
  userId: number;
  sub: string;
  exp: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = environment.apiUrl;
  private readonly TOKEN_KEY = 'token';
  
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasValidToken());
  isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/auth/login`, credentials).pipe(
      tap(response => {
        localStorage.setItem(this.TOKEN_KEY, response.token);
        localStorage.setItem('nombre', response.nombre);
        localStorage.setItem('email', response.email);
        localStorage.setItem('userId', String(response.usuarioId));
        this.isAuthenticatedSubject.next(true);
      })
    );
  }

  registro(data: RegistroRequest): Observable<any> {
    return this.http.post(`${this.API_URL}/auth/registro`, data);
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem('nombre');
    localStorage.removeItem('email');
    localStorage.removeItem('userId');
    this.isAuthenticatedSubject.next(false);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRol(): Rol | null {
    const token = this.getToken();
    if (!token) return null;
    
    try {
      const decoded = jwtDecode<JwtPayload>(token);
      return decoded.rol;
    } catch {
      return null;
    }
  }

  getUserId(): number | null {
    const stored = localStorage.getItem('userId');
    if (stored) return Number(stored);
    
    const token = this.getToken();
    if (!token) return null;
    
    try {
      const decoded = jwtDecode<JwtPayload>(token);
      return decoded.userId;
    } catch {
      return null;
    }
  }

  getNombre(): string | null {
    return localStorage.getItem('nombre');
  }

  getEmail(): string | null {
    return localStorage.getItem('email');
  }

  isAuthenticated(): boolean {
    return this.hasValidToken();
  }

  private hasValidToken(): boolean {
    const token = this.getToken();
    if (!token) return false;
    
    try {
      const decoded = jwtDecode<JwtPayload>(token);
      const currentTime = Date.now() / 1000;
      return decoded.exp > currentTime;
    } catch {
      return false;
    }
  }

  isEstudiante(): boolean {
    return this.getRol() === 'ESTUDIANTE';
  }

  isDocente(): boolean {
    return this.getRol() === 'DOCENTE';
  }

  isAdministrativo(): boolean {
    return this.getRol() === 'ADMINISTRATIVO';
  }

  canClasificar(): boolean {
    return this.isAdministrativo();
  }

  canPriorizar(): boolean {
    return this.isAdministrativo();
  }

  canAsignarResponsable(): boolean {
    return this.isAdministrativo();
  }

  canIniciarAtencion(): boolean {
    return this.isDocente() || this.isAdministrativo();
  }

  canMarcarAtendida(): boolean {
    return this.isDocente() || this.isAdministrativo();
  }

  canCerrar(): boolean {
    return this.isDocente() || this.isAdministrativo();
  }

  canConfirmarSugerenciaIA(): boolean {
    return this.isDocente() || this.isAdministrativo();
  }
}
