# Gestor de Eventos Comunitarios — API REST

API REST para publicar y gestionar eventos locales. Cualquier persona puede consultar los eventos;
para crearlos hace falta autenticarse con **OAuth 2.0 (Auth0)**, y solo el **creador** de un evento
puede modificarlo o eliminarlo.

## Tabla de contenido

- [Stack](#stack)
- [Arquitectura](#arquitectura)
- [Requisitos previos](#requisitos-previos)
- [1. Configurar Auth0](#1-configurar-auth0)
- [2. Instalación y ejecución](#2-instalación-y-ejecución)
- [3. Obtener un token](#3-obtener-un-token)
- [Endpoints](#endpoints)
- [Probar la API](#probar-la-api)
- [Tests](#tests)
- [Decisiones técnicas](#decisiones-técnicas)

## Stack

| Tecnología | Uso |
|---|---|
| Java 25 + Spring Boot 4.1 | Aplicación (Spring Web MVC, Spring Data JPA, Bean Validation) |
| Spring Security — OAuth2 Resource Server | Validación de tokens JWT |
| Auth0 | Proveedor OAuth 2.0 |
| PostgreSQL 17 | Base de datos (en Docker) |
| Flyway | Migraciones del esquema |
| JUnit 5 + Testcontainers | Tests de integración |
| Lombok | Reducción de código repetitivo |

## Arquitectura

Organizada por capas:

```
src/main/java/com/sebs/gestor_eventos/
├── config/        SecurityConfig: reglas de acceso y validación de JWT
├── controller/    EventoController: endpoints REST
├── domain/        Evento: entidad JPA
├── dto/           EventoRequest (entrada + validaciones) / EventoResponse (salida)
├── exceptions/    Excepciones de negocio + GlobalExceptionHandler (respuestas ProblemDetail)
├── repository/    EventoRepository (Spring Data JPA)
└── service/       EventoService: lógica de negocio y validación de propiedad
src/main/resources/
├── application.yml            Configuración (lee variables de entorno / .env)
├── application-example.yml    Plantilla documentada de configuración
└── db/migration/              Migraciones Flyway (V1__crear_tabla_eventos.sql)
```

## Requisitos previos

- **Java 25** (`java -version`)
- **Docker** con Docker Compose (para PostgreSQL)
- Una cuenta gratuita de **[Auth0](https://auth0.com)**

No hace falta instalar Maven: el proyecto incluye el wrapper `./mvnw`.

## 1. Configurar Auth0

1. **Crear la API**: en el panel de Auth0, *Applications → APIs → Create API*.
   - **Name**: `Gestor Eventos API`
   - **Identifier**: `https://gestor-eventos-api` (será el *audience*; no necesita ser una URL real)
   - **Signing Algorithm**: `RS256`
2. **Crear dos aplicaciones Machine to Machine** (*Applications → Applications → Create Application*),
   por ejemplo `Cliente A` y `Cliente B`, para simular **dos usuarios distintos** y probar la
   autorización por propietario.
3. En cada aplicación, pestaña **API Access** → fila *Gestor Eventos API* → *Edit* → habilitar
   **Client Access** (no hace falta asignar permisos).
4. Anotar:
   - el **dominio del tenant** (ej. `dev-xxxx.us.auth0.com`)
   - el **Client ID** y el **Client Secret** de cada aplicación (pestaña *Credentials*)

## 2. Instalación y ejecución

```bash
# 1. Clonar el repositorio
git clone https://github.com/Sebastian21-p/gestor-eventos.git
cd gestor-eventos

# 2. Crear el archivo de variables de entorno
cp .env.example .env
```

Editar `.env` y completar los valores:

| Variable | Descripción | Ejemplo |
|---|---|---|
| `DB_HOST` | Host de PostgreSQL | `localhost` |
| `DB_PORT` | Puerto de PostgreSQL | `5432` |
| `DB_NAME` | Nombre de la base de datos | `gestor_eventos` |
| `DB_USER` | Usuario de la base de datos | `gestor` |
| `DB_PASSWORD` | Contraseña de la base de datos | `gestor_local` |
| `AUTH0_ISSUER` | Dominio del tenant **con `/` final** | `https://dev-xxxx.us.auth0.com/` |
| `AUTH0_AUDIENCE` | Identifier de la API en Auth0 | `https://gestor-eventos-api` |

> El archivo `.env` lo leen tanto Docker Compose como la aplicación, y está en `.gitignore`.
> Todas las claves están documentadas también en
> [`application-example.yml`](src/main/resources/application-example.yml).

```bash
# 3. Levantar PostgreSQL
docker compose up -d

# 4. Iniciar la aplicación (Flyway crea las tablas automáticamente al arrancar)
./mvnw spring-boot:run
```

La API queda disponible en **http://localhost:8080**.

Para detener la base de datos: `docker compose down` (añadir `-v` para borrar también los datos).

## 3. Obtener un token

Las rutas de escritura requieren un **access token** de Auth0 en el header
`Authorization: Bearer <token>`. Con las aplicaciones M2M se obtiene con el flujo
*client credentials*:

```bash
curl -s -X POST https://TU-TENANT.us.auth0.com/oauth/token \
  -H 'Content-Type: application/json' \
  -d '{
    "client_id": "CLIENT_ID",
    "client_secret": "CLIENT_SECRET",
    "audience": "https://gestor-eventos-api",
    "grant_type": "client_credentials"
  }'
```

La respuesta incluye `access_token` (válido 24 h). El claim `sub` del token
(`<client_id>@clients`) identifica al usuario y se guarda como `creadorId` del evento.

## Endpoints

| Método | Ruta | Acceso | Descripción | Respuestas |
|---|---|---|---|---|
| `GET` | `/eventos` | Público | Lista todos los eventos | `200` |
| `GET` | `/eventos/{id}` | Público | Detalle de un evento | `200`, `400`, `404` |
| `POST` | `/eventos` | Autenticado | Crea un evento; el creador se toma del token | `201` + header `Location`, `400`, `401` |
| `PUT` | `/eventos/{id}` | Propietario | Actualiza un evento | `200`, `400`, `401`, `403`, `404` |
| `DELETE` | `/eventos/{id}` | Propietario | Elimina un evento | `204`, `401`, `403`, `404` |

**Códigos de error**

| Código | Cuándo |
|---|---|
| `400 Bad Request` | Datos inválidos en el body o `id` que no es un UUID |
| `401 Unauthorized` | Falta el token, es inválido o expiró |
| `403 Forbidden` | Token válido, pero el usuario no es el creador del evento |
| `404 Not Found` | El evento no existe |

Los errores se devuelven en formato estándar **ProblemDetail (RFC 9457)**:

```json
{
  "type": "about:blank",
  "title": "Forbidden",
  "status": 403,
  "detail": "No tienes permisos para acceder a este evento",
  "instance": "/eventos/4d87377d-f2a4-4d24-938a-c2a278d5f96a"
}
```

### Body de creación y actualización

```json
{
  "titulo": "Taller de Spring Boot",
  "descripcion": "Introducción a APIs REST con Spring",
  "fechaHora": "2026-11-15T18:00:00-05:00",
  "ubicacion": "Biblioteca Central, Cúcuta"
}
```

| Campo | Reglas |
|---|---|
| `titulo` | Obligatorio, no vacío, máximo 120 caracteres |
| `descripcion` | Obligatorio, no vacío |
| `fechaHora` | Obligatorio, fecha futura, formato ISO-8601 con zona horaria (`2026-11-15T18:00:00-05:00`) |
| `ubicacion` | Obligatorio, no vacío, máximo 255 caracteres (lugar físico o enlace virtual) |

`id`, `creadorId` y `fechaCreacion` los asigna el sistema. Las fechas se devuelven en UTC
(`2026-11-15T23:00:00Z` es el mismo instante que `2026-11-15T18:00:00-05:00`).

Un error de validación devuelve el detalle por campo:

```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "La solicitud tiene datos inválidos",
  "errores": {
    "titulo": "El título es obligatorio",
    "fechaHora": "La fecha del evento debe ser futura"
  }
}
```

## Probar la API

### Con Postman

En la carpeta [`postman/`](postman/) están la colección y una plantilla del environment.

1. Importar ambos archivos en Postman (*Import*).
2. En el environment completar `auth0Domain`, `audience`, `clientIdA`, `clientSecretA`,
   `clientIdB` y `clientSecretB`, y seleccionarlo como environment activo.
3. Ejecutar las peticiones de **0 - Auth**: guardan automáticamente `tokenA` y `tokenB`.
4. Ejecutar el resto en orden.
### Con curl

```bash
TOKEN_A="<access_token del Cliente A>"
TOKEN_B="<access_token del Cliente B>"

# Listar (público)
curl -i http://localhost:8080/eventos

# Crear sin token -> 401
curl -i -X POST http://localhost:8080/eventos -H 'Content-Type: application/json' \
  -d '{"titulo":"Taller","descripcion":"Intro","fechaHora":"2026-11-15T18:00:00-05:00","ubicacion":"Cúcuta"}'

# Crear con el Cliente A -> 201 (copiar el "id" de la respuesta)
curl -i -X POST http://localhost:8080/eventos -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN_A" \
  -d '{"titulo":"Taller","descripcion":"Intro","fechaHora":"2026-11-15T18:00:00-05:00","ubicacion":"Cúcuta"}'

# Editar el evento de A con el Cliente B -> 403
curl -i -X PUT http://localhost:8080/eventos/<ID> -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN_B" \
  -d '{"titulo":"Editado","descripcion":"Intro","fechaHora":"2026-11-15T18:00:00-05:00","ubicacion":"Cúcuta"}'

# Eliminar con el Cliente A (propietario) -> 204
curl -i -X DELETE http://localhost:8080/eventos/<ID> -H "Authorization: Bearer $TOKEN_A"
```

## Tests

```bash
./mvnw test
```

Los tests de integración usan **Testcontainers**: levantan un PostgreSQL 17 temporal, por lo que
requieren Docker en ejecución (no usan la base de datos de `docker compose`).

