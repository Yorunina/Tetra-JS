package net.yiran.tetrajs.kubejs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.world.entity.player.Player;

public class TemperatureUpdateEventJS extends EventJS {
    public Player player;
    public float current;
    public float target;
    public float biomeTemperature;
    public float timeMultiplier;
    public int zone;
    public int heatResistance;
    public int coldResistance;
    public String penalty;
    private boolean penaltyModified;

    public TemperatureUpdateEventJS(
            Player player,
            float current,
            float target,
            float biomeTemperature,
            float timeMultiplier,
            int zone,
            int heatResistance,
            int coldResistance,
            String penalty
    ) {
        this.player = player;
        this.current = current;
        this.target = target;
        this.biomeTemperature = biomeTemperature;
        this.timeMultiplier = timeMultiplier;
        this.zone = zone;
        this.heatResistance = heatResistance;
        this.coldResistance = coldResistance;
        this.penalty = penalty;
    }

    public Player getPlayer() {
        return player;
    }

    public float getCurrent() {
        return current;
    }

    public float getTarget() {
        return target;
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public float getBiomeTemperature() {
        return biomeTemperature;
    }

    public float getTimeMultiplier() {
        return timeMultiplier;
    }

    public int getZone() {
        return zone;
    }

    public int getHeatResistance() {
        return heatResistance;
    }

    public int getColdResistance() {
        return coldResistance;
    }

    public String getPenalty() {
        return penalty;
    }

    public void setPenalty(String penalty) {
        this.penalty = penalty;
        this.penaltyModified = true;
    }

    public void clearPenalty() {
        this.penalty = null;
        this.penaltyModified = true;
    }

    public boolean isPenaltyModified() {
        return penaltyModified;
    }
}
