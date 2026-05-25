import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MainLayoutComponent } from '../../../shared/components/main-layout/main-layout.component';
import { SolicitudService } from '../../../core/services/solicitud.service';
import { ToastService } from '../../../core/services/toast.service';
import { TipoSolicitud, CanalOrigen } from '../../../core/models/models';

@Component({
  selector: 'app-nueva-solicitud',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MainLayoutComponent],
  templateUrl: './nueva-solicitud.component.html',
  styleUrl: './nueva-solicitud.component.css'
})
export class NuevaSolicitudComponent {
  solicitudForm: FormGroup;
  isLoading = false;

  tipos: { value: TipoSolicitud; label: string }[] = [
    { value: 'REGISTRO_ASIGNATURAS', label: 'Registro de Asignaturas' },
    { value: 'HOMOLOGACION', label: 'Homologacion' },
    { value: 'CANCELACION_ASIGNATURAS', label: 'Cancelacion de Asignaturas' },
    { value: 'SOLICITUD_CUPOS', label: 'Solicitud de Cupos' },
    { value: 'CONSULTA_ACADEMICA', label: 'Consulta Academica' },
    { value: 'OTRO', label: 'Otro' }
  ];

  canales: { value: CanalOrigen; label: string }[] = [
    { value: 'CSU', label: 'CSU' },
    { value: 'CORREO', label: 'Correo Electronico' },
    { value: 'SAC', label: 'SAC' },
    { value: 'TELEFONO', label: 'Telefono' },
    { value: 'PRESENCIAL', label: 'Presencial' }
  ];

  constructor(
    private fb: FormBuilder,
    private solicitudService: SolicitudService,
    private toastService: ToastService,
    private router: Router
  ) {
    this.solicitudForm = this.fb.group({
      tipo: ['', Validators.required],
      descripcion: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]],
      canalOrigen: ['', Validators.required],
      fechaLimite: ['']
    });
  }

  onSubmit(): void {
    if (this.solicitudForm.invalid) {
      this.solicitudForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const formValue = this.solicitudForm.value;
    
    const data = {
      tipo: formValue.tipo,
      descripcion: formValue.descripcion,
      canalOrigen: formValue.canalOrigen,
      fechaLimite: formValue.fechaLimite || undefined
    };

    this.solicitudService.registrar(data).subscribe({
      next: (solicitud) => {
        this.toastService.success('Solicitud registrada exitosamente');
        this.router.navigate(['/solicitudes', solicitud.id]);
      },
      error: (error) => {
        this.isLoading = false;
        this.toastService.httpError(error, 'Error al registrar la solicitud');
      }
    });
  }

  get descripcion() {
    return this.solicitudForm.get('descripcion');
  }

  get descripcionLength(): number {
    return this.descripcion?.value?.length || 0;
  }

  cancelar(): void {
    this.router.navigate(['/solicitudes']);
  }
}
