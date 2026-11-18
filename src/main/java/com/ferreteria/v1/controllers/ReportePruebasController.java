package com.ferreteria.v1.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ferreteria.v1.services.ReportePruebasService;

@RestController
@RequestMapping("/api/reportes/pruebas")
public class ReportePruebasController {

    private final ReportePruebasService reportePruebasService;

    public ReportePruebasController(ReportePruebasService reportePruebasService) {
        this.reportePruebasService = reportePruebasService;
    }

    @PostMapping("/ejecutar")
    public ResponseEntity<Map<String, Object>> ejecutar() {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(reportePruebasService.ejecutar());
    }

    @GetMapping("/estado")
    public ResponseEntity<Map<String, Object>> estado() {
        return ResponseEntity.ok(reportePruebasService.estado());
    }
}
