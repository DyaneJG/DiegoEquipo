# Documentación del sistema-rm-backend

Este documento contiene el código fuente completo de las clases desarrolladas en el proyecto, la configuración principal y la explicación de cómo montar el sistema.

## Resumen del proyecto

- Aplicación Spring Boot 3.5.16 con Java 21.
- Paquetes principales: auth, catalogo, clientes, notificaciones, pagos, pedidos, repartidores, rutas, tracking, shared.
- Usa PostgreSQL con Flyway, Redis, JWT, Spring Security, Hibernate Spatial y OpenAPI.
- Clase principal: `src/main/java/mx/edomex/rutas/SistemaRmBackendApplication.java`.

## Configuración del proyecto

### build.gradle.kts
```kotlin
plugins {
	java
	id("org.springframework.boot") version "3.5.16"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "mx.edomex.rutas"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
	implementation("org.hibernate:hibernate-spatial:6.5.2.Final")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
```

### application.yml
```yaml
﻿spring:
  application:
    name: sistema-rm-backend
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:sistema_rm}
    username: ${DB_USER:rm_admin}
    password: ${DB_PASSWORD:rm_dev_local}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
    open-in-view: false
  flyway:
    enabled: true
    locations: classpath:db/migration
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

server:
  port: 8080

jwt:
  private-key: ${JWT_PRIVATE_KEY_PATH:file:./keys/private_key.pem}
  public-key: ${JWT_PUBLIC_KEY_PATH:file:./keys/public_key.pem}
  expiration-minutes: ${JWT_EXPIRATION_MINUTES:15}
  refresh-expiration-days: ${JWT_REFRESH_EXPIRATION_DAYS:7}
```

