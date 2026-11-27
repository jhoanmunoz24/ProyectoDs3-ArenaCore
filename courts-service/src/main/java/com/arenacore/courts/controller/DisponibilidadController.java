package com.arenacore.courts.controller;

import com.arenacore.courts.dto.CourtsDtos.DisponibilidadDiaResponse;
import com.arenacore.courts.service.CourtsCoreService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/disponibilidad")
public class DisponibilidadController {

    private final CourtsCoreService courtsService;

    public DisponibilidadController(CourtsCoreService courtsService) {
        this.courtsService = courtsService;
    }

    @GetMapping
    public ResponseEntity<DisponibilidadDiaResponse> consultar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate target = fecha != null ? fecha : LocalDate.now(CourtsCoreService.BOGOTA_ZONE);
        return ResponseEntity.ok(courtsService.consultarDisponibilidad(target));
    }
}
