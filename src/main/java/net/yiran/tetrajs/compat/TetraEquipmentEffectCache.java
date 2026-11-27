package net.yiran.tetrajs.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Living entity equipment cache for modular Curios items and TetraWear armor.
 * The cache is invalidated by equipment lifecycle events or explicitly from KubeJS.
 */
public final class TetraEquipmentEffectCache {
    public static final TetraEquipmentEffectCache INSTANCE = new TetraEquipmentEffectCache();

    private final Map<LivingEntity, EquipmentSnapshot> snapshots = new WeakHashMap<>();

    private TetraEquipmentEffectCache() {
    }

    public synchronized EquipmentSnapshot get(LivingEntity entity) {
        if (entity == null) {
            return EquipmentSnapshot.EMPTY;
        }
        return snapshots.computeIfAbsent(entity, this::buildSnapshot);
    }

    public Map<String, EffectSourceSnapshot> getStrongestSources(LivingEntity entity) {
        return getStrongestSources(entity, null);
    }

    /** Equal levels prefer the projectile, then main hand, off hand, Curios and armor. */
    public Map<String, EffectSourceSnapshot> getStrongestSources(LivingEntity entity, ItemStack projectileStack) {
        List<ModularItemSnapshot> items = new ArrayList<>();
        addSnapshot(items, projectileStack, "projectile");
        addSnapshot(items, entity.getMainHandItem(), "mainhand");
        addSnapshot(items, entity.getOffhandItem(), "offhand");
        items.addAll(get(entity).getItems());

        Map<String, EffectSourceSnapshot> sources = new LinkedHashMap<>();
        for (ModularItemSnapshot item : items) {
            for (EffectSnapshot effect : item.getEffects()) {
                EffectSourceSnapshot previous = sources.get(effect.getKey());
                if (previous == null || effect.getLevel() > previous.getLevel()) {
                    sources.put(effect.getKey(), new EffectSourceSnapshot(item, effect));
                }
            }
        }
        return sources;
    }

    public synchronized void invalidate(LivingEntity entity) {
        if (entity != null) {
            snapshots.remove(entity);
        }
    }

    public synchronized void invalidateAll() {
        snapshots.clear();
    }

    private EquipmentSnapshot buildSnapshot(LivingEntity entity) {
        List<ModularItemSnapshot> items = new ArrayList<>();

        CompatManager.getCuriosModularItems(entity).forEach(stack -> addSnapshot(items, stack, "curios"));

        for (ItemStack stack : entity.getArmorSlots()) {
            addSnapshot(items, stack, "armor");
        }

        return new EquipmentSnapshot(items);
    }

    private static void addSnapshot(List<ModularItemSnapshot> items, ItemStack stack, String location) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof IModularItem modularItem)) {
            return;
        }

        List<EffectSnapshot> effects = new ArrayList<>();
        for (ItemEffect effect : modularItem.getEffects(stack)) {
            int level = modularItem.getEffectLevel(stack, effect);
            float efficiency = modularItem.getEffectEfficiency(stack, effect);
            effects.add(new EffectSnapshot(effect.getKey(), level, efficiency));
        }
        items.add(new ModularItemSnapshot(stack.copy(), effects, location));
    }

    public static final class EquipmentSnapshot {
        private static final EquipmentSnapshot EMPTY = new EquipmentSnapshot(List.of());
        private final List<ModularItemSnapshot> items;
        private final Map<String, EffectGroup> effects;

        private EquipmentSnapshot(List<ModularItemSnapshot> items) {
            this.items = Collections.unmodifiableList(new ArrayList<>(items));
            Map<String, EffectGroup> effects = new LinkedHashMap<>();
            for (ModularItemSnapshot item : items) {
                for (EffectSnapshot effect : item.getEffects()) {
                    effects.computeIfAbsent(effect.getKey(), ignored -> new EffectGroup(effect.getKey()))
                            .addSource(new EffectSourceSnapshot(item, effect));
                }
            }
            this.effects = Collections.unmodifiableMap(effects);
        }

        public List<ModularItemSnapshot> getItems() {
            return items;
        }

        public List<String> getEffectKeys() {
            return List.copyOf(effects.keySet());
        }

        public Map<String, EffectGroup> getEffects() {
            return effects;
        }

        public EffectGroup getEffect(String key) {
            return effects.get(key);
        }
    }

    public static final class ModularItemSnapshot {
        private final ItemStack stack;
        private final List<EffectSnapshot> effects;

        private final String location;

        private ModularItemSnapshot(ItemStack stack, List<EffectSnapshot> effects, String location) {
            this.location = location;
            this.stack = stack;
            this.effects = Collections.unmodifiableList(new ArrayList<>(effects));
        }

        public ItemStack getStack() {
            return stack;
        }

        public List<EffectSnapshot> getEffects() {
            return effects;
        }
    }

    public static final class EffectSnapshot {
        private final String key;
        private final int level;
        private final float efficiency;

        private EffectSnapshot(String key, int level, float efficiency) {
            this.key = key;
            this.level = level;
            this.efficiency = efficiency;
        }

        public String getKey() {
            return key;
        }

        public int getLevel() {
            return level;
        }

        public float getEfficiency() {
            return efficiency;
        }
    }

    public static final class EffectGroup {
        private final String key;
        private final List<EffectSourceSnapshot> sources = new ArrayList<>();

        private EffectGroup(String key) {
            this.key = key;
        }

        private void addSource(EffectSourceSnapshot source) {
            sources.add(source);
        }

        public String getKey() {
            return key;
        }

        public List<EffectSourceSnapshot> getSources() {
            return Collections.unmodifiableList(sources);
        }
    }

    public static final class EffectSourceSnapshot {
        private final ModularItemSnapshot item;
        private final EffectSnapshot effect;

        private EffectSourceSnapshot(ModularItemSnapshot item, EffectSnapshot effect) {
            this.item = item;
            this.effect = effect;
        }

        public ModularItemSnapshot getItem() {
            return item;
        }

        public EffectSnapshot getEffect() {
            return effect;
        }

        public int getLevel() {
            return effect.getLevel();
        }

        public float getEfficiency() {
            return effect.getEfficiency();
        }

        public String getLocation() {
            return item.location;
        }

        public ItemStack getStack() {
            return item.getStack();
        }
    }

    @SubscribeEvent
    public void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot().isArmor()) {
            invalidate(event.getEntity());
        }
    }

    @SubscribeEvent
    public void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            invalidateAll();
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        invalidate(event.getOriginal());
        invalidate(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        invalidate(event.getEntity());
    }
}