### Migración inicial de base de datos
```sql
﻿-- ==========================================================
-- EXTENSIONES
-- ==========================================================
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================================================
-- DOMINIO: AUTENTICACION Y RBAC
-- ==========================================================
CREATE TABLE roles (
    id     SMALLINT PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE
);

INSERT INTO roles (id, nombre) VALUES
    (1, 'admin'),
    (2, 'supervisor'),
    (3, 'repartidor'),
    (4, 'cliente');

CREATE TABLE usuarios (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre_completo VARCHAR(150) NOT NULL,
    correo          VARCHAR(150) NOT NULL UNIQUE,
    telefono        VARCHAR(20),
    password_hash   VARCHAR(255) NOT NULL,
    rol_id          SMALLINT NOT NULL REFERENCES roles(id),
    mfa_habilitado  BOOLEAN NOT NULL DEFAULT FALSE,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_usuarios_rol ON usuarios(rol_id);

-- ==========================================================
-- DOMINIO: CLIENTES
-- ==========================================================
CREATE TABLE clientes (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    creado_en  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ==========================================================
-- DOMINIO: ZONAS (geocercas)
-- ==========================================================
CREATE TABLE zonas (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre      VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    poligono    GEOMETRY(Polygon, 4326) NOT NULL,
    color_hex   CHAR(7),
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_zonas_poligono ON zonas USING GIST(poligono);

-- ==========================================================
-- DOMINIO: DIRECCIONES CLIENTE (con zona automática)
-- ==========================================================
CREATE TABLE direcciones_cliente (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cliente_id        UUID NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
    etiqueta          VARCHAR(50),
    calle_numero      VARCHAR(200) NOT NULL,
    colonia           VARCHAR(120),
    municipio         VARCHAR(80) NOT NULL,
    codigo_postal     VARCHAR(10) NOT NULL,
    referencias       TEXT,
    ubicacion         GEOMETRY(Point, 4326) NOT NULL,
    zona_id           UUID REFERENCES zonas(id) ON DELETE SET NULL,
    es_predeterminada BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_direcciones_cliente ON direcciones_cliente(cliente_id);
CREATE INDEX idx_direcciones_ubicacion ON direcciones_cliente USING GIST(ubicacion);
CREATE INDEX idx_direcciones_zona ON direcciones_cliente(zona_id);

-- Trigger para asignar zona automáticamente
CREATE OR REPLACE FUNCTION asignar_zona_automatica()
RETURNS TRIGGER AS $$
BEGIN
    SELECT id INTO NEW.zona_id
    FROM zonas
    WHERE ST_Within(NEW.ubicacion, poligono)
    LIMIT 1;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_asignar_zona
BEFORE INSERT OR UPDATE OF ubicacion ON direcciones_cliente
FOR EACH ROW
EXECUTE FUNCTION asignar_zona_automatica();

-- ==========================================================
-- DOMINIO: CATALOGO (productos e inventario separados)
-- ==========================================================
CREATE TABLE categorias (
    id     UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE productos (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sku             VARCHAR(50) NOT NULL UNIQUE,
    nombre          VARCHAR(200) NOT NULL,
    descripcion     TEXT,
    precio_unitario NUMERIC(12,2) NOT NULL CHECK (precio_unitario >= 0),
    categoria_id    UUID REFERENCES categorias(id),
    imagen_url      VARCHAR(500),
    unidad_medida   VARCHAR(20) NOT NULL DEFAULT 'pieza',
    peso_kg         NUMERIC(8,3),
    volumen_m3      NUMERIC(8,4),
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_productos_categoria ON productos(categoria_id);

CREATE TABLE inventario (
    id                  UUID PRIMARY KEY REFERENCES productos(id) ON DELETE CASCADE,
    stock_actual        INTEGER NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    stock_minimo        INTEGER NOT NULL DEFAULT 5,
    stock_reservado     INTEGER NOT NULL DEFAULT 0 CHECK (stock_reservado >= 0),
    reserva_expira_at   TIMESTAMPTZ,
    ultima_actualizacion TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_inventario_stock_bajo ON inventario(id) WHERE stock_actual <= stock_minimo;

-- ==========================================================
-- DOMINIO: TASAS DE IMPUESTOS (históricas)
-- ==========================================================
CREATE TABLE tasas_impuestos (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre        VARCHAR(50) NOT NULL,
    porcentaje    NUMERIC(5,4) NOT NULL CHECK (porcentaje >= 0),
    vigencia_desde DATE NOT NULL,
    vigencia_hasta DATE,
    creado_en     TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO tasas_impuestos (nombre, porcentaje, vigencia_desde)
VALUES ('IVA General', 0.1600, '2020-01-01');

-- ==========================================================
-- DOMINIO: VEHICULOS
-- ==========================================================
CREATE TABLE vehiculos (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tipo                VARCHAR(50) NOT NULL,
    placa               VARCHAR(15) NOT NULL UNIQUE,
    capacidad_peso_kg   NUMERIC(8,2) NOT NULL,
    capacidad_volumen_m3 NUMERIC(8,3) NOT NULL,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ==========================================================
-- DOMINIO: REPARTIDORES
-- ==========================================================
CREATE TABLE repartidores (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id          UUID NOT NULL REFERENCES usuarios(id),
    vehiculo_actual_id  UUID REFERENCES vehiculos(id) ON DELETE SET NULL,
    zona_preferente_id  UUID REFERENCES zonas(id) ON DELETE SET NULL,
    estado              VARCHAR(20) NOT NULL DEFAULT 'FUERA_DE_LINEA',
    fcm_token           TEXT,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_repartidores_usuario ON repartidores(usuario_id);

-- ==========================================================
-- DOMINIO: PEDIDOS
-- ==========================================================
CREATE TABLE pedidos (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    numero_pedido           VARCHAR(25) NOT NULL UNIQUE,
    cliente_id              UUID NOT NULL REFERENCES clientes(id),
    direccion_entrega_id    UUID NOT NULL REFERENCES direcciones_cliente(id),
    estado                  VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    subtotal                NUMERIC(12,2) NOT NULL,
    iva                     NUMERIC(12,2) NOT NULL,
    cargo_entrega           NUMERIC(8,2) NOT NULL DEFAULT 0,
    total                   NUMERIC(12,2) NOT NULL,
    notas                   TEXT,
    ventana_inicio          TIME,
    ventana_fin             TIME,
    fecha_entrega_programada DATE,
    fecha_entrega_real      TIMESTAMPTZ,
    cancelado_motivo        TEXT,
    creado_en               TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_pedidos_cliente ON pedidos(cliente_id);
CREATE INDEX idx_pedidos_estado ON pedidos(estado);
CREATE INDEX idx_pedidos_fecha_programada ON pedidos(fecha_entrega_programada);

CREATE TABLE pedido_detalle (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pedido_id           UUID NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    producto_id         UUID NOT NULL REFERENCES productos(id),
    cantidad            INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario     NUMERIC(12,2) NOT NULL,
    tasa_iva_aplicada   NUMERIC(5,4) NOT NULL,
    iva_unitario        NUMERIC(12,2) NOT NULL,
    subtotal_linea      NUMERIC(12,2) NOT NULL,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_pedido_detalle_pedido ON pedido_detalle(pedido_id);
CREATE INDEX idx_pedido_detalle_producto ON pedido_detalle(producto_id);

-- ==========================================================
-- DOMINIO: PAGOS
-- ==========================================================
CREATE TABLE metodos_pago (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    codigo        VARCHAR(30) NOT NULL UNIQUE,
    nombre        VARCHAR(100) NOT NULL,
    requiere_token BOOLEAN NOT NULL DEFAULT FALSE,
    activo        BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO metodos_pago (codigo, nombre) VALUES
    ('efectivo', 'Efectivo'),
    ('stripe', 'Tarjeta (Stripe)'),
    ('mercadopago', 'Tarjeta (Mercado Pago)'),
    ('spei', 'Transferencia SPEI'),
    ('codi', 'CoDi');

CREATE TABLE pagos (
    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pedido_id          UUID NOT NULL REFERENCES pedidos(id) ON DELETE RESTRICT,
    metodo_pago_id     UUID NOT NULL REFERENCES metodos_pago(id),
    proveedor          VARCHAR(30),
    estado             VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    monto              NUMERIC(12,2) NOT NULL,
    referencia_externa VARCHAR(200),
    qr_codigo          TEXT,
    qr_expira_at       TIMESTAMPTZ,
    pagado_at          TIMESTAMPTZ,
    creado_en          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_pagos_pedido ON pagos(pedido_id);
CREATE INDEX idx_pagos_estado ON pagos(estado);

-- ==========================================================
-- DOMINIO: AUDITORIA DE ESTADOS DE PEDIDO
-- ==========================================================
CREATE TABLE pedido_estados_log (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pedido_id         UUID NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    estado_anterior   VARCHAR(20),
    estado_nuevo      VARCHAR(20) NOT NULL,
    usuario_id        UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    repartidor_id     UUID REFERENCES repartidores(id) ON DELETE SET NULL,
    tipo_actor        VARCHAR(20) NOT NULL CHECK (tipo_actor IN ('admin', 'repartidor', 'sistema', 'cliente')),
    notas             TEXT,
    creado_en         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_pedidos_log_pedido ON pedido_estados_log(pedido_id);
CREATE INDEX idx_pedidos_log_creado ON pedido_estados_log(creado_en);

-- ==========================================================
-- DOMINIO: RUTAS
-- ==========================================================
CREATE TABLE rutas (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    numero_ruta         VARCHAR(20) NOT NULL UNIQUE,
    repartidor_id       UUID NOT NULL REFERENCES repartidores(id),
    vehiculo_id         UUID NOT NULL REFERENCES vehiculos(id),
    zona_id             UUID NOT NULL REFERENCES zonas(id),
    fecha_ruta          DATE NOT NULL,
    estado              VARCHAR(20) NOT NULL DEFAULT 'PLANIFICADA',
    distancia_total_km  NUMERIC(8,2),
    tiempo_estimado_min INTEGER,
    total_paradas       SMALLINT NOT NULL DEFAULT 0,
    geometria_ruta      GEOMETRY(LineString, 4326),
    inicio_real_at      TIMESTAMPTZ,
    fin_real_at         TIMESTAMPTZ,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_rutas_repartidor ON rutas(repartidor_id);
CREATE INDEX idx_rutas_fecha ON rutas(fecha_ruta);
CREATE INDEX idx_rutas_estado ON rutas(estado);

-- ==========================================================
-- DOMINIO: RUTA PARADAS
-- ==========================================================
CREATE TABLE ruta_paradas (
    id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ruta_id               UUID NOT NULL REFERENCES rutas(id) ON DELETE CASCADE,
    pedido_id             UUID NOT NULL REFERENCES pedidos(id) ON DELETE RESTRICT,
    secuencia             INTEGER NOT NULL CHECK (secuencia > 0),
    eta                   TIMESTAMPTZ,
    distancia_anterior_km NUMERIC(6,2),
    estado                VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    creado_en             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_ruta_paradas_pedido ON ruta_paradas(pedido_id);
CREATE INDEX idx_ruta_paradas_ruta ON ruta_paradas(ruta_id, secuencia);

-- ==========================================================
-- DOMINIO: ENTREGAS
-- ==========================================================
CREATE TABLE entregas (
    id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    parada_id             UUID NOT NULL UNIQUE REFERENCES ruta_paradas(id) ON DELETE CASCADE,
    checkin_at            TIMESTAMPTZ,
    checkin_coordenadas   GEOMETRY(Point, 4326),
    checkout_at           TIMESTAMPTZ,
    entregado_at          TIMESTAMPTZ,
    confirmacion_tipo     VARCHAR(20) CHECK (confirmacion_tipo IN ('firma_digital', 'pin', 'foto')),
    pin_generado          CHAR(4),
    firma_url             TEXT,
    resultado             VARCHAR(30) CHECK (resultado IN ('entregado', 'no_entregado', 'parcial')),
    creado_en             TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_entregas_parada ON entregas(parada_id);
CREATE INDEX idx_entregas_checkin_geom ON entregas USING GIST(checkin_coordenadas);

-- ==========================================================
-- DOMINIO: EVIDENCIAS DE ENTREGA
-- ==========================================================
CREATE TABLE entrega_evidencias (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entrega_id    UUID NOT NULL REFERENCES entregas(id) ON DELETE CASCADE,
    tipo          VARCHAR(30) NOT NULL CHECK (tipo IN ('entrega', 'incidencia', 'checkin', 'otro')),
    url           TEXT NOT NULL,
    coordenadas   GEOMETRY(Point, 4326),
    taken_at      TIMESTAMPTZ NOT NULL,
    creado_en     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_evidencias_entrega ON entrega_evidencias(entrega_id);

-- ==========================================================
-- DOMINIO: INCIDENCIAS
-- ==========================================================
CREATE TABLE incidencias (
    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entrega_id         UUID NOT NULL REFERENCES entregas(id) ON DELETE RESTRICT,
    repartidor_id      UUID NOT NULL REFERENCES repartidores(id) ON DELETE RESTRICT,
    admin_id           UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    tipo               VARCHAR(50) NOT NULL,
    descripcion        TEXT NOT NULL,
    estado_resolucion  VARCHAR(20) NOT NULL DEFAULT 'pendiente',
    resolucion_notas   TEXT,
    resuelto_at        TIMESTAMPTZ,
    creado_en          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_incidencias_entrega ON incidencias(entrega_id);

-- ==========================================================
-- DOMINIO: TRACKING DE UBICACIONES (particionado por mes)
-- ==========================================================
CREATE TABLE tracking_ubicaciones (
    id            UUID NOT NULL DEFAULT uuid_generate_v4(),
    repartidor_id UUID NOT NULL REFERENCES repartidores(id) ON DELETE CASCADE,
    ubicacion     GEOMETRY(Point, 4326) NOT NULL,
    precision_metros NUMERIC(6,2),
    velocidad_kmh NUMERIC(6,2),
    rumbo_grados  NUMERIC(5,2),
    creado_en     TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (id, creado_en)
) PARTITION BY RANGE (creado_en);

CREATE TABLE tracking_ubicaciones_2026_07 PARTITION OF tracking_ubicaciones
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');
CREATE TABLE tracking_ubicaciones_2026_08 PARTITION OF tracking_ubicaciones
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');
CREATE TABLE tracking_ubicaciones_2026_09 PARTITION OF tracking_ubicaciones
    FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');
CREATE TABLE tracking_ubicaciones_2026_10 PARTITION OF tracking_ubicaciones
    FOR VALUES FROM ('2026-10-01') TO ('2026-11-01');

CREATE INDEX idx_tracking_repartidor ON tracking_ubicaciones(repartidor_id);
CREATE INDEX idx_tracking_ubicacion ON tracking_ubicaciones USING GIST(ubicacion);
CREATE INDEX idx_tracking_creado ON tracking_ubicaciones(creado_en DESC);

-- ==========================================================
-- DOMINIO: NOTIFICACIONES
-- ==========================================================
CREATE TABLE notificaciones (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id        UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    pedido_id         UUID REFERENCES pedidos(id) ON DELETE SET NULL,
    canal             VARCHAR(20) NOT NULL CHECK (canal IN ('push', 'email', 'sms', 'whatsapp')),
    titulo            VARCHAR(200) NOT NULL,
    cuerpo            TEXT NOT NULL,
    estado            VARCHAR(20) NOT NULL DEFAULT 'pendiente' CHECK (estado IN ('pendiente', 'enviado', 'fallido', 'leido')),
    enviado_at        TIMESTAMPTZ,
    error_detalle     TEXT,
    creado_en         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notificaciones_usuario ON notificaciones(usuario_id);
CREATE INDEX idx_notificaciones_pedido ON notificaciones(pedido_id) WHERE pedido_id IS NOT NULL;
CREATE INDEX idx_notificaciones_estado ON notificaciones(estado);

-- ==========================================================
-- VISTA UTIL: pedidos con ultimo estado
-- ==========================================================
CREATE VIEW v_pedidos_actuales AS
SELECT 
    p.id,
    p.numero_pedido,
    u.nombre_completo AS cliente,
    p.estado,
    p.total,
    p.fecha_entrega_programada,
    p.creado_en,
    (SELECT creado_en FROM pedido_estados_log WHERE pedido_id = p.id ORDER BY creado_en DESC LIMIT 1) AS ultimo_cambio
FROM pedidos p
JOIN clientes c ON p.cliente_id = c.id
JOIN usuarios u ON c.usuario_id = u.id;

-- ==========================================================
-- FIN DEL SCRIPT
-- ==========================================================
```

