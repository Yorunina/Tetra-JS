package net.yiran.tetrajs.data;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import se.mickelus.mutil.data.AbstractUpdateDataPacket;

import java.util.Map;

public class RequirementUpdateDataPacket extends AbstractUpdateDataPacket {
    public RequirementUpdateDataPacket() {
    }

    public RequirementUpdateDataPacket(String directory, Map<ResourceLocation, JsonElement> data) {
        super(directory, data);
    }

    @Override
    public void handle(Player player) {
        RequirementDataManager.instance.onDataReceived(this.directory, this.data);
    }
}
