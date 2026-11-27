package com.arenacore.equipment;

import com.arenacore.equipment.domain.Implemento;
import com.arenacore.equipment.dto.EquipmentDtos.*;
import com.arenacore.equipment.repository.*;
import com.arenacore.equipment.service.EquipmentCoreService;
import com.arenacore.equipment.service.IdempotencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class EquipmentCoreServiceTest {

    @Test
    void shouldDecrementStockOnLoanAndMarkDamageOnReturn() {
        CategoriaImplementoRepository catRepo = Mockito.mock(CategoriaImplementoRepository.class);
        ImplementoRepository impRepo = Mockito.mock(ImplementoRepository.class);
        PrestamoRepository prestamoRepo = Mockito.mock(PrestamoRepository.class);
        MovimientoInventarioRepository kardexRepo = Mockito.mock(MovimientoInventarioRepository.class);
        IdempotencyService idempotencyService = new IdempotencyService(
                Mockito.mock(IdempotencyRecordRepository.class),
                new ObjectMapper().findAndRegisterModules()
        );

        EquipmentCoreService service = new EquipmentCoreService(catRepo, impRepo, prestamoRepo, kardexRepo, idempotencyService);

        UUID impId = UUID.randomUUID();
        Implemento petos = new Implemento();
        petos.setId(impId);
        petos.setNombre("Juego de 10 Petos");
        petos.setCodigoTarifa("TAR_JUEGO_PETOS");
        petos.setStockTotal(4);
        petos.setStockDisponible(4);
        petos.setActivo(true);

        when(impRepo.findByIdForUpdate(impId)).thenReturn(Optional.of(petos));
        when(impRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(prestamoRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistrarPrestamoRequest req = new RegistrarPrestamoRequest(
                UUID.randomUUID(), "Entrega previa", List.of(new ItemPrestamoRequest(impId, 1))
        );
        ResponseEntity<String> res = service.registrarPrestamoLocal("idem-1", "POST:/api/v1/prestamos", req, UUID.randomUUID());

        assertEquals(201, res.getStatusCode().value());
        assertEquals(3, petos.getStockDisponible());
    }
}