## Explicación del módulo `auth`

El módulo `auth` implementa la autenticación basada en JWT y refresh tokens, con registro de usuarios y validación de credenciales.

### Controlador
- `AuthController.java`: expone los endpoints de autenticación:
  - `POST /api/auth/register`: crea un nuevo usuario.
  - `POST /api/auth/login`: valida credenciales y retorna tokens.
  - `POST /api/auth/refresh`: renueva el token de acceso usando refresh token.

### DTOs
- `LoginRequest`: solicita `email` y `password` para iniciar sesión.
- `LoginResponse`: devuelve `accessToken`, `refreshToken` y `expiraEnSegundos`.
- `RefreshRequest`: envía `refreshToken` para renovación.
- `RegisterRequest`: envía `email`, `password` y `rolNombre`; valida email y contraseña de al menos 8 caracteres.
- `RegisterResponse`: responde con `id`, `email` y `rol`.

### Modelos
- `Rol`: entidad en la tabla `roles` con `id` y `nombre`.
- `Usuario`: entidad en la tabla `usuarios`, extiende `BaseEntity`, y contiene `email`, `passwordHash`, `rol` y `activo`.

### Repositorios
- `RolRepository`: busca roles por nombre con `findByNombre(String nombre)`.
- `UsuarioRepository`: busca usuarios activos por email y verifica si un email ya existe.

