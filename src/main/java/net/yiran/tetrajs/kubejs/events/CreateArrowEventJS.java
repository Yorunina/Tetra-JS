package net.yiran.tetrajs.kubejs.events;

import dev.latvian.mods.kubejs.level.LevelEventJS;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CreateArrowEventJS extends LevelEventJS {
    public Level level;
    public ItemStack item;
    public ArrowItem ammoItem;
    public ItemStack ammoStack;
    public AbstractArrow projectile;
    public Player player;
    private boolean modifyProjectile = false;
    public int drawProgress = 0;

    public CreateArrowEventJS(ItemStack itemStack, Level world, ArrowItem ammoItem, ItemStack ammoStack, Player player, AbstractArrow projectile) {
        this.item = itemStack;
        this.level = world;
        this.ammoItem = ammoItem;
        this.ammoStack = ammoStack;
        this.player = player;
        this.projectile = projectile;
    }
    public CreateArrowEventJS(ItemStack itemStack, Level world, ArrowItem ammoItem, ItemStack ammoStack, Player player, AbstractArrow projectile, int drawProgress) {
        this.item = itemStack;
        this.level = world;
        this.ammoItem = ammoItem;
        this.ammoStack = ammoStack;
        this.player = player;
        this.projectile = projectile;
        this.drawProgress = drawProgress;
    }
    public int getDrawProgress() {
        return drawProgress;
    }
    @Override
    public Level getLevel() {
        return level;
    }
    public ItemStack getItem() {
        return item;
    }
    public ArrowItem getAmmoItem() {
        return ammoItem;
    }
    public ItemStack getAmmoStack() {
        return ammoStack;
    }
    public AbstractArrow getProjectile() {
        return projectile;
    }
    public Player getPlayer() {
        return player;
    }
    public void setProjectile(AbstractArrow projectile) {
        this.projectile = projectile;
        this.modifyProjectile = true;
    }
    public boolean isModifyProjectile() {
        return modifyProjectile;
    }
}
