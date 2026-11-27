package com.arenacore.courts.controller;

import com.arenacore.courts.dto.CourtsDtos.SedeCreateRequest;
import com.arenacore.courts.dto.CourtsDtos.SedeResponse;
import com.arenacore.courts.service.CourtsCoreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sedes")
public class ComplejoSetupController {

    private final CourtsCoreService courtsService;

    public ComplejoSetupController(CourtsCoreService courtsService) {
        this.courtsService = courtsService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<SedeResponse> crearSede(@Valid @RequestBody SedeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courtsService.crearSede(request));
    }

    @GetMapping
    public ResponseEntity<List<SedeResponse>> listarSedes() {
        return ResponseEntity.ok(courtsService.listarSedes());
    }
}