### Servicios
- `AuthService`: lógica principal de autenticación:
  - `register`: crea usuario con rol, codifica contraseña y guarda en DB.
  - `login`: valida credenciales, genera JWT y refresh token.
  - `refresh`: valida refresh token en Redis y genera un nuevo JWT.
- `JwtService`: genera JWT con RS256 usando llave privada, establece subject como email y claim `rol`.
- `RefreshTokenService`: administra refresh tokens en Redis con caducidad y permite validarlos o revocarlos.

### Configuración relacionada
- `application.yml` define la conexión a PostgreSQL, Redis, Flyway y parámetros JWT (llaves, expiración de acceso y expiración de refresh token).

### Flujo de autenticación
1. `POST /api/auth/register` crea usuario y asigna rol.
2. `POST /api/auth/login` valida usuario y contraseña, retorna tokens.
3. `POST /api/auth/refresh` renueva el token de acceso usando el refresh token almacenado en Redis.

## Clases Java y código fuente

## src/main/java/mx/edomex/rutas/auth/controller/AuthController.java
```java
package mx.edomex.rutas.auth.controller;

import jakarta.validation.Valid;
import mx.edomex.rutas.auth.dto.*;
import mx.edomex.rutas.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }
}
```

## src/main/java/mx/edomex/rutas/auth/dto/LoginRequest.java
```java
package mx.edomex.rutas.auth.dto;

public record LoginRequest(String email, String password) {}
```

