CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE empresas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    direccion TEXT,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP
);

CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email_verificado BOOLEAN NOT NULL DEFAULT FALSE,
    token_verificacion VARCHAR(100),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP
);

CREATE TABLE usuarios_empresas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    empresa_id UUID NOT NULL REFERENCES empresas(id),
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('OWNER', 'ADMIN', 'ANALYST')),
    fecha_union TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(usuario_id, empresa_id)
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    token VARCHAR(500) NOT NULL UNIQUE,
    fecha_expiracion TIMESTAMP NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    revocado BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE api_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresas(id),
    creado_por UUID NOT NULL REFERENCES usuarios(id),
    nombre VARCHAR(100) NOT NULL,
    clave_hash VARCHAR(255) NOT NULL UNIQUE,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_revocacion TIMESTAMP
);

CREATE TABLE logs_api (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    api_key_id UUID REFERENCES api_keys(id),
    endpoint VARCHAR(255) NOT NULL,
    metodo VARCHAR(10) NOT NULL,
    cuerpo_peticion TEXT,
    codigo_respuesta INTEGER NOT NULL,
    direccion_ip VARCHAR(45),
    fecha_peticion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE reportes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresas(id),
    solicitado_por UUID REFERENCES usuarios(id),
    mes INTEGER NOT NULL,
    anio INTEGER NOT NULL,
    ruta_archivo VARCHAR(500),
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('PROGRAMADO', 'MANUAL')),
    fecha_generacion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE configuracion_webhook (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL UNIQUE REFERENCES empresas(id),
    url_webhook VARCHAR(500) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE notificaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    empresa_id UUID NOT NULL REFERENCES empresas(id),
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('ALERTA', 'ANOMALIA')),
    mensaje TEXT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ENVIADO',
    fecha_envio TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_empresas_email ON empresas(email);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_api_keys_hash ON api_keys(clave_hash);
CREATE INDEX idx_reportes_empresa ON reportes(empresa_id);
CREATE INDEX idx_notificaciones_usuario ON notificaciones(usuario_id);
CREATE INDEX idx_logs_api_key ON logs_api(api_key_id);
