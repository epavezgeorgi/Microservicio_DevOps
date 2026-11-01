package com.hackerfinder.backend.usuarios.controller;

import com.hackerfinder.backend.usuarios.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    @Autowired
    private StatusService statusService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> checkStatus() {
        Map<String, Object> estado = statusService.verificarEstadoSistema();
        return ResponseEntity.ok(estado);
    }
}