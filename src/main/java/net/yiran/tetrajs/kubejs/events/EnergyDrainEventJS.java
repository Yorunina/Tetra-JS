package net.yiran.tetrajs.kubejs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.world.entity.player.Player;

public class EnergyDrainEventJS extends EventJS {
    public Player player;
    public double amount;
    public String reason;
    public double current;
    public double max;
    public boolean exhausted;

    public EnergyDrainEventJS(Player player, double amount, String reason, double current, double max, boolean exhausted) {
        this.player = player;
        this.amount = amount;
        this.reason = reason;
        this.current = current;
        this.max = max;
        this.exhausted = exhausted;
    }

    public Player getPlayer() {
        return player;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public double getCurrent() {
        return current;
    }

    public double getMax() {
        return max;
    }

    public boolean isExhausted() {
        return exhausted;
    }
}