## src/main/java/mx/edomex/rutas/auth/dto/LoginResponse.java
```java
package mx.edomex.rutas.auth.dto;

public record LoginResponse(String accessToken, String refreshToken, long expiraEnSegundos) {}
```

## src/main/java/mx/edomex/rutas/auth/dto/RefreshRequest.java
```java
package mx.edomex.rutas.auth.dto;

public record RefreshRequest(String refreshToken) {}
```

## src/main/java/mx/edomex/rutas/auth/dto/RegisterRequest.java
```java
package mx.edomex.rutas.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String password,
    @NotBlank String rolNombre
) {}
```

## src/main/java/mx/edomex/rutas/auth/dto/RegisterResponse.java
```java
package mx.edomex.rutas.auth.dto;

public record RegisterResponse(Long id, String email, String rol) {}
```

## src/main/java/mx/edomex/rutas/auth/model/Rol.java
```java
package mx.edomex.rutas.auth.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
```

## src/main/java/mx/edomex/rutas/auth/model/Usuario.java
```java
package mx.edomex.rutas.auth.model;

import jakarta.persistence.*;
import mx.edomex.rutas.shared.audit.BaseEntity;

@Entity
@Table(name = "usuarios")
public class Usuario extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo = true;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
```

## src/main/java/mx/edomex/rutas/auth/package-info.java
```java
/**
 * Gestión de autenticación y autorización de usuarios, roles y tokens JWT RS256.
 *
 * Dueño del dominio: jacobo
 *
 * REGLA: repository y model de este paquete son de uso interno.
 * Otros dominios solo pueden llamar a las clases de service (interfaces
 * públicas). Nunca importar repository/model de aquí desde otro dominio.
 */
package mx.edomex.rutas.auth;
```

