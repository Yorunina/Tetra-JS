package net.yiran.tetrajs.kubejs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.world.entity.player.Player;

public class DodgeEventJS extends EventJS {
    public Player player;
    public String direction;
    public double strength;
    public double energyCost;
    public double saturationCost;

    public DodgeEventJS(Player player, String direction, double strength, double energyCost, double saturationCost) {
        this.player = player;
        this.direction = direction;
        this.strength = strength;
        this.energyCost = energyCost;
        this.saturationCost = saturationCost;
    }

    public Player getPlayer() {
        return player;
    }

    public String getDirection() {
        return direction;
    }

    public double getStrength() {
        return strength;
    }

    public void setStrength(double strength) {
        this.strength = strength;
    }

    public double getEnergyCost() {
        return energyCost;
    }

    public void setEnergyCost(double energyCost) {
        this.energyCost = energyCost;
    }

    public double getSaturationCost() {
        return saturationCost;
    }

    public void setSaturationCost(double saturationCost) {
        this.saturationCost = saturationCost;
    }
}
