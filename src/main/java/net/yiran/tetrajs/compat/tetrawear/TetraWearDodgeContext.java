package net.yiran.tetrajs.compat.tetrawear;

public final class TetraWearDodgeContext {
    private static final ThreadLocal<Double> STRENGTH = new ThreadLocal<>();
    private static final ThreadLocal<Double> ENERGY_COST = new ThreadLocal<>();
    private static final ThreadLocal<Double> SATURATION_COST = new ThreadLocal<>();

    private TetraWearDodgeContext() {
    }

    public static void set(double strength, double energyCost, double saturationCost) {
        STRENGTH.set(strength);
        ENERGY_COST.set(energyCost);
        SATURATION_COST.set(saturationCost);
    }

    public static void clear() {
        STRENGTH.remove();
        ENERGY_COST.remove();
        SATURATION_COST.remove();
    }

    public static Double getStrength() {
        return STRENGTH.get();
    }

    public static Double getEnergyCost() {
        return ENERGY_COST.get();
    }

    public static Double getSaturationCost() {
        return SATURATION_COST.get();
    }
}
