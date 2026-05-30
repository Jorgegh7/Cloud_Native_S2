# Learning Platform - Gestión de Almacenamiento con Amazon S3 y Spring Boot

## Descripción

Microservicio Spring Boot que gestiona una plataforma de aprendizaje con inscripciones de estudiantes a cursos. Integra AWS S3 para almacenar y gestionar resúmenes de inscripción como archivos de texto, permitiendo subirlos, descargarlos, modificarlos y borrarlos desde el bucket S3.

## Arquitectura

El proyecto sigue una arquitectura por capas:

```
├── model/                  → Entidades y DTOs
│   ├── Usuario.java        → Estudiantes y profesores
│   ├── Rol.java            → Enum: ESTUDIANTE, PROFESOR
│   ├── Curso.java          → Cursos con profesor asignado
│   ├── Inscripcion.java    → Inscripción de estudiante a cursos
│   └── S3ObjectDto.java    → DTO para representar archivos en S3
│
├── repository/             → Acceso a datos (JPA)
│   ├── UsuarioRepository.java
│   ├── CursoRepository.java
│   └── InscripcionRepository.java
│
├── service/
│   ├── contrato/           → Interfaces (contratos)
│   │   ├── UsuarioService.java
│   │   ├── CursoService.java
│   │   ├── InscripcionService.java
│   │   ├── AwsS3Service.java           → CRUD genérico de archivos en S3
│   │   └── InscripcionS3Service.java   → Gestión de resúmenes en S3
│   │
│   └── impl/               → Implementaciones
│       ├── UsuarioServiceImpl.java
│       ├── CursoServiceImpl.java
│       ├── InscripcionServiceImpl.java
│       ├── AwsS3ServiceImpl.java
│       └── InscripcionS3ServiceImpl.java
│
├── controller/             → Endpoints REST
│   ├── UsuarioController.java
│   ├── CursoController.java
│   ├── InscripcionController.java
│   ├── AwsS3Controller.java           → Endpoints genéricos de S3
│   └── InscripcionS3Controller.java   → Endpoints de resúmenes S3
```

## Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| Spring Boot | 3.3.13 | Framework principal |
| Java | 21 | Lenguaje |
| Spring Cloud AWS | 3.1.1 | Integración con AWS S3 |
| AWS SDK v2 | 2.21.46 | Cliente S3 (auto-configurado) |
| H2 Database | - | Base de datos en memoria |
| Lombok | - | Reducción de código boilerplate |
| Docker | - | Contenedorización |
| GitHub Actions | - | CI/CD pipeline |

## Configuración

### application.properties

```properties
spring.application.name=learningplatform
server.port=8080

# Base de datos H2 en memoria
spring.datasource.url=jdbc:h2:mem:learningPlatformDb
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true

# AWS S3
spring.cloud.aws.region.static=${AWS_REGION}
aws.s3.bucket=mslpbucket
```

### Variables de entorno requeridas

| Variable | Descripción |
|---|---|
| `AWS_ACCESS_KEY_ID` | Access key de AWS |
| `AWS_SECRET_ACCESS_KEY` | Secret key de AWS |
| `AWS_SESSION_TOKEN` | Session token (AWS Academy) |
| `AWS_REGION` | Región de AWS (us-east-1) |

Las credenciales de AWS Academy expiran cada 4 horas y deben actualizarse.

## Endpoints

### Endpoints base (Semana 1)

| Método | URL | Descripción |
|---|---|---|
| POST | `/api/learningplatform/usuarios` | Crear usuario |
| GET | `/api/learningplatform/usuarios` | Listar usuarios |
| POST | `/api/learningplatform/cursos` | Crear curso |
| GET | `/api/learningplatform/cursos` | Listar cursos |
| POST | `/api/learningplatform/inscripciones` | Crear inscripción |
| GET | `/api/learningplatform/inscripciones/{id}` | Buscar inscripción |
| PUT | `/api/learningplatform/inscripciones/{id}` | Actualizar inscripción |
| DELETE | `/api/learningplatform/inscripciones/{id}` | Eliminar inscripción |

### Endpoints S3 - Genérico (pruebas)

