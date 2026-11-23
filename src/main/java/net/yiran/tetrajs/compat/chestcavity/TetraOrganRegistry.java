package net.yiran.tetrajs.compat.chestcavity;

import net.minecraft.resources.ResourceLocation;
import se.mickelus.tetra.effect.ItemEffect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TetraOrganRegistry {
    private static final Map<ResourceLocation, List<Mapping>> MAPPINGS = new LinkedHashMap<>();

    private TetraOrganRegistry() {
    }

    public static void register(String itemId, String effectId, String scoreId, double multiplier) {
        register(itemId, effectId, scoreId, multiplier, 0.0);
    }

    public static void register(String itemId, String effectId, String scoreId, double multiplier, double base) {
        ResourceLocation item = new ResourceLocation(itemId);
        ResourceLocation score = new ResourceLocation(scoreId);
        ItemEffect effect = ItemEffect.get(effectId);
        if (effect == null) {
            throw new IllegalArgumentException("Unknown Tetra effect: " + effectId);
        }
        List<Mapping> mappings = MAPPINGS.computeIfAbsent(item, ignored -> new ArrayList<>());
        mappings.removeIf(mapping -> mapping.effect().equals(effect) && mapping.score().equals(score));
        mappings.add(new Mapping(effect, score, (float) multiplier, (float) base));
    }

    static List<Mapping> getMappings(ResourceLocation itemId) {
        return MAPPINGS.getOrDefault(itemId, List.of());
    }

    record Mapping(ItemEffect effect, ResourceLocation score, float multiplier, float base) {
    }
}
