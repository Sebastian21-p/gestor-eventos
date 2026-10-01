package com.sebs.gestor_eventos.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.sebs.gestor_eventos.dto.EventoRequest;
import com.sebs.gestor_eventos.dto.EventoResponse;
import com.sebs.gestor_eventos.service.EventoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    private static final String USUARIO_PRUEBA = "usuario_ejemplo";

    @GetMapping
    public List<EventoResponse> listarEventos() {
        return eventoService.listar();
    }

    @GetMapping("/{id}")
    public EventoResponse obtenerEventoPorId(@PathVariable UUID id) {
        return eventoService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<EventoResponse> crearEvento(@Valid @RequestBody EventoRequest request) {
        var creado = eventoService.crear(request, USUARIO_PRUEBA);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(uri).body(creado);
    }

    @PutMapping("/{id}")
    public EventoResponse actualizarEvento(@PathVariable UUID id, @Valid @RequestBody EventoRequest request) {
        return eventoService.actualizar(id, request, USUARIO_PRUEBA);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEvento(@PathVariable UUID id) {
        eventoService.eliminar(id, USUARIO_PRUEBA);
        return ResponseEntity.noContent().build();
    }

}
