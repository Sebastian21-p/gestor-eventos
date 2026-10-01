package com.sebs.gestor_eventos.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sebs.gestor_eventos.domain.Evento;

public interface EventoRepository extends JpaRepository<Evento, UUID> {
    
}
