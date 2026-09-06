package net.yiran.tetrajs.core.mixins;

import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.module.schematic.ConfigSchematic;
import se.mickelus.tetra.module.schematic.SchematicDefinition;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ConfigSchematic.class, remap = false)
public class ConfigSchematicDescriptionMixin {
    @Shadow
    @Final
    private SchematicDefinition definition;

    @Inject(method = "getRequirementDescription", at = @At(value = "HEAD"), cancellable = true)
    private void getRequirementDescription(CallbackInfoReturnable<List<Component>> cir) {
        List<Component> list = new ArrayList<>();
        if (this.definition.materialRevealSlot > -1) {
            list.add(Component.translatable("tetra.holo.material_reveal"));
        }
        if (this.definition.requirement != null && this.definition.requirement.getDescription() != null) {
            list.addAll(this.definition.requirement.getDescription());
        }
        if(list.isEmpty()) {
            list = null;
        }
        cir.setReturnValue(list);
    }
}