## src/main/java/mx/edomex/rutas/auth/repository/RolRepository.java
```java
package mx.edomex.rutas.auth.repository;

import mx.edomex.rutas.auth.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre);
}
```

## src/main/java/mx/edomex/rutas/auth/repository/UsuarioRepository.java
```java
package mx.edomex.rutas.auth.repository;

import mx.edomex.rutas.auth.model.Usuario;
import mx.edomex.rutas.shared.audit.BaseAuditRepository;

import java.util.Optional;

public interface UsuarioRepository extends BaseAuditRepository<Usuario, Long> {
    Optional<Usuario> findByEmailAndActivoTrue(String email);
    boolean existsByEmail(String email);
}
```

## src/main/java/mx/edomex/rutas/auth/service/AuthService.java
```java
package mx.edomex.rutas.auth.service;

import mx.edomex.rutas.auth.dto.*;
import mx.edomex.rutas.auth.model.Rol;
import mx.edomex.rutas.auth.model.Usuario;
import mx.edomex.rutas.auth.repository.RolRepository;
import mx.edomex.rutas.auth.repository.UsuarioRepository;
import mx.edomex.rutas.shared.exception.DominioException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                        PasswordEncoder passwordEncoder, JwtService jwtService,
                        RefreshTokenService refreshTokenService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public RegisterResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new DominioException("Ya existe un usuario con ese email", HttpStatus.CONFLICT);
        }

        Rol rol = rolRepository.findByNombre(request.rolNombre())
            .orElseThrow(() -> new DominioException(
                "Rol no válido: " + request.rolNombre(), HttpStatus.BAD_REQUEST));

        Usuario usuario = new Usuario();
        usuario.setEmail(request.email());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setRol(rol);
        usuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(usuario);

        return new RegisterResponse(guardado.getId(), guardado.getEmail(), guardado.getRol().getNombre());
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailAndActivoTrue(request.email())
            .orElseThrow(() -> new DominioException("Credenciales inválidas", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new DominioException("Credenciales inválidas", HttpStatus.UNAUTHORIZED);
        }

        try {
            String accessToken = jwtService.generarToken(usuario.getEmail(), usuario.getRol().getNombre());
            String refreshToken = refreshTokenService.generar(usuario.getEmail());
            return new LoginResponse(accessToken, refreshToken, 900);
        } catch (Exception e) {
            throw new DominioException("Error generando token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public LoginResponse refresh(RefreshRequest request) {
        String email = refreshTokenService.obtenerEmail(request.refreshToken())
            .orElseThrow(() -> new DominioException("Refresh token inválido o expirado", HttpStatus.UNAUTHORIZED));

        Usuario usuario = usuarioRepository.findByEmailAndActivoTrue(email)
            .orElseThrow(() -> new DominioException("Usuario no encontrado", HttpStatus.UNAUTHORIZED));

        try {
            String nuevoAccessToken = jwtService.generarToken(usuario.getEmail(), usuario.getRol().getNombre());
            return new LoginResponse(nuevoAccessToken, request.refreshToken(), 900);
        } catch (Exception e) {
            throw new DominioException("Error generando token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
```

## src/main/java/mx/edomex/rutas/auth/service/JwtService.java
```java
package mx.edomex.rutas.auth.service;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.private-key}")
    private Resource privateKeyResource;

    @Value("${jwt.expiration-minutes}")
    private long expiracionMinutos;

    public String generarToken(String email, String rol) throws Exception {
        RSAPrivateKey privateKey = cargarLlavePrivada();
        Instant ahora = Instant.now();
        return Jwts.builder()
            .subject(email)
            .claim("rol", rol)
            .issuedAt(Date.from(ahora))
            .expiration(Date.from(ahora.plus(expiracionMinutos, ChronoUnit.MINUTES)))
            .signWith(privateKey, Jwts.SIG.RS256)
            .compact();
    }

    private RSAPrivateKey cargarLlavePrivada() throws Exception {
        String contenido = new String(privateKeyResource.getInputStream().readAllBytes())
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
        byte[] bytes = Base64.getDecoder().decode(contenido);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA")
            .generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }
}
```

