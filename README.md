# FinCore Platform

Plataforma de inteligencia financiera para PYMEs. Este proyecto tiene dos modulos grandes: el Core (transacciones, proyecciones, alertas) y la Infraestructura (autenticacion, reportes, notificaciones).

## Modulo de Infraestructura

Lo que hice en este modulo:

- **Autenticacion y multi-tenant**: Las empresas se registran como tenants. Los usuarios se registran, verifican su email y pueden pertenecer a varias empresas con roles distintos (OWNER, ADMIN, ANALYST).
- **Seguridad JWT**: Access tokens de 15 minutos y refresh tokens de 7 dias. El tenant_id va dentro del JWT para aislar los datos.
- **API Publica**: Sistemas externos se conectan con API Keys. Tiene rate limiting de 100 requests por minuto y se registra cada llamada.
- **Reportes automaticos**: PDFs mensuales con resumen, balance y graficos. Quartz los genera solito el dia 1 de cada mes. El usuario tambien puede pedir un reporte cuando quiera.
- **Notificaciones**: Escucha eventos de Kafka. Las alertas mandan email y las anomalias mandan email mas webhook con reintentos.

## Tecnologias

Java 21, Spring Boot 3.4, PostgreSQL, Flyway, Spring Security, JWT (jjwt), Quartz, Apache Kafka, iText 8, Bucket4j, Swagger/OpenAPI, JUnit 5, Mockito.

## Como levantar el proyecto

### Necesitas

1. Java 21 ([descargar](https://adoptium.net/))
2. PostgreSQL ([descargar](https://www.postgresql.org/download/) o con Docker)
3. Maven (o usa el wrapper `mvnw`)

Kafka es opcional, sin el las notificaciones no se reciben pero la app funciona igual.

### Pasos

```bash
git clone https://github.com/SebastianMontes-Dev/Fincore-platform.git
cd Fincore-platform
```

Crea la base de datos en PostgreSQL:
```sql
CREATE DATABASE fincore;
```

Variables de entorno que puedes configurar:

| Variable | Que es | Default |
|----------|--------|---------|
| DB_URL | URL de la BD | jdbc:postgresql://localhost:5432/fincore |
| DB_USER | Usuario BD | fincore |
| DB_PASSWORD | Password BD | fincore |
| JWT_SECRET | Clave para firmar tokens | (cambiala en produccion) |
| MAIL_HOST | Servidor SMTP | smtp.gmail.com |
| MAIL_PORT | Puerto SMTP | 587 |
| MAIL_USERNAME | Tu correo | - |
| MAIL_PASSWORD | Password del correo | - |

Arranca:
```bash
mvn spring-boot:run
```

La API corre en `http://localhost:8080` y el Swagger en `http://localhost:8080/swagger-ui.html`.

## Endpoints principales

### Autenticacion
| Metodo | Ruta | Que hace |
|--------|------|----------|
| POST | /api/v1/usuarios/registro | Registrar usuario |
| POST | /api/v1/auth/login | Iniciar sesion (devuelve JWT) |
| POST | /api/v1/auth/refrescar | Renovar access token |
| GET | /api/v1/auth/verificar?token=xxx | Verificar email |
| POST | /api/v1/auth/cambiar-empresa | Cambiar de empresa activa |
| POST | /api/v1/auth/cerrar-sesion | Cerrar sesion |

### Empresas
| Metodo | Ruta | Que hace |
|--------|------|----------|
| POST | /api/v1/empresas/registro | Registrar empresa |
| GET | /api/v1/empresas/{id} | Ver datos de empresa |
| POST | /api/v1/empresas/{id}/invitar | Invitar usuario |

### API Keys
| Metodo | Ruta | Que hace |
|--------|------|----------|
| POST | /api/v1/empresas/{id}/api-keys | Generar API Key |
| GET | /api/v1/empresas/{id}/api-keys | Listar API Keys |
| DELETE | /api/v1/empresas/{id}/api-keys/{kid} | Revocar |

### API Publica
| Metodo | Ruta | Que hace |
|--------|------|----------|
| POST | /api/v1/publico/transacciones | Registrar transaccion externa |
| GET | /api/v1/publico/ping | Probar conexion |

### Reportes
| Metodo | Ruta | Que hace |
|--------|------|----------|
| POST | /api/v1/reportes/{empresaId}?mes=5&anio=2025 | Pedir reporte manual |
| GET | /api/v1/reportes/empresa/{empresaId} | Listar reportes |
| GET | /api/v1/reportes/{id}/descargar | Bajar PDF |

### Notificaciones
| Metodo | Ruta | Que hace |
|--------|------|----------|
| GET | /api/v1/notificaciones/usuario/{id} | Ver notificaciones |
| POST | /api/v1/empresas/{id}/webhook | Configurar webhook |
| GET | /api/v1/empresas/{id}/webhook | Ver config webhook |

## Estructura del proyecto

```
src/main/java/com/fincore/platform/
  FincorePlatformApplication.java
  core/                           -> Modulo Core (a construir despues)
  infrastructure/
    auth/
      controller/  -> AuthController, ApiKeyController, PublicoController, DashboardController
      domain/      -> Usuario, RefreshToken, ApiKey, LogApi, RolEmpresa
      dto/         -> Requests y responses de auth
      repository/  -> Repositorios JPA
      security/    -> JWT, filtros, @UsuarioActual
      service/     -> UsuarioService, ApiKeyService
    tenant/
      controller/  -> EmpresaController
      domain/      -> Empresa, UsuarioEmpresa
      dto/         -> Requests y responses de tenant
      repository/
      service/     -> EmpresaService
    report/
      controller/  -> ReporteController
      domain/      -> Reporte
      dto/
      repository/
      scheduler/   -> Quartz Job y Config
      service/     -> ReporteService
    notification/
      controller/  -> NotificacionController
      domain/      -> Notificacion, WebhookConfig
      dto/
      kafka/       -> KafkaConsumidor
      repository/
      service/     -> NotificacionService
    common/
      config/      -> WebConfig, OpenApi, Mail, Kafka
      dto/         -> MensajeResponse
      exception/   -> GlobalExceptionHandler
```

## Probar con Postman

En la carpeta `docs/` esta la coleccion de Postman con todos los endpoints listos para importar y probar.

## Flujo tipico

1. Registrar la empresa: `POST /api/v1/empresas/registro`
2. Registrar al dueno: `POST /api/v1/usuarios/registro`
3. Verificar email con el link que llega
4. Login: `POST /api/v1/auth/login` (guardar el access_token)
5. Usar el token en los headers: `Authorization: Bearer <token>`
6. El dueno invita a mas usuarios a su empresa
