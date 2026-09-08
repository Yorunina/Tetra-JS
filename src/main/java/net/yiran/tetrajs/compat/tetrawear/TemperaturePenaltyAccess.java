package net.yiran.tetrajs.compat.tetrawear;

import se.mickelus.tetrawear.systems.temperature.TemperaturePenalty;

public interface TemperaturePenaltyAccess {
    void tetrajs$setPenaltyOverride(TemperaturePenalty penalty, boolean present);

    boolean tetrajs$hasPenaltyOverride();

    TemperaturePenalty tetrajs$getPenaltyOverride();
}
