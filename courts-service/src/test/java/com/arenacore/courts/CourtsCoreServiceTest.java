package com.arenacore.courts;

import com.arenacore.courts.service.CourtsCoreService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CourtsCoreServiceTest {

    @Test
    void shouldSplitDiurnalAndNocturnalHoursAcrossBoundary() {
        int[] split = CourtsCoreService.calcularHorasDiurnasYNocturnas(17, 2);
        assertArrayEquals(new int[]{1, 1}, split);

        int[] onlyNocturnal = CourtsCoreService.calcularHorasDiurnasYNocturnas(20, 1);
        assertArrayEquals(new int[]{0, 1}, onlyNocturnal);
    }

    @Test
    void shouldRespectTenMinuteGracePeriodBeforeChargingExtraHour() {
        Instant scheduledEnd = Instant.parse("2026-11-26T23:00:00Z");
        assertEquals(0, CourtsCoreService.calcularHorasAdicionalesPorGracia(
                scheduledEnd, scheduledEnd.plus(10, ChronoUnit.MINUTES)));
        assertEquals(1, CourtsCoreService.calcularHorasAdicionalesPorGracia(
                scheduledEnd, scheduledEnd.plus(11, ChronoUnit.MINUTES)));
    }
}
