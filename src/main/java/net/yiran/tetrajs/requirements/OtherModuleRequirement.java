package net.yiran.tetrajs.requirements;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import se.mickelus.tetra.module.schematic.CraftingContext;
import se.mickelus.tetra.module.schematic.requirement.CraftingRequirement;

import javax.annotation.Nullable;
import java.util.List;

public class OtherModuleRequirement implements CraftingRequirement {
    public CraftingRequirement requirement;
    public String slot;

    @Override
    public boolean test(CraftingContext cxt) {
        return requirement.test(changeSlot(cxt, slot));
    }

    @Nullable
    @Override
    public List<Component> getDescription() {

        ImmutableList.Builder<Component> builder = ImmutableList.builder();
        builder.add(Component.translatable("tetrajs.holo.other_module_requirement", Component.translatable("tetra.slot." + slot)));
        List<Component> description = requirement.getDescription();
        if (description != null) {
            for (int j = 0; j < description.size(); ++j) {
                if (j == 0) {
                    builder.add(Component.literal(" §8└§r ").append(description.get(j)));
                } else {
                    builder.add(Component.literal("      ").append(description.get(j)));
                }
            }
        }
        return builder.build();
    }

    private static CraftingContext changeSlot(CraftingContext cxt, String slot) {
        return new CraftingContext(cxt.world, cxt.pos, cxt.blockState, cxt.player, cxt.targetStack, slot, cxt.unlocks);
    }
}