| Método | URL | Descripción |
|---|---|---|
| GET | `/s3/test` | Test de conexión (lista buckets) |
| GET | `/s3/{bucket}` | Listar archivos del bucket |
| POST | `/s3/{bucket}/object?key=nombre` | Subir archivo (form-data: file) |
| GET | `/s3/{bucket}/object?key=nombre` | Descargar archivo |
| POST | `/s3/{bucket}/move?sourceKey=x&destKey=y` | Mover/renombrar archivo |
| DELETE | `/s3/{bucket}/object?key=nombre` | Borrar archivo |

### Endpoints S3 - Resumen de Inscripción (principal)

| Método | URL | Descripción |
|---|---|---|
| GET | `/inscripciones/{id}/resumen/generar` | Genera resumen desde BD (sin S3) |
| POST | `/inscripciones/{id}/resumen/subir` | Genera y sube resumen a S3 |
| GET | `/inscripciones/{id}/resumen/descargar` | Descarga resumen desde S3 |
| PUT | `/inscripciones/{id}/resumen/modificar` | Actualiza BD + regenera en S3 |
| DELETE | `/inscripciones/{id}/resumen/borrar` | Elimina resumen de S3 |

## Cómo ejecutar

### Ejecución local

1. Configurar variables de entorno en IntelliJ:
   - Run → Edit Configurations → Environment Variables
   - Agregar: `AWS_ACCESS_KEY_ID=...;AWS_SECRET_ACCESS_KEY=...;AWS_SESSION_TOKEN=...;AWS_REGION=us-east-1`

2. Ejecutar la aplicación desde IntelliJ o terminal:
```bash
mvn clean compile
mvn spring-boot:run
```

3. Probar en Postman: `http://localhost:8080/s3/test`

### Ejecución en EC2 con Docker

1. Construir imagen Docker:
```bash
docker build -t usuario/my-app:latest .
```

2. Ejecutar contenedor con credenciales:
```bash
docker run -d --name my-app -p 8080:8080 \
  -e AWS_ACCESS_KEY_ID=tu-access-key \
  -e AWS_SECRET_ACCESS_KEY=tu-secret-key \
  -e AWS_SESSION_TOKEN=tu-session-token \
  -e AWS_REGION=us-east-1 \
  usuario/my-app:latest
```

3. Verificar logs:
```bash
docker logs my-app
```

4. Probar: `http://<IP-EC2>:8080/s3/test`

## Pipeline CI/CD

El proyecto usa GitHub Actions para automatizar el despliegue:

```
git push → GitHub Actions → Build + Docker → DockerHub → EC2
```

1. **git push**: dispara el pipeline al hacer push a main
2. **GitHub Actions**: compila el proyecto y construye la imagen Docker
3. **DockerHub**: almacena la imagen Docker
4. **EC2**: descarga la imagen y ejecuta el contenedor con las credenciales AWS

### Actualización de credenciales

Cuando las credenciales de AWS Academy expiran:
1. Ir a GitHub → Settings → Secrets and variables → Actions
2. Actualizar `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` y `AWS_SESSION_TOKEN`
3. En Actions → Re-run all jobs (sin necesidad de commit)

## Estructura de archivos en S3

Los resúmenes se organizan en carpetas virtuales por ID de inscripción:

```
mslpbucket/
  ├── 1/
  │   └── resumen_inscripcion.txt
  ├── 2/
  │   └── resumen_inscripcion.txt
  └── 3/
      └── resumen_inscripcion.txt
```

Nota: las carpetas en S3 son virtuales (no existen físicamente). El `/` en el key es solo un separador visual.

## Ejemplo de resumen generado

```
========================================
     RESUMEN DE INSCRIPCIÓN #1
========================================

Estudiante: Jorge Heck
Fecha: 28/05/2026 22:09:30

Cursos inscritos:
  - Backend II

Total a pagar: $50000
========================================
```

## Notas importantes

- **H2 es volátil**: los datos en la BD se pierden al reiniciar la aplicación. Los archivos en S3 persisten.
- **Credenciales AWS Academy**: expiran cada 4 horas, deben actualizarse en GitHub Secrets o en las variables de entorno.
- **S3 no tiene carpetas reales**: las "carpetas" son parte del nombre (key) del archivo. Al borrar el archivo, la carpeta desaparece.
