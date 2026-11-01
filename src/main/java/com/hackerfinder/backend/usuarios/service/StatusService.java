package com.hackerfinder.backend.usuarios.service;

import com.hackerfinder.backend.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class StatusService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Map<String, Object> verificarEstadoSistema() {
        Map<String, Object> respuesta = new HashMap<>();
        
        respuesta.put("aplicacion", "HackerFinder Backend");
        respuesta.put("version", "1.0.0");
        respuesta.put("hora_servidor", LocalDateTime.now());

        try {
            long totalUsuarios = usuarioRepository.count();
            
            respuesta.put("base_de_datos", "CONECTADA");
            respuesta.put("total_usuarios", totalUsuarios);
            respuesta.put("estado", "OPERATIVO");
            
        } catch (Exception e) {
            respuesta.put("base_de_datos", "ERROR DE CONEXIÓN");
            respuesta.put("error_detalle", e.getMessage());
            respuesta.put("estado", "DEGRADADO");
        }

        return respuesta;
    }
}