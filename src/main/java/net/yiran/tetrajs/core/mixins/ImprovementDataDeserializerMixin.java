package net.yiran.tetrajs.core.mixins;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import net.yiran.tetrajs.core.InfiniteImprovementAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.module.data.ImprovementData;

import java.lang.reflect.Type;

@Mixin(value = ImprovementData.Deserializer.class, remap = false)
public class ImprovementDataDeserializerMixin {
    @Inject(
            method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lse/mickelus/tetra/module/data/ImprovementData;",
            at = @At("RETURN")
    )
    private void tetrajs$readInfinite(JsonElement json, Type type, JsonDeserializationContext context, CallbackInfoReturnable<ImprovementData> cir) {
        ImprovementData data = cir.getReturnValue();
        if (data instanceof InfiniteImprovementAccess access && json != null && json.isJsonObject() && json.getAsJsonObject().has("infinite")) {
            access.tetrajs$setInfinite(json.getAsJsonObject().get("infinite").getAsBoolean());
        }
    }
}