package com.hackerfinder.backend.usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.hackerfinder.backend.usuarios.model.Usuario;
import com.hackerfinder.backend.usuarios.service.UsuarioService;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(1L, "testuser", "Test User", "test@test.com", "testpassword123");
    }

    @Test
    void register_ShouldReturnCreated_WhenUserDoesNotExist() throws Exception {
        when(usuarioService.findByUsername("testuser")).thenReturn(null);
        when(usuarioService.saveUsuario(any(Usuario.class))).thenReturn(usuario);

        String userJson = "{\"username\":\"testuser\", \"nombreReal\":\"Test User\", \"email\":\"test@test.com\", \"contrasena\":\"testpassword123\"}";

        mockMvc.perform(post("/api/usuarios/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                // la contraseña nunca debe viajar en la respuesta
                .andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    void register_ShouldReturnConflict_WhenUserExists() throws Exception {
        when(usuarioService.findByUsername("testuser")).thenReturn(usuario);

        String userJson = "{\"username\":\"testuser\", \"nombreReal\":\"Test User\", \"email\":\"test@test.com\", \"contrasena\":\"x\"}";

        mockMvc.perform(post("/api/usuarios/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isConflict());
    }

    @Test
    void login_ShouldReturnOk_WhenCredentialsAreCorrect() throws Exception {
        when(usuarioService.inicioSesion("testuser", "testpassword123")).thenReturn(usuario);

        String loginJson = "{\"username\":\"testuser\", \"contrasena\":\"testpassword123\"}";

        mockMvc.perform(post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenCredentialsAreWrong() throws Exception {
        when(usuarioService.inicioSesion("testuser", "mala")).thenReturn(null);

        String loginJson = "{\"username\":\"testuser\", \"contrasena\":\"mala\"}";

        mockMvc.perform(post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_ShouldReturnList() throws Exception {
        when(usuarioService.getAllUsuarios()).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    void getAll_ShouldReturnNoContent_WhenEmpty() throws Exception {
        when(usuarioService.getAllUsuarios()).thenReturn(List.of());

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isNoContent());
    }

    @Test
    void findById_ShouldReturnUser_OrNotFound() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(usuario);
        when(usuarioService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(1));
        mockMvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByUsername_ShouldReturnUser_OrNotFound() throws Exception {
        when(usuarioService.findByUsername("testuser")).thenReturn(usuario);
        when(usuarioService.findByUsername("nadie")).thenReturn(null);

        mockMvc.perform(get("/api/usuarios/username/testuser"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/usuarios/username/nadie"))
                .andExpect(status().isNotFound());
    }
}
