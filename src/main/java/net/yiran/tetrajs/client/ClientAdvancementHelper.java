package net.yiran.tetrajs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class ClientAdvancementHelper {
    public static boolean hasAdvancement(ResourceLocation id) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) return false;
        return connection.getAdvancements().getAdvancements().get(id) != null;
    }
}
