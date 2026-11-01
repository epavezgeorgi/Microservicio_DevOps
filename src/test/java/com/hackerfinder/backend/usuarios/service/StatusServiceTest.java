package com.hackerfinder.backend.usuarios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.hackerfinder.backend.usuarios.repository.UsuarioRepository;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatusServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private StatusService statusService;

    @Test
    void estado_ShouldBeOperativo_WhenDatabaseResponds() {
        when(usuarioRepository.count()).thenReturn(5L);

        Map<String, Object> estado = statusService.verificarEstadoSistema();

        assertEquals("OPERATIVO", estado.get("estado"));
        assertEquals(5L, estado.get("total_usuarios"));
    }

    @Test
    void estado_ShouldBeDegradado_WhenDatabaseFails() {
        when(usuarioRepository.count()).thenThrow(new RuntimeException("sin conexión"));

        Map<String, Object> estado = statusService.verificarEstadoSistema();

        assertEquals("DEGRADADO", estado.get("estado"));
        assertEquals("ERROR DE CONEXIÓN", estado.get("base_de_datos"));
    }
}
