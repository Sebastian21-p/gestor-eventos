-- Migración inicial: crea la tabla de eventos.
--
-- Convención de Flyway: V<versión>__<descripcion>.sql (dos guiones bajos después de la versión).
-- IMPORTANTE: una migración que ya se ejecutó NO se edita. Flyway guarda su checksum y fallará
-- al arrancar si cambia. Para modificar el esquema se crea una nueva: V2__..., V3__..., etc.

CREATE TABLE eventos (
    -- UUID generado por Hibernate (GenerationType.UUID) al hacer save()
    id              UUID         PRIMARY KEY,
    -- El CHECK impide títulos vacíos o solo con espacios, aunque alguien inserte directo en la BD
    titulo          VARCHAR(120) NOT NULL CHECK (btrim(titulo) <> ''),
    -- TEXT: sin límite de longitud (agenda, requisitos, etc.)
    descripcion     TEXT         NOT NULL,
    -- TIMESTAMPTZ guarda el instante exacto (en UTC); se mapea a OffsetDateTime en Java
    fecha_hora      TIMESTAMPTZ  NOT NULL,
    ubicacion       VARCHAR(255) NOT NULL,
    -- "sub" del JWT del proveedor OAuth (p. ej. "auth0|abc123"). Es texto, no UUID.
    creador_id      VARCHAR(255) NOT NULL,
    -- Valor por defecto en la BD como respaldo; Hibernate también lo llena con @CreationTimestamp
    fecha_creacion  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Índices para las consultas que haremos: eventos por creador y búsqueda/ordenamiento por fecha
CREATE INDEX idx_eventos_creador_id ON eventos (creador_id);
CREATE INDEX idx_eventos_fecha_hora ON eventos (fecha_hora);