## src/main/java/mx/edomex/rutas/auth/service/RefreshTokenService.java
```java
package mx.edomex.rutas.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final String PREFIX = "refresh_token:";

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh-expiration-days}")
    private long expiracionDias;

    public RefreshTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generar(String email) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(PREFIX + token, email, Duration.ofDays(expiracionDias));
        return token;
    }

    public Optional<String> obtenerEmail(String refreshToken) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(PREFIX + refreshToken));
    }

    public void revocar(String refreshToken) {
        redisTemplate.delete(PREFIX + refreshToken);
    }
}
```

## src/main/java/mx/edomex/rutas/catalogo/package-info.java
```java
/**
 * Gestión del catálogo de productos, categorías e inventario.
 *
 * Dueño del dominio: Armando
 *
 * REGLA: repository y model de este paquete son de uso interno.
 * Otros dominios solo pueden llamar a las clases de service (interfaces
 * públicas). Nunca importar repository/model de aquí desde otro dominio.
 */
package mx.edomex.rutas.catalogo;
```

## src/main/java/mx/edomex/rutas/clientes/package-info.java
```java
/**
 * Gestión de clientes y sus direcciones de entrega.
 *
 * Dueño del dominio: Armando
 *
 * REGLA: repository y model de este paquete son de uso interno.
 * Otros dominios solo pueden llamar a las clases de service (interfaces
 * públicas). Nunca importar repository/model de aquí desde otro dominio.
 */
package mx.edomex.rutas.clientes;
```

## src/main/java/mx/edomex/rutas/notificaciones/package-info.java
```java
/**
 * Envío de notificaciones push, WhatsApp/SMS y correo.
 *
 * Dueño del dominio: Ricardo
 *
 * REGLA: repository y model de este paquete son de uso interno.
 * Otros dominios solo pueden llamar a las clases de service (interfaces
 * públicas). Nunca importar repository/model de aquí desde otro dominio.
 */
package mx.edomex.rutas.notificaciones;
```

## src/main/java/mx/edomex/rutas/pagos/package-info.java
```java
/**
 * Integración con pasarelas de pago y métodos de cobro.
 *
 * Dueño del dominio: Jacobo
 *
 * REGLA: repository y model de este paquete son de uso interno.
 * Otros dominios solo pueden llamar a las clases de service (interfaces
 * públicas). Nunca importar repository/model de aquí desde otro dominio.
 */
package mx.edomex.rutas.pagos;
```

## src/main/java/mx/edomex/rutas/pedidos/package-info.java
```java
/**
 * Gestión del ciclo de vida de pedidos, detalle y log de estados.
 *
 * Dueño del dominio: Armando
 *
 * REGLA: repository y model de este paquete son de uso interno.
 * Otros dominios solo pueden llamar a las clases de service (interfaces
 * públicas). Nunca importar repository/model de aquí desde otro dominio.
 */
package mx.edomex.rutas.pedidos;
```

## src/main/java/mx/edomex/rutas/repartidores/package-info.java
```java
/**
 * Gestión de repartidores y vehículos.
 *
 * Dueño del dominio: Nicola
 *
 * REGLA: repository y model de este paquete son de uso interno.
 * Otros dominios solo pueden llamar a las clases de service (interfaces
 * públicas). Nunca importar repository/model de aquí desde otro dominio.
 */
package mx.edomex.rutas.repartidores;
```

## src/main/java/mx/edomex/rutas/rutas/package-info.java
```java
/**
 * Planificación de rutas, zonas geográficas y optimización VRP.
 *
 * Dueño del dominio: Ricardo
 *
 * REGLA: repository y model de este paquete son de uso interno.
 * Otros dominios solo pueden llamar a las clases de service (interfaces
 * públicas). Nunca importar repository/model de aquí desde otro dominio.
 */
package mx.edomex.rutas.rutas;
```

## src/main/java/mx/edomex/rutas/shared/audit/BaseAuditRepository.java
```java
package mx.edomex.rutas.shared.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.time.LocalDateTime;
import java.util.List;

@NoRepositoryBean
public interface BaseAuditRepository<T, ID> extends JpaRepository<T, ID> {
    List<T> findByCreadoEnBetween(LocalDateTime desde, LocalDateTime hasta);
}
```

## src/main/java/mx/edomex/rutas/shared/audit/BaseEntity.java
```java
package mx.edomex.rutas.shared.audit;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @LastModifiedDate
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    public Long getId() { return id; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
}
```

