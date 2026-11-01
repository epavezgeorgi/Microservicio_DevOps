package com.hackerfinder.backend.usuarios.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.hackerfinder.backend.usuarios.model.Usuario;
import com.hackerfinder.backend.usuarios.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void saveUsuario_ShouldHashPassword() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        Usuario nuevo = new Usuario(null, "juan", "Juan", "juan@test.com", "secreta123");

        Usuario guardado = usuarioService.saveUsuario(nuevo);

        assertNotEquals("secreta123", guardado.getContrasena());
        assertTrue(guardado.getContrasena().startsWith("$2"), "Debe ser un hash BCrypt");
    }

    @Test
    void inicioSesion_ShouldReturnUser_WhenPasswordMatches() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        Usuario guardado = usuarioService.saveUsuario(new Usuario(1L, "juan", "Juan", "juan@test.com", "secreta123"));
        when(usuarioRepository.findByUsername("juan")).thenReturn(guardado);

        assertNotNull(usuarioService.inicioSesion("juan", "secreta123"));
        assertNull(usuarioService.inicioSesion("juan", "incorrecta"));
        assertNull(usuarioService.inicioSesion("juan", null));
    }

    @Test
    void inicioSesion_ShouldReturnNull_WhenUserDoesNotExist() {
        when(usuarioRepository.findByUsername("nadie")).thenReturn(null);

        assertNull(usuarioService.inicioSesion("nadie", "x"));
    }

    @Test
    void findMethods_ShouldDelegateToRepository() {
        Usuario u = new Usuario(1L, "juan", "Juan", "juan@test.com", "hash");
        when(usuarioRepository.findAll()).thenReturn(List.of(u));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());
        when(usuarioRepository.findByUsername("juan")).thenReturn(u);

        assertEquals(1, usuarioService.getAllUsuarios().size());
        assertEquals(u, usuarioService.findById(1L));
        assertNull(usuarioService.findById(2L));
        assertEquals(u, usuarioService.findByUsername("juan"));
    }

    @Test
    void delete_ShouldCallRepository() {
        usuarioService.delete(1L);
        verify(usuarioRepository).deleteById(1L);
    }
}
