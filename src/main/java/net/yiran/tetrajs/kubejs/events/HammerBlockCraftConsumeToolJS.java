package net.yiran.tetrajs.kubejs.events;

import dev.latvian.mods.kubejs.level.LevelEventJS;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.blocks.forged.hammer.HammerHeadBlock;

public class HammerBlockCraftConsumeToolJS extends LevelEventJS {
    public Level level;
    public BlockPos pos;
    public BlockState baseBlockState;
    public HammerHeadBlock hammerHeadBlock;
    public ItemStack targetStack;
    public Player player;
    public ToolAction requiredTool;
    public int requiredLevel;
    public boolean consumeResources;
    public HammerBlockCraftConsumeToolJS(Level world, BlockPos pos, BlockState baseBlockState, HammerHeadBlock hammerHeadBlock, ItemStack targetStack, Player player, ToolAction requiredTool, int requiredLevel, boolean consumeResources) {
        this.level = world;
        this.pos = pos;
        this.baseBlockState = baseBlockState;
        this.hammerHeadBlock = hammerHeadBlock;
        this.targetStack = targetStack;
        this.player = player;
        this.requiredTool = requiredTool;
        this.requiredLevel = requiredLevel;
        this.consumeResources = consumeResources;
    }

    @Override
    public Level getLevel() {
        return level;
    }
    public BlockPos getPos() {
        return pos;
    }
    public BlockState getBaseBlockState() {
        return baseBlockState;
    }
    public HammerHeadBlock getHammerHeadBlock() {
        return hammerHeadBlock;
    }
    public ItemStack getTargetStack() {
        return targetStack;
    }
    public Player getPlayer() {
        return player;
    }
    public ToolAction getRequiredTool() {
        return requiredTool;
    }
    public int getRequiredLevel() {
        return requiredLevel;
    }
    public boolean consumeResources() {
        return consumeResources;
    }
}
