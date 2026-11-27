package com.arenacore.billing;

import com.arenacore.billing.domain.TarifaCancha;
import com.arenacore.billing.domain.TarifaImplemento;
import com.arenacore.billing.dto.BillingDtos.*;
import com.arenacore.billing.repository.*;
import com.arenacore.billing.service.BillingCoreService;
import com.arenacore.billing.service.IdempotencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BillingCoreServiceTest {

    @Test
    void shouldConsolidateNocturnalCourtAndEquipmentRental() {
        TarifaCanchaRepository tarifaCanchaRepo = Mockito.mock(TarifaCanchaRepository.class);
        TarifaImplementoRepository tarifaImplementoRepo = Mockito.mock(TarifaImplementoRepository.class);
        LiquidacionRepository liquidacionRepo = Mockito.mock(LiquidacionRepository.class);
        CajaRepository cajaRepo = Mockito.mock(CajaRepository.class);
        SesionCajaRepository sesionCajaRepo = Mockito.mock(SesionCajaRepository.class);
        PagoRepository pagoRepo = Mockito.mock(PagoRepository.class);
        MovimientoCajaRepository movimientoCajaRepo = Mockito.mock(MovimientoCajaRepository.class);
        ComprobanteRepository comprobanteRepo = Mockito.mock(ComprobanteRepository.class);
        IdempotencyService idempotencyService = new IdempotencyService(
                Mockito.mock(IdempotencyRecordRepository.class),
                new ObjectMapper().findAndRegisterModules()
        );

        BillingCoreService service = new BillingCoreService(
                tarifaCanchaRepo, tarifaImplementoRepo, liquidacionRepo, cajaRepo,
                sesionCajaRepo, pagoRepo, movimientoCajaRepo, comprobanteRepo, idempotencyService
        );

        TarifaCancha tNocturna = new TarifaCancha();
        tNocturna.setTipoCancha("FUTBOL_5");
        tNocturna.setFranja("NOCTURNA");
        tNocturna.setValorHoraCop(new BigDecimal("110000.00"));

        TarifaImplemento tBalon = new TarifaImplemento();
        tBalon.setCodigoTarifa("TAR_BALON_PRO_5");
        tBalon.setValorAlquilerCop(new BigDecimal("10000.00"));
        tBalon.setValorReposicionCop(new BigDecimal("120000.00"));

        when(liquidacionRepo.findBySesionJuegoId(any())).thenReturn(Optional.empty());
        when(tarifaCanchaRepo.findByTipoCanchaAndFranjaAndActivaTrue("FUTBOL_5", "NOCTURNA"))
                .thenReturn(Optional.of(tNocturna));
        when(tarifaImplementoRepo.findByCodigoTarifaAndActivaTrue("TAR_BALON_PRO_5"))
                .thenReturn(Optional.of(tBalon));
        when(liquidacionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CrearLiquidacionRequest req = new CrearLiquidacionRequest(
                UUID.randomUUID(),
                "Bombonera Fútbol 5 - Norte",
                "FUTBOL_5",
                0,
                1,
                0,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                List.of(new CargoImplementoRequest("TAR_BALON_PRO_5", "Balón Golty #5", 1, 0, 0))
        );

        LiquidacionResponse res = service.crearLiquidacionConsolidada(req);
        assertEquals(0, new BigDecimal("120000.00").compareTo(res.totalCop()));
        assertEquals("PENDIENTE", res.estado());
    }
}
