package com.sebs.gestor_eventos.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.sebs.gestor_eventos.domain.Evento;

public record EventoResponse(
        UUID id,
        String titulo,
        String descripcion,
        String descripcion2,
        OffsetDateTime fechaHora,
        String ubicacion,
        String creadorId,
        OffsetDateTime fechaCreacion) {

    public static EventoResponse desde(Evento e){
        return new EventoResponse(
                e.getId(),
                e.getTitulo(),
                e.getDescripcion(),
                e.getDescripcion2(),
                e.getFechaHora(),
                e.getUbicacion(),
                e.getCreadorId(),
                e.getFechaCreacion()
        );
    }
}
