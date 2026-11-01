package com.hackerfinder.backend.usuarios;

import com.hackerfinder.backend.usuarios.controller.UsuarioController;
import com.hackerfinder.backend.usuarios.model.Usuario;
import com.hackerfinder.backend.usuarios.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WebMvcTest(UsuarioController.class)
class UsuariosApplicationTests {

	@Autowired
	private UsuarioController usuarioController;

	@MockitoBean
	private UsuarioService usuarioService;

	@Test
	void contextLoads() {
		assertNotNull(usuarioController);
	}

	@Test
	void testRegister() {
		Usuario nuevoUsuario = new Usuario(null, "newuser", "New User", "new@test.com", "password123");
		Usuario savedUsuario = new Usuario(1L, "newuser", "New User", "new@test.com", "password123");

		when(usuarioService.findByUsername("newuser")).thenReturn(null);
		when(usuarioService.saveUsuario(any(Usuario.class))).thenReturn(savedUsuario);

		ResponseEntity<Usuario> response = usuarioController.register(nuevoUsuario);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(1L, response.getBody().getIdUsuario());
		assertEquals("newuser", response.getBody().getUsername());
	}

	@Test
	void testLogin() {
		Usuario loginRequest = new Usuario();
		loginRequest.setUsername("existinguser");
		loginRequest.setContrasena("password123");

		Usuario existeUsuario = new Usuario(1L, "existinguser", "Existing User", "existing@test.com", "password123");

		when(usuarioService.inicioSesion("existinguser", "password123")).thenReturn(existeUsuario);

		ResponseEntity<Usuario> response = usuarioController.login(loginRequest);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("existinguser", response.getBody().getUsername());
	}

}
