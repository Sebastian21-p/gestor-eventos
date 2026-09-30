package com.sebs.gestor_eventos;

import org.springframework.boot.SpringApplication;

public class TestGestorEventosApplication {

	public static void main(String[] args) {
		SpringApplication.from(GestorEventosApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
