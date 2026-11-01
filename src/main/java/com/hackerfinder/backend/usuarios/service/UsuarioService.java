package com.hackerfinder.backend.usuarios.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.hackerfinder.backend.usuarios.repository.UsuarioRepository;
import java.util.List;
import com.hackerfinder.backend.usuarios.model.Usuario;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Las contraseñas se guardan con hash BCrypt, nunca en texto plano
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario findById(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario saveUsuario(Usuario usuario) {
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        return usuarioRepository.save(usuario);
    }

    public Usuario findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public Usuario inicioSesion(String username, String password) {
        Usuario usuario = usuarioRepository.findByUsername(username);
        if (usuario == null || password == null) {
            return null;
        }
        return passwordEncoder.matches(password, usuario.getContrasena()) ? usuario : null;
    }

    public void delete(Long id) {
        usuarioRepository.deleteById(id);
    }
}
