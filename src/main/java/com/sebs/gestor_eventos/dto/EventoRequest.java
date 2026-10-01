package com.sebs.gestor_eventos.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EventoRequest(
    @NotBlank(message = "El título es obligatorio")
    @Size (max = 120, message = "El título no puede superar los 120 caracteres")
    String titulo,
    @NotBlank (message = "La descripción es obligatoria")
    String descripcion,
    @NotNull(message = "La fecha y hora del evento es obligatoria")
    @Future(message = "La fecha del evento debe ser futura")
    OffsetDateTime fechaHora,
    @NotBlank(message = "La ubicación es obligatoria")
    @Size(max = 255, message = "La ubicación no puede superar 255 caracteres")
    String ubicacion
){}
