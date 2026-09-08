package net.yiran.tetrajs.compat.tetrawear;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetrawear.ArmorItemEffect;
import se.mickelus.tetrawear.abilities.dodge.Dodge;
import se.mickelus.tetrawear.abilities.dodge.DodgeAttributes;
import se.mickelus.tetrawear.attributes.AgilityAttribute;
import se.mickelus.tetrawear.item.ModularArmor;
import se.mickelus.tetrawear.systems.energy.EnergyAttributes;
import se.mickelus.tetrawear.systems.energy.EnergyHelper;
import se.mickelus.tetrawear.systems.energy.EnergySystem;
import se.mickelus.tetrawear.systems.energy.PlayerEnergy;
import se.mickelus.tetrawear.systems.stealth.StealthAttribute;
import se.mickelus.tetrawear.systems.temperature.IPlayerTemperature;
import se.mickelus.tetrawear.systems.temperature.TemperatureAttributes;
import se.mickelus.tetrawear.systems.temperature.TemperaturePenalty;
import se.mickelus.tetrawear.systems.temperature.TemperatureSystem;
import se.mickelus.tetrawear.util.ArmorHelper;

public class TetraWearHelper {
    public static final TetraWearHelper INSTANCE = new TetraWearHelper();

    public final ItemEffect elytra = ArmorItemEffect.elytra;
    public final ItemEffect dazzling = ArmorItemEffect.dazzling;
    public final ItemEffect snowWalker = ArmorItemEffect.snowWalker;
    public final ItemEffect insulating = ArmorItemEffect.insulating;
    public final ItemEffect waterBreathing = ArmorItemEffect.waterBreathing;
    public final ItemEffect charge = ItemEffect.get("charge");
    public final ItemEffect evade = ItemEffect.get("evade");
    public final ItemEffect inertia = ItemEffect.get("inertia");
    public final ItemEffect shadowstep = ItemEffect.get("shadowstep");
    public final ItemEffect skitter = ItemEffect.get("skitter");
    public final ItemEffect retreatingShot = ItemEffect.get("retreatingShot");
    public final ItemEffect lifesteal = ItemEffect.get("lifesteal");
    public final ItemEffect webbingArmor = ItemEffect.get("webbingArmor");
    public final ItemEffect witheringArmor = ItemEffect.get("witheringArmor");
    public final ItemEffect harvestSpeed = ItemEffect.get("harvestSpeed");
    public final ItemEffect dampenFall = ItemEffect.get("dampenFall");
    public final ItemEffect webWalker = ItemEffect.get("webWalker");
    public final ItemEffect skeletalSway = ItemEffect.get("skeletalSway");
    public final ItemEffect shadowSway = ItemEffect.get("shadowSway");

    public PlayerEnergy getEnergy(Player player) {
        return EnergySystem.getOrCreate(player);
    }

    public double getCurrent(Player player) {
        return getEnergy(player).current;
    }

    public double getMax(Player player) {
        return getEnergy(player).max;
    }

    public double getRegen(Player player) {
        return getEnergy(player).regen;
    }

    public boolean isExhausted(Player player) {
        return EnergyHelper.isExhausted(player);
    }

    public boolean canUseEnergy(Player player) {
        return EnergySystem.canUseEnergy(player);
    }

    public boolean isEffectivelyEnabled(Player player) {
        return EnergySystem.isEffectivelyEnabled(player);
    }

    public void drain(Player player, double amount) {
        TetraWearEnergyContext.run("script", () -> EnergyHelper.drain(player, amount));
    }

    public float getCurrentTemperature(Player player) {
        return TemperatureSystem.getTemperature(player)
                .map(IPlayerTemperature::getCurrentTemperature)
                .orElse(0.0F);
    }

    public float getTargetTemperature(Player player) {
        return TemperatureSystem.getTemperature(player)
                .map(IPlayerTemperature::getTargetTemperature)
                .orElse(0.0F);
    }

    public int getTempZone(Player player) {
        return TemperatureSystem.getTempZone(getCurrentTemperature(player));
    }

    public String getPenalty(Player player) {
        return TemperatureSystem.getTemperaturePenalty(player)
                .map(TemperaturePenalty::name)
                .orElse(null);
    }

    public int getHeatResistance(Player player) {
        return TemperatureSystem.getHeatResistance(player);
    }

    public int getColdResistance(Player player) {
        return TemperatureSystem.getColdResistance(player);
    }

    public boolean hasModularArmor(Player player) {
        return ArmorHelper.hasModularArmor(player);
    }

    public boolean isModularArmorItem(Item item) {
        return item instanceof ModularArmor;
    }

    public EffectData getArmorEffects(LivingEntity entity) {
        return ArmorHelper.getArmorEffects(entity);
    }

    public boolean isDodgeEnabled(Player player) {
        return Dodge.isEnabled(player);
    }

    public boolean canDodge(Player player) {
        return Dodge.isEnabled(player) && Dodge.checkEnergy(player);
    }

    public Attribute agility() {
        return AgilityAttribute.attribute.get();
    }

    public Attribute stealth() {
        return StealthAttribute.attribute.get();
    }

    public Attribute dodgeStrength() {
        return DodgeAttributes.dodgeStrength.get();
    }

    public Attribute dodgeEnergyCost() {
        return DodgeAttributes.dodgeEnergyCost.get();
    }

    public Attribute heatResistance() {
        return TemperatureAttributes.heatResistance.get();
    }

    public Attribute coldResistance() {
        return TemperatureAttributes.coldResistance.get();
    }

    public Attribute energyAttackDamage() {
        return EnergyAttributes.energyAttackDamageAttribute.get();
    }

    public Attribute energyAttackCost() {
        return EnergyAttributes.energyAttackCostAttribute.get();
    }

    public Attribute energyHarvestSpeed() {
        return EnergyAttributes.energyHarvestSpeedAttribute.get();
    }

    public Attribute energyHarvestCost() {
        return EnergyAttributes.energyHarvestCostAttribute.get();
    }

    public Attribute energyArmor() {
        return EnergyAttributes.energyArmorAttribute.get();
    }

    public Attribute energyToughness() {
        return EnergyAttributes.energyToughnessAttribute.get();
    }

    public Attribute energyDefenceCost() {
        return EnergyAttributes.energyDefenceCostAttribute.get();
    }

    public Attribute energyItemUseCost() {
        return EnergyAttributes.energyItemUseCostAttribute.get();
    }
}
