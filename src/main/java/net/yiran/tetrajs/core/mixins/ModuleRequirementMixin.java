package net.yiran.tetrajs.core.mixins;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.schematic.requirement.ModuleRequirement;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mixin(value = ModuleRequirement.class, remap = false)
public abstract class ModuleRequirementMixin {
    @Shadow
    private String moduleKey;

    @Shadow
    private String moduleVariant;

    @Shadow
    private String materialPattern;

    @Shadow
    static String getModuleName(String moduleKey) {
        return null;
    }

    @Inject(method = "getDescription", at = @At(value = "HEAD"), cancellable = true)
    private void getDescription(CallbackInfoReturnable<List<Component>> cir) {
        String[] values = new String[]{moduleKey != null ? I18n.get("tetra.holo.module_requirement.module_key", getModuleName(moduleKey)) : null, moduleVariant != null ? I18n.get("tetra.holo.module_requirement.variant_key", ItemModule.getVariantName(moduleVariant)) : null, materialPattern != null ? I18n.get("tetra.holo.module_requirement.material", I18n.get("tetra.material." + materialPattern)) : null,};

        cir.setReturnValue(
                List.of(Component.translatable("tetra.holo.module_requirement.module", Arrays.stream(values).filter(Objects::nonNull).collect(Collectors.joining(", "))))
        );
    }
}
