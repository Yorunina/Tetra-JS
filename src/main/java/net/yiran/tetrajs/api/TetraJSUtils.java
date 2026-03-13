package net.yiran.tetrajs.api;

import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.aspect.ItemAspect;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.schematic.OutcomeMaterial;

import java.util.Objects;
import java.util.Set;

public class TetraJSUtils {
    public static TetraJSUtils INSTANCE = new TetraJSUtils();

    @Info("检测一个物品是否为tetra的材料")
    public boolean stackIsTetraMaterial(ItemStack stack) {
        return DataManager.instance.materialData
                .getData()
                .values()
                .stream()
                .map(data -> data.material)
                .map(OutcomeMaterial::getPredicate)
                .filter(Objects::nonNull)
                .anyMatch(itemPredicate -> itemPredicate.matches(stack));
    }

    @Info("检测一个物品是否为tetra工具")
    public boolean isModularItem(Item item) {
        return item instanceof IModularItem;
    }

    @Info("获取改进等级,不存在则返回-1")
    public int getImprovementLevel(ItemStack stack, String slot, String improvement) {
        if (stack.getItem() instanceof IModularItem modularItem) {
            if (modularItem.getModuleFromSlot(stack, slot) instanceof ItemModuleMajor moduleMajor) {
                return moduleMajor.getImprovementLevel(stack, improvement);
            }
        }
        return -1;
    }

    @Info("给物品添加改进")
    public void addImprovement(ItemStack stack, String slot, String improvement, int level) {
        if (stack.getItem() instanceof IModularItem modularItem) {
            if (modularItem.getModuleFromSlot(stack, slot) instanceof ItemModuleMajor moduleMajor) {
                moduleMajor.addImprovement(stack, improvement, level);
            }
        }
    }

    @Info("给物品添加改进,并检查添加的是否为更高级的改进,如果不是则取消添加，成功添加返回true")
    public boolean addCheckedImprovement(ItemStack stack, String slot, String improvement, int level) {
        if (stack.getItem() instanceof IModularItem modularItem) {
            if (modularItem.getModuleFromSlot(stack, slot) instanceof ItemModuleMajor moduleMajor) {
                if (level > moduleMajor.getImprovementLevel(stack, improvement)) {
                    moduleMajor.addImprovement(stack, improvement, level);
                    return true;
                }
            }
        }
        return false;
    }

    public int getAspectLevel(ItemStack stack, String slot, ItemAspect aspect) {
        if (stack.getItem() instanceof IModularItem modularItem) {
            ItemModule itemModule = modularItem.getModuleFromSlot(stack, slot);
            if (itemModule != null) {
                return itemModule.getAspects(stack).getLevel(aspect);
            }
        }
        return -1;
    }

    public Set<ItemAspect> getAspects(ItemStack stack, String slot, ItemAspect aspect) {
        if (stack.getItem() instanceof IModularItem modularItem) {
            ItemModule itemModule = modularItem.getModuleFromSlot(stack, slot);
            if (itemModule != null) {
                return itemModule.getAspects(stack).getValues();
            }
        }
        return Set.of();
    }

    public boolean hasAspectLevel(ItemStack stack, String slot, ItemAspect aspect) {
        if (stack.getItem() instanceof IModularItem modularItem) {
            ItemModule itemModule = modularItem.getModuleFromSlot(stack, slot);
            if (itemModule != null) {
                return itemModule.hasAspect(stack, aspect);
            }
        }
        return false;
    }

}