## src/main/java/mx/edomex/rutas/shared/config/SecurityConfig.java
```java
package mx.edomex.rutas.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${jwt.public-key}")
    private Resource publicKeyResource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())));
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        try {
            String contenido = new String(publicKeyResource.getInputStream().readAllBytes())
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
            byte[] bytes = Base64.getDecoder().decode(contenido);
            RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(bytes));
            return NimbusJwtDecoder.withPublicKey(publicKey).build();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo crear el JwtDecoder", ex);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## src/main/java/mx/edomex/rutas/shared/exception/DominioException.java
```java
package mx.edomex.rutas.shared.exception;

import org.springframework.http.HttpStatus;

public class DominioException extends RuntimeException {
    private final HttpStatus status;

    public DominioException(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
```

## src/main/java/mx/edomex/rutas/shared/exception/GlobalExceptionHandler.java
```java
package mx.edomex.rutas.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DominioException.class)
    public ResponseEntity<Map<String, Object>> handleDominio(DominioException ex) {
        return ResponseEntity.status(ex.getStatus()).body(cuerpo(ex.getMessage(), ex.getStatus()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cuerpo(mensaje, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(cuerpo("Ocurrió un error inesperado", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private Map<String, Object> cuerpo(String mensaje, HttpStatus status) {
        return Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", status.value(),
            "error", status.getReasonPhrase(),
            "mensaje", mensaje
        );
    }
}
```

## src/main/java/mx/edomex/rutas/shared/package-info.java
```java
/**
 * Infraestructura transversal compartida por todos los dominios:
 * seguridad, manejo de excepciones, auditoría, cache y eventos.
 *
 * REGLA: los demás dominios pueden depender de este paquete,
 * pero shared NUNCA debe depender de ningún dominio de negocio.
 */
package mx.edomex.rutas.shared;
```

## src/main/java/mx/edomex/rutas/SistemaRmBackendApplication.java
```java
package mx.edomex.rutas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SistemaRmBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemaRmBackendApplication.class, args);
	}

}
```

## src/main/java/mx/edomex/rutas/tracking/package-info.java
```java
/**
 * Seguimiento en tiempo real de entregas, evidencias, incidencias y ubicación GPS.
 *
 * Dueño del dominio: Nicola
 *
 * REGLA: repository y model de este paquete son de uso interno.
 * Otros dominios solo pueden llamar a las clases de service (interfaces
 * públicas). Nunca importar repository/model de aquí desde otro dominio.
 */
package mx.edomex.rutas.tracking;
```

## src/test/java/mx/edomex/rutas/architecture/ArchitectureTest.java
```java
package mx.edomex.rutas.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static final String BASE = "mx.edomex.rutas";

    private static JavaClasses clases;

    private static final String[] DOMINIOS = {
        "auth", "clientes", "catalogo", "pedidos", "pagos",
        "rutas", "repartidores", "tracking", "notificaciones"
    };

    @BeforeAll
    static void importar() {
        clases = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE);
    }

    @Test
    void ningun_dominio_debe_acceder_repository_de_otro_dominio() {
        for (String dominio : DOMINIOS) {
            String paquete = BASE + "." + dominio;
            ArchRule regla = noClasses()
                .that().resideOutsideOfPackage(paquete + "..")
                .should().accessClassesThat().resideInAPackage(paquete + ".repository..")
                .because("solo el propio dominio '%s' puede usar su repository".formatted(dominio))
                .allowEmptyShould(true);
            regla.check(clases);
        }
    }

    @Test
    void ningun_dominio_debe_acceder_model_de_otro_dominio() {
        for (String dominio : DOMINIOS) {
            String paquete = BASE + "." + dominio;
            ArchRule regla = noClasses()
                .that().resideOutsideOfPackage(paquete + "..")
                .should().accessClassesThat().resideInAPackage(paquete + ".model..")
                .because("solo el propio dominio '%s' puede usar sus entities".formatted(dominio))
                .allowEmptyShould(true);
            regla.check(clases);
        }
    }

    @Test
    void shared_no_debe_depender_de_ningun_dominio_de_negocio() {
        String[] paquetesDominios = new String[DOMINIOS.length];
        for (int i = 0; i < DOMINIOS.length; i++) {
            paquetesDominios[i] = BASE + "." + DOMINIOS[i] + "..";
        }
        ArchRule regla = noClasses()
            .that().resideInAPackage(BASE + ".shared..")
            .should().dependOnClassesThat().resideInAnyPackage(paquetesDominios)
            .because("shared es infraestructura transversal, nunca debe conocer un dominio de negocio");
        regla.check(clases);
    }
}
```

## src/test/java/mx/edomex/rutas/SistemaRmBackendApplicationTests.java
```java
package mx.edomex.rutas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SistemaRmBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
```
