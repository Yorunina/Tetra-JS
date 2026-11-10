package net.yiran.tetrajs.kubejs.events;

import dev.latvian.mods.kubejs.level.LevelEventJS;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import se.mickelus.tetra.items.modular.ThrownModularItemEntity;

/**
 * A server-side modular item throw, after construction and before inventory removal.
 */
public class CreateThrownItemEventJS extends LevelEventJS {
    private final Player player;
    private final ThrownModularItemEntity projectile;
    private final ItemStack item;

    public CreateThrownItemEventJS(Player player, ThrownModularItemEntity projectile, ItemStack item) {
        this.player = player;
        this.projectile = projectile;
        this.item = item;
    }

    @Override
    public Level getLevel() {
        return player.level();
    }

    public Player getPlayer() {
        return player;
    }

    public ThrownModularItemEntity getProjectile() {
        return projectile;
    }

    public ItemStack getItem() {
        return item;
    }
}
