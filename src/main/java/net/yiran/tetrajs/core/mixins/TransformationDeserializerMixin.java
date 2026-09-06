package net.yiran.tetrajs.core.mixins;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.mutil.util.JsonOptional;
import se.mickelus.tetra.data.deserializer.QuaternionDeserializer;
import se.mickelus.tetra.data.deserializer.TransformationDeserializer;
import se.mickelus.tetra.data.deserializer.VectorDeserializer;

import java.lang.reflect.Type;

@Mixin(value = TransformationDeserializer.class, remap = false)
public class TransformationDeserializerMixin {
    @Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lcom/mojang/math/Transformation;", at = @At(value = "HEAD"), cancellable = true)
    private void tetrajs$deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context, CallbackInfoReturnable<Transformation> cir) {
        JsonObject object = json.getAsJsonObject();
        String left = object.has("rotationL")?"rotationL":"rotation";
        String right = object.has("rotationR")?"rotationR":"rotation";
        try {
            cir.setReturnValue(new Transformation(
                    JsonOptional.field(object, "translation")
                            .map(VectorDeserializer::deserialize)
                            .orElse(null),
                    JsonOptional.field(object, left)
                            .map(QuaternionDeserializer::deserialize)
                            .orElse(null),
                    JsonOptional.field(object, "scale")
                            .map(VectorDeserializer::deserialize)
                            .orElse(null),
                    JsonOptional.field(object, right)
                            .map(QuaternionDeserializer::deserialize)
                            .orElse(null)
            )
        );
        } catch (JsonParseException e) {
            throw new JsonParseException("Tried to parse faulty Transformation: " + json, e);
        }
    }
}
