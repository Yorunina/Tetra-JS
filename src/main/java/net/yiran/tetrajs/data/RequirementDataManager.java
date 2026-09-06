package net.yiran.tetrajs.data;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.yiran.tetrajs.TetraJS;
import net.yiran.tetrajs.requirements.group.GroupRequirementStore;
import se.mickelus.mutil.data.DataDistributor;
import se.mickelus.mutil.data.DataStore;
import se.mickelus.tetra.blocks.scroll.ScrollData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.yiran.tetrajs.TetraJS.NETWORK;
import static se.mickelus.tetra.data.DataManager.gson;

public class RequirementDataManager implements DataDistributor {
    public static RequirementDataManager instance = new RequirementDataManager();

    public DataStore<GroupRequirementStore> groupRequirement;
    public DataStore<ScrollData[]> scrolls;
    public List<DataStore<?>> dataStores=new ArrayList<>(10);

    public RequirementDataManager() {
        groupRequirement = new DataStore<>(gson, TetraJS.MODID,"group", GroupRequirementStore.class,this);
        scrolls = new DataStore<>(gson, TetraJS.MODID,"scrolls", ScrollData[].class,this);
        groupRequirement.onReload(()->GroupRequirementStore.handler(groupRequirement.getData()));
        dataStores.add(groupRequirement);
        dataStores.add(scrolls);
    }

    @SubscribeEvent(
            priority = EventPriority.LOWEST
    )
    public void addReloadListener(AddReloadListenerEvent event) {
        dataStores.forEach(event::addListener);
    }

    @SubscribeEvent
    public void playerConnected(PlayerEvent.PlayerLoggedInEvent event) {
        dataStores.forEach(dataStore -> dataStore.sendToPlayer((ServerPlayer)event.getEntity()));
    }


    public void onDataReceived(String directory, Map<ResourceLocation, String> data) {
        dataStores.stream()
                .filter(dataStore -> dataStore.getDirectory().equals(directory))
                .forEach(dataStore -> dataStore.loadFromPacket(data));
    }

    @Override
    public void sendToAll(String directory, Map<ResourceLocation, JsonElement> data) {
        NETWORK.sendToAllPlayers(new RequirementUpdateDataPacket(directory, data));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, String directory, Map<ResourceLocation, JsonElement> data) {
        NETWORK.sendTo(new RequirementUpdateDataPacket(directory, data), player);
    }
}
