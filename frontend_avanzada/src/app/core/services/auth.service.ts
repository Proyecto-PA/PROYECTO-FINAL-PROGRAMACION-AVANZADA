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
  private readonly ROL_KEY = 'rol';
  private readonly USER_ID_KEY = 'userId';
  private readonly NOMBRE_KEY = 'nombre';
  private readonly EMAIL_KEY = 'email';

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasValidToken());
  isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/auth/login`, credentials).pipe(
      tap(response => this.guardarSesion(response))
    );
  }

  registro(data: RegistroRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/auth/registro`, data).pipe(
      tap(response => this.guardarSesion(response))
    );
  }

  private guardarSesion(response: LoginResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.NOMBRE_KEY, response.nombre);
    localStorage.setItem(this.EMAIL_KEY, response.email);
    localStorage.setItem(this.USER_ID_KEY, String(response.usuarioId));
    localStorage.setItem(this.ROL_KEY, response.rol);

    this.isAuthenticatedSubject.next(true);
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.NOMBRE_KEY);
    localStorage.removeItem(this.EMAIL_KEY);
    localStorage.removeItem(this.USER_ID_KEY);
    localStorage.removeItem(this.ROL_KEY);

    this.isAuthenticatedSubject.next(false);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRol(): Rol | null {
    const storedRol = localStorage.getItem(this.ROL_KEY) as Rol | null;

    if (storedRol === 'ESTUDIANTE' || storedRol === 'DOCENTE' || storedRol === 'ADMINISTRATIVO') {
      return storedRol;
    }

    const token = this.getToken();

    if (!token) {
      return null;
    }

    try {
      const decoded = jwtDecode<JwtPayload>(token);

      if (
        decoded.rol === 'ESTUDIANTE' ||
        decoded.rol === 'DOCENTE' ||
        decoded.rol === 'ADMINISTRATIVO'
      ) {
        localStorage.setItem(this.ROL_KEY, decoded.rol);
        return decoded.rol;
      }

      return null;
    } catch {
      return null;
    }
  }

  getUserId(): number | null {
    const stored = localStorage.getItem(this.USER_ID_KEY);

    if (stored) {
      return Number(stored);
    }

    const token = this.getToken();

    if (!token) {
      return null;
    }

    try {
      const decoded = jwtDecode<JwtPayload>(token);
      localStorage.setItem(this.USER_ID_KEY, String(decoded.userId));
      return decoded.userId;
    } catch {
      return null;
    }
  }

  getNombre(): string | null {
    return localStorage.getItem(this.NOMBRE_KEY);
  }

  getEmail(): string | null {
    return localStorage.getItem(this.EMAIL_KEY);
  }

  isAuthenticated(): boolean {
    return this.hasValidToken();
  }

  private hasValidToken(): boolean {
    const token = this.getToken();

    if (!token) {
      return false;
    }

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

  canConsultarSugerenciaIA(): boolean {
    return this.isAdministrativo();
  }

  canConfirmarSugerenciaIA(): boolean {
    return this.isAdministrativo();
  }
}