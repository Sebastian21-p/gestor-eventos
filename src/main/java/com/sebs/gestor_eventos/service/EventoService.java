package com.sebs.gestor_eventos.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sebs.gestor_eventos.domain.Evento;
import com.sebs.gestor_eventos.dto.EventoRequest;
import com.sebs.gestor_eventos.dto.EventoResponse;
import com.sebs.gestor_eventos.exceptions.AccesoDenegadoException;
import com.sebs.gestor_eventos.exceptions.RecursoNoEncontradoException;
import com.sebs.gestor_eventos.repository.EventoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;

    @Transactional(readOnly = true)
    public List<EventoResponse> listar() {
        return eventoRepository.findAll().stream()
                .map(EventoResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventoResponse obtenerPorId(UUID id) {
        return eventoRepository.findById(id)
                .map(EventoResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado con id: " + id));
    }

    @Transactional
    public EventoResponse crear(EventoRequest req, String creadorId) {
        var evento = new Evento();
        copiarDatos(req, evento);
        evento.setCreadorId(creadorId);
        var eventoGuardado = eventoRepository.save(evento);
        return EventoResponse.desde(eventoGuardado);
    }

    @Transactional
    public EventoResponse actualizar(UUID id, EventoRequest req, String usuarioId) {
        var evento = buscarComoPropietario(id, usuarioId);
        copiarDatos(req, evento);
        return EventoResponse.desde(eventoRepository.save(evento));
    }

    @Transactional
    public void eliminar(UUID id, String usuarioId) {
        var evento = buscarComoPropietario(id, usuarioId);
        eventoRepository.delete(evento);
    }

    private void copiarDatos(EventoRequest req, Evento evento) {
        evento.setTitulo(req.titulo());
        evento.setDescripcion(req.descripcion());
        evento.setFechaHora(req.fechaHora());
        evento.setUbicacion(req.ubicacion());
    }

    private Evento buscarComoPropietario(UUID id, String usuarioId) {
        var evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado" + id));
        if (!evento.getCreadorId().equals(usuarioId)) {
            throw new AccesoDenegadoException("No tienes permisos para acceder a este evento");
        }
        return evento;
    }

}
