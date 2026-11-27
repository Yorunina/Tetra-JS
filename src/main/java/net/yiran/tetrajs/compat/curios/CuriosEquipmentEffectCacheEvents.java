package net.yiran.tetrajs.compat.curios;

import net.yiran.tetrajs.compat.TetraEquipmentEffectCache;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

public final class CuriosEquipmentEffectCacheEvents {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCurioChange(CurioChangeEvent event) {
        TetraEquipmentEffectCache.INSTANCE.invalidate(event.getEntity());
    }
}
