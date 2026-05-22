# Sistema de Triage y Gestión de Solicitudes Académicas

Sistema backend desarrollado con **Spring Boot** para el Programa de Ingeniería de Sistemas y Computación de la Universidad del Quindío. Permite registrar, clasificar, priorizar y gestionar el ciclo de vida completo de solicitudes académicas (homologaciones, registro de asignaturas, cancelaciones, cupos, entre otras) provenientes de múltiples canales.

---

## Tabla de contenidos

- [Descripción general](#descripción-general)
- [Stack tecnológico](#stack-tecnológico)
- [Arquitectura y patrones de diseño](#arquitectura-y-patrones-de-diseño)
- [Requisitos funcionales implementados](#requisitos-funcionales-implementados)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Configuración y ejecución local](#configuración-y-ejecución-local)
- [Variables de entorno](#variables-de-entorno)
- [Endpoints de la API](#endpoints-de-la-api)
- [Máquina de estados](#máquina-de-estados)
- [Motor de reglas de prioridad](#motor-de-reglas-de-prioridad)
- [Integración con IA (opcional)](#integración-con-ia-opcional)
- [Tests](#tests)
- [Colección Postman](#colección-postman)

---

## Descripción general

El sistema centraliza solicitudes académicas que actualmente se reciben por múltiples canales (CSU, correo, SAC, teléfono, presencial) sin estructura unificada. Provee:

- Registro estructurado con trazabilidad completa
- Clasificación y priorización automática mediante motor de reglas
- Gestión del ciclo de vida con validación de transiciones de estado
- Asignación de responsables con control de roles
- Historial auditable de cada acción
- Asistente de IA opcional para sugerencias de clasificación y resúmenes (no bloquea el sistema si no está disponible)

---

## Stack tecnológico

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3 |
| Seguridad | Spring Security + JWT (jjwt) |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos (producción) | PostgreSQL (Neon) |
| Base de datos (tests) | H2 en memoria |
| Build | Maven |
| IA externa | Groq API (LLaMA 3.3 70B) |
| Documentación API | OpenAPI 3.0 |

---

## Arquitectura y patrones de diseño

El proyecto sigue una **arquitectura en capas** estricta:

```
controller/     →  Recibe HTTP, delega al servicio, devuelve ResponseEntity
service/        →  Lógica de aplicación e interfaces de servicio
domain/         →  Entidades, enums, reglas de negocio, patrones
  ├── entity/
  ├── enums/
  ├── factory/
  ├── observer/
  ├── rules/
  └── validator/
repository/     →  Interfaces JPA
dto/            →  Objetos de transferencia (request / response)
config/         →  Seguridad, JWT, propiedades externas
exception/      →  Manejo global de errores
util/           →  Utilidades (JwtUtil)
```

### Patrones implementados

**Factory** — `SolicitudFactory` centraliza la creación de entidades `SolicitudAcademica` y el mapeo a DTOs de respuesta. Evita que los servicios tengan lógica de construcción duplicada.

**Observer** — `SolicitudObserver` / `HistorialObserver` desacopla la auditoría del flujo principal. Cada cambio de estado, asignación de responsable o cálculo de prioridad notifica automáticamente al observer, que persiste la entrada en el historial sin que el servicio lo gestione directamente.

**Strategy / Rules Engine** — `ReglaPrioridad` define el contrato para cada regla. `MotorReglasPrioridad` las aplica con pesos distintos y calcula una prioridad ponderada con justificación textual. Agregar una nueva regla no requiere modificar el motor.

**State Machine** — `ValidadorTransicionEstado` centraliza en un mapa inmutable todas las transiciones válidas. Cualquier intento de transición no permitida lanza `IllegalStateException` antes de tocar la base de datos.

---

## Requisitos funcionales implementados

| RF | Descripción | Estado |
|---|---|---|
| RF-01 | Registro de solicitudes académicas
| RF-02 | Clasificación por tipo 
| RF-03 | Priorización con motor de reglas 
| RF-04 | Gestión del ciclo de vida 
| RF-05 | Asignación de responsables 
| RF-06 | Historial auditable 
| RF-07 | Consulta con filtros y paginación
| RF-08 | Cierre formal de solicitudes 
| RF-09 | Resúmenes con IA *(opcional)*
| RF-10 | Sugerencia automática de clasificación *(opcional)* 
| RF-11 | Funcionamiento independiente de IA 
| RF-12 | API REST 
| RF-13 | Autorización por roles 

---

## Estructura del proyecto

```
gestion-solicitudes/
├── src/
│   ├── main/
│   │   ├── java/co/edu/uniquindio/gestion_solicitudes/
│   │   │   ├── config/
│   │   │   │   ├── AppConfig.java
│   │   │   │   ├── IaProperties.java
│   │   │   │   ├── JwtAuthFilter.java
│   │   │   │   ├── JwtProperties.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── HistorialController.java
│   │   │   │   └── SolicitudController.java
│   │   │   ├── domain/
│   │   │   │   ├── entity/
│   │   │   │   │   ├── HistorialSolicitud.java
│   │   │   │   │   ├── SolicitudAcademica.java
│   │   │   │   │   └── Usuario.java
│   │   │   │   ├── enums/
│   │   │   │   │   ├── CanalOrigen.java
│   │   │   │   │   ├── EstadoSolicitud.java
│   │   │   │   │   ├── Prioridad.java
│   │   │   │   │   ├── RolUsuario.java
│   │   │   │   │   └── TipoSolicitud.java
│   │   │   │   ├── factory/
│   │   │   │   │   └── SolicitudFactory.java
│   │   │   │   ├── observer/
│   │   │   │   │   ├── HistorialObserver.java
│   │   │   │   │   └── SolicitudObserver.java
│   │   │   │   ├── rules/
│   │   │   │   │   ├── ReglaPrioridad.java
│   │   │   │   │   ├── ResultadoPrioridad.java
│   │   │   │   │   └── impl/
│   │   │   │   │       ├── ReglaPorCanalOrigen.java
│   │   │   │   │       ├── ReglaPorFechaLimite.java
│   │   │   │   │       ├── ReglaPorImpactoAcademico.java
│   │   │   │   │       └── ReglaPorTipoSolicitud.java
│   │   │   │   └── validator/
│   │   │   │       └── ValidadorTransicionEstado.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── CredencialesInvalidasException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── ServicioIANoDisponibleException.java
│   │   │   ├── repository/
│   │   │   │   ├── HistorialRepository.java
│   │   │   │   ├── SolicitudRepository.java
│   │   │   │   └── UsuarioRepository.java
│   │   │   ├── service/
│   │   │   │   ├── AsistenteIA.java
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── HistorialService.java
│   │   │   │   ├── IAService.java
│   │   │   │   ├── MotorReglasPrioridad.java
│   │   │   │   ├── SolicitudService.java
│   │   │   │   └── impl/
│   │   │   │       ├── HistorialServiceImpl.java
│   │   │   │       ├── IAServiceImpl.java
│   │   │   │       └── SolicitudServiceImpl.java
│   │   │   └── util/
│   │   │       └── JwtUtil.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── openapi.yaml
│   └── test/
│       ├── java/co/edu/uniquindio/gestion_solicitudes/
│       │   ├── controller/
│       │   ├── domain/validator/
│       │   ├── repository/
│       │   ├── service/
│       │   └── util/
│       │       └── TestDataFactory.java
│       └── resources/
│           └── application-test.properties
├── PruebasPostman.json
└── pom.xml
```

---

## Configuración y ejecución local

### Prerrequisitos

- Java 17+
- Maven 3.8+
- PostgreSQL (o acceso a una instancia en Neon)

### Pasos

**1. Clonar el repositorio**

```bash
git clone <url-del-repositorio>
cd gestion-solicitudes
```

**2. Crear el archivo `.env`** en la raíz del proyecto con las variables de entorno (ver sección siguiente). Este archivo nunca debe subirse al repositorio.

**3. Compilar y ejecutar**

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080/api`.

**4. Ejecutar los tests**

```bash
mvn test
```

Los tests usan H2 en memoria con el perfil `test`, por lo que no requieren base de datos externa.

---

## Variables de entorno

El archivo `application.properties` lee los valores sensibles desde variables de entorno. Crear un archivo `.env` en la raíz (no subir a Git):

```env
DB_URL=jdbc:postgresql://<host>/neondb?sslmode=require&channel_binding=require
DB_USERNAME=<usuario>
DB_PASSWORD=<contraseña>
JWT_SECRET=<clave-secreta-minimo-32-caracteres>
IA_API_KEY=<clave-groq>
```

En IntelliJ IDEA: **Run/Debug Configurations → Environment variables** → agregar las variables, o instalar el plugin **EnvFile** para leer el `.env` automáticamente.

> **Importante:** si en algún momento estas credenciales fueron subidas a un repositorio público, deben rotarse inmediatamente en Neon y en Groq.

---

## Endpoints de la API

Base URL: `http://localhost:8080/api`

Todos los endpoints excepto `/auth/**` requieren el header:
```
Authorization: Bearer <token>
```

### Autenticación

| Método | Endpoint | Descripción | Roles |
|---|---|---|---|
| POST | `/auth/registro` | Registrar nuevo usuario | Público |
| POST | `/auth/login` | Iniciar sesión y obtener token JWT | Público |

### Solicitudes

| Método | Endpoint | Descripción | Roles |
|---|---|---|---|
| POST | `/solicitudes` | Registrar nueva solicitud | Todos |
| GET | `/solicitudes` | Listar con filtros y paginación | Todos* |
| GET | `/solicitudes/{id}` | Obtener detalle por ID | Todos |
| PUT | `/solicitudes/{id}/clasificar` | Clasificar (REGISTRADA → CLASIFICADA) | ADMINISTRATIVO |
| PUT | `/solicitudes/{id}/priorizar` | Recalcular prioridad | ADMINISTRATIVO |
| PUT | `/solicitudes/{id}/responsable` | Asignar responsable | ADMINISTRATIVO |
| PUT | `/solicitudes/{id}/iniciar-atencion` | Iniciar atención (CLASIFICADA → EN_ATENCION) | DOCENTE, ADMINISTRATIVO |
| PUT | `/solicitudes/{id}/marcar-atendida` | Marcar atendida (EN_ATENCION → ATENDIDA) | DOCENTE, ADMINISTRATIVO |
| PUT | `/solicitudes/{id}/cerrar` | Cerrar solicitud (ATENDIDA → CERRADA) | DOCENTE, ADMINISTRATIVO |
| PUT | `/solicitudes/{id}/cancelar` | Cancelar solicitud | Todos** |

> *Los estudiantes solo ven sus propias solicitudes (filtro automático por rol).
> **Los estudiantes solo pueden cancelar sus propias solicitudes.

### Historial

| Método | Endpoint | Descripción | Roles |
|---|---|---|---|
| GET | `/historial/{solicitudId}` | Consultar historial auditable | Todos |

### Asistente IA *(opcional)*

| Método | Endpoint | Descripción | Roles |
|---|---|---|---|
| GET | `/solicitudes/{id}/sugerencia-ia` | Obtener sugerencia de tipo, prioridad y resumen | Todos |
| PUT | `/solicitudes/{id}/sugerencia-ia/confirmar` | Aplicar o descartar la sugerencia | DOCENTE, ADMINISTRATIVO |

### Parámetros de consulta — `GET /solicitudes`

| Parámetro | Tipo | Descripción |
|---|---|---|
| `estado` | `EstadoSolicitud` | Filtrar por estado |
| `tipo` | `TipoSolicitud` | Filtrar por tipo |
| `prioridad` | `Prioridad` | Filtrar por prioridad |
| `responsableId` | `Long` | Filtrar por responsable |
| `solicitanteId` | `Long` | Filtrar por solicitante (ignorado para estudiantes) |
| `page` | `int` | Página (base 0, default 0) |
| `size` | `int` | Tamaño de página (default 20) |

### Códigos de respuesta

| Código | Significado |
|---|---|
| 200 | OK |
| 201 | Creado |
| 400 | Datos inválidos |
| 401 | Token ausente o inválido |
| 403 | Sin permisos para la operación |
| 404 | Recurso no encontrado |
| 409 | Conflicto (email duplicado, transición inválida, estado terminal) |
| 503 | Servicio de IA no disponible (el sistema sigue operando) |

---

## Máquina de estados

```
REGISTRADA ──→ CLASIFICADA ──→ EN_ATENCION ──→ ATENDIDA ──→ CERRADA
     │
     └──────────────────────────────────────────────────────→ CANCELADA
```

| Transición | Desde | Hacia | Endpoint |
|---|---|---|---|
| T0 | REGISTRADA | CANCELADA | `PUT /cancelar` |
| T1 | REGISTRADA | CLASIFICADA | `PUT /clasificar` |
| T2 | CLASIFICADA | EN_ATENCION | `PUT /iniciar-atencion` |
| T3 | EN_ATENCION | ATENDIDA | `PUT /marcar-atendida` |
| T4 | ATENDIDA | CERRADA | `PUT /cerrar` |

**CERRADA** y **CANCELADA** son estados terminales — ninguna modificación es posible una vez alcanzados.

---

## Motor de reglas de prioridad

El motor evalúa cuatro reglas con pesos distintos y calcula una prioridad ponderada:

| Regla | Peso | Criterio |
|---|---|---|
| `ReglaPorFechaLimite` | 4 | ≤2 días → CRITICA · ≤7 días → ALTA · ≤30 días → MEDIA · resto → BAJA |
| `ReglaPorImpactoAcademico` | 3 | Nivel 5 → CRITICA · 4 → ALTA · 3 → MEDIA · resto → BAJA |
| `ReglaPorTipoSolicitud` | 2 | HOMOLOGACION / CANCELACION → ALTA · REGISTRO / CUPOS → MEDIA · resto → BAJA |
| `ReglaPorCanalOrigen` | 1 | CSU / PRESENCIAL → ALTA · CORREO / SAC → MEDIA · TELEFONO → BAJA |

La prioridad final se obtiene del promedio ponderado:

| Promedio | Prioridad final |
|---|---|
| ≥ 3.5 | CRITICA |
| ≥ 2.5 | ALTA |
| ≥ 1.5 | MEDIA |
| < 1.5 | BAJA |

La respuesta incluye el campo `justificacionPrioridad` con el detalle de cada regla aplicada.

---

## Integración con IA (opcional)

El sistema se integra con la API de **Groq** (modelo LLaMA 3.3 70B) para:

- Sugerir el tipo de solicitud a partir de la descripción libre
- Sugerir la prioridad según el contexto
- Generar un resumen breve del caso para los responsables

### Principios de diseño

- La IA **asiste**, no reemplaza. Toda sugerencia debe ser confirmada o descartada por un usuario humano antes de aplicarse.
- Si la IA no está disponible (clave no configurada, servicio caído, respuesta inválida), el sistema devuelve `503 Service Unavailable` y continúa operando con normalidad — **RF-11 garantizado**.
- La integración se activa/desactiva con la propiedad `ia.habilitada=true/false`.

### Flujo

```
GET /solicitudes/{id}/sugerencia-ia        → obtiene sugerencia (no modifica nada)
PUT /solicitudes/{id}/sugerencia-ia/confirmar  → usuario aplica o descarta
```

---

## Tests

El proyecto incluye tests unitarios y de integración organizados por capa:

```
test/
├── controller/
│   ├── AuthControllerTest
│   ├── HistorialControllerTest
│   ├── SolicitudControllerTest
│   ├── SolicitudControllerClasificarTest
│   ├── SolicitudControllerPriorizarTest
│   ├── SolicitudControllerResponsableTest
│   └── SolicitudControllerTransicionesTest
├── domain/validator/
│   └── ValidadorTransicionEstadoTest
├── repository/                              ← tests de integración con @DataJpaTest + H2
│   ├── SolicitudRepositoryTest
│   ├── HistorialRepositoryTest
│   └── UsuarioRepositoryTest
├── service/
│   ├── AuthServiceTest
│   ├── FlujoCicloVidaSolicitudTest
│   ├── HistorialServiceTest
│   ├── MotorReglasPrioridadTest
│   ├── SolicitudServiceTest
│   ├── SolicitudServiceClasificarTest
│   ├── SolicitudServicePriorizarTest
│   ├── SolicitudServiceResponsableTest
│   └── SolicitudServiceTransicionesTest
└── util/
    └── TestDataFactory                      ← fábrica de datos de prueba compartida
```

Los tests de controlador y servicio usan **Mockito** (`@ExtendWith(MockitoExtension.class)`).
Los tests de repositorio usan **`@DataJpaTest`** con H2 en memoria — verifican que las queries JPQL reales funcionan correctamente.

Ejecutar todos los tests:

```bash
mvn test
```

Ejecutar solo los tests de repositorio:

```bash
mvn test -Dtest="*RepositoryTest"
```

---

## Colección Postman

El archivo `PruebasPostman.json` en la raíz del proyecto contiene una colección completa con:

- Flujos de autenticación para los tres roles (ESTUDIANTE, DOCENTE, ADMINISTRATIVO)
- CRUD de solicitudes con casos de error
- Ciclo de vida completo paso a paso
- Pruebas de seguridad (token inválido, permisos insuficientes)
- Pruebas del asistente IA

Para importar: abrir Postman → **Import** → seleccionar `PruebasPostman.json`.

La variable `base_url` apunta a `http://localhost:8080/api` por defecto. Los tokens se guardan automáticamente en variables de colección al ejecutar los requests de login o registro.

---

## Equipo de desarrollo

Proyecto final — Programación Avanzada  
Programa de Ingeniería de Sistemas y Computación  
Universidad del Quindío