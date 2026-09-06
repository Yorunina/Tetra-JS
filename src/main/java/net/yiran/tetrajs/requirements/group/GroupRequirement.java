package net.yiran.tetrajs.requirements.group;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import se.mickelus.tetra.module.schematic.CraftingContext;
import se.mickelus.tetra.module.schematic.requirement.CraftingRequirement;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class GroupRequirement implements CraftingRequirement {
    public String key;
    public boolean hideExtend = false;

    @Override
    public boolean test(CraftingContext cxt) {
        GroupRequirementStore groupRequirementStore = GroupRequirementStore.groups.get(key);
        if (groupRequirementStore == null) {
            return true;
        }
        return Arrays.stream(groupRequirementStore.requirements).allMatch((requirement) -> requirement.test(cxt));
    }

    @Nullable
    @Override
    public List<Component> getDescription() {
        GroupRequirementStore groupRequirementStore = GroupRequirementStore.groups.get(key);
        if (groupRequirementStore == null) {
            return List.of(Component.translatable("tetrajs.group.empty"));
        }
        ImmutableList.Builder<Component> builder = ImmutableList.builder();
        builder.add(Component.translatable(groupRequirementStore.translation));
        if (!(hideExtend||groupRequirementStore.hideExtend)) {
            var requirements = groupRequirementStore.requirements;
            for (int i = 0; i < requirements.length; ++i) {
                List<Component> description = requirements[i].getDescription();
                if (description != null) {
                    for (int j = 0; j < description.size(); ++j) {
                        if (j == 0) {
                            builder.add(Component.literal(i == requirements.length - 1 ? " §8└§r " : " §8├§r ").append(description.get(j)));
                        } else {
                            builder.add(Component.literal(i == requirements.length - 1 ? "      " :  " §8│§r ").append(description.get(j)));
                        }
                    }
                }
            }
        }

        return builder.build();
    }
}
