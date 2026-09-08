package net.yiran.tetrajs.kubejs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.world.entity.player.Player;

public class EnergyExhaustEventJS extends EventJS {
    public Player player;
    public double current;
    public double max;
    public String reason;
    public double amount;

    public EnergyExhaustEventJS(Player player, double current, double max, String reason, double amount) {
        this.player = player;
        this.current = current;
        this.max = max;
        this.reason = reason;
        this.amount = amount;
    }

    public Player getPlayer() {
        return player;
    }

    public double getCurrent() {
        return current;
    }

    public double getMax() {
        return max;
    }

    public String getReason() {
        return reason;
    }

    public double getAmount() {
        return amount;
    }
}
