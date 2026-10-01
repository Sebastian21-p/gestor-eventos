package com.sebs.gestor_eventos.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/*
 * Entidad que mapea la tabla "eventos" (creada en V1__crear_tabla_eventos.sql).
 *
 * Nombres de columnas: Spring Boot convierte camelCase a snake_case automáticamente
 * (fechaHora -> fecha_hora, creadorId -> creador_id), por eso no hace falta @Column(name = ...).
 *
 * Como ddl-auto = validate, Hibernate compara esta clase con la tabla al arrancar:
 * si un tipo o una columna no coincide, la app no levanta (y eso es bueno, avisa del error temprano).
 *
 * @Getter/@Setter en lugar de @Data: @Data genera equals/hashCode/toString con todos los campos,
 * lo que da problemas con entidades JPA (proxies, relaciones lazy, colecciones). Bien elegido.
 */
@Entity
@Table(name = "eventos")
@Getter
@Setter
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(columnDefinition = "TEXT")
    private String descripcion2;

    @Column(nullable = false)
    private OffsetDateTime fechaHora;

    @Column(nullable = false, length = 255)
    private String ubicacion;

    @Column(nullable = false)
    private String creadorId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private OffsetDateTime fechaCreacion;

}
