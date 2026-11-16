package net.yiran.tetrajs.compat.chestcavity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.tigereye.chestcavity.chestcavities.json.organs.OrganData;
import net.tigereye.chestcavity.interfaces.OrganDataProvider;
import se.mickelus.tetra.items.modular.IModularItem;

public class TetraOrganDataProvider implements OrganDataProvider {
    @Override
    public OrganData getOrganData(ItemStack itemStack) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
        if (itemId == null || !(itemStack.getItem() instanceof IModularItem modularItem)) {
            return null;
        }

        OrganData organData = new OrganData();
        for (TetraOrganRegistry.Mapping mapping : TetraOrganRegistry.getMappings(itemId)) {
            int level = modularItem.getEffectLevel(itemStack, mapping.effect());
            if (level <= 0) continue;
            float score = mapping.base() + level * mapping.multiplier();
            organData.organScores.merge(mapping.score(), score, Float::sum);
        }
        return organData.isEmpty() ? null : organData;
    }
}
