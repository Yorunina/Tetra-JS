package net.yiran.tetrajs.construction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.level.BlockEvent;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public final class ConstructionJob {
    private static final int MAX_RANGE = 100;
    private static final Property<?>[] COPIED_PROPERTIES = {
            BlockStateProperties.HORIZONTAL_FACING,
            BlockStateProperties.FACING,
            BlockStateProperties.FACING_HOPPER,
            BlockStateProperties.ROTATION_16,
            BlockStateProperties.AXIS,
            BlockStateProperties.HALF,
            BlockStateProperties.STAIRS_SHAPE
    };

    private ConstructionJob() {
    }

    public static boolean hasEffect(ItemStack stack) {
        return getLevel(stack) > 0;
    }

    public static int getLevel(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IModularItem modularItem)) {
            return 0;
        }
        return modularItem.getEffectLevel(stack, ConstructionEffect.EFFECT);
    }

    public static boolean wouldPerformToolAction(Player player, Level level, InteractionHand hand, BlockHitResult hit, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemModularHandheld handheld)) {
            return false;
        }
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        UseOnContext context = new UseOnContext(player, hand, hit);
        for (ToolAction action : handheld.getToolActions(stack)) {
            if (state.getToolModifiedState(context, action, true) != null) {
                return true;
            }
        }
        return handheld.getEffectLevel(stack, ItemEffect.denailing) > 0 && ItemModularHandheld.canDenail(state);
    }

    public static List<BlockPos> collectPositions(Player player, Level level, InteractionHand hand, BlockHitResult hit, ItemStack stack) {
        return collect(player, level, hand, hit, stack).stream().map(PlacedBlock::pos).toList();
    }

    public static boolean execute(Player player, Level level, InteractionHand hand, BlockHitResult hit, ItemStack stack) {
        List<PlacedBlock> planned = collect(player, level, hand, hit, stack);
        if (planned.isEmpty()) {
            return false;
        }
        ConstructionOptions options = new ConstructionOptions(stack);
        ConstructionSupplier supplier = new ConstructionSupplier(player, options);
        Item targetItem = level.getBlockState(hit.getBlockPos()).getBlock().asItem();
        supplier.gather(targetItem instanceof BlockItem blockItem ? blockItem : null);

        List<PlacedBlock> placed = new ArrayList<>();
        for (PlacedBlock plan : planned) {
            if (!canKeepPlacing(stack, player)) {
                break;
            }
            BlockState supporting = level.getBlockState(plan.pos().relative(hit.getDirection().getOpposite()));
            BlockState placedState = placeOne(player, level, hand, plan.pos(), plan.item(), hit.getDirection(), supporting, options);
            if (placedState == null) {
                continue;
            }
            if (!supplier.consume(plan.item())) {
                level.removeBlock(plan.pos(), false);
                break;
            }
            if (!player.getAbilities().instabuild && stack.getItem() instanceof IModularItem modularItem && stack.getMaxDamage() > 0) {
                modularItem.applyDamage(1, stack, player);
            }
            placed.add(new PlacedBlock(plan.pos(), plan.item(), placedState));
            if (stack.getItem() instanceof IModularItem modularItem && modularItem.isBroken(stack)) {
                break;
            }
        }
        if (!placed.isEmpty()) {
            SoundType sound = placed.get(0).state().getSoundType();
            level.playSound(null, player.blockPosition(), sound.getPlaceSound(), SoundSource.BLOCKS, sound.getVolume(), sound.getPitch());
            ConstructionUndoHistory.add(player, level, placed);
        }
        return !placed.isEmpty();
    }

    public static boolean canRestore(Level level, Player player, PlacedBlock block) {
        BlockState current = level.getBlockState(block.pos());
        if (!level.mayInteract(player, block.pos())) {
            return false;
        }
        if (player.getAbilities().instabuild) {
            return true;
        }
        if (current.getDestroySpeed(level, block.pos()) <= -1 || level.getBlockEntity(block.pos()) != null) {
            return false;
        }
        return SimilarBlocks.match(current.getBlock(), block.state().getBlock());
    }

    public static boolean restore(Level level, Player player, PlacedBlock block) {
        if (!canRestore(level, player, block)) {
            return false;
        }
        BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(level, block.pos(), level.getBlockState(block.pos()), player);
        MinecraftForge.EVENT_BUS.post(breakEvent);
        if (breakEvent.isCanceled()) {
            return false;
        }
        return level.removeBlock(block.pos(), false);
    }

    private static List<PlacedBlock> collect(Player player, Level level, InteractionHand hand, BlockHitResult hit, ItemStack stack) {
        int limit = ConstructionEffect.maxBlocks(getLevel(stack));
        if (limit <= 0 || hit == null) {
            return List.of();
        }
        ConstructionOptions options = new ConstructionOptions(stack);
        BlockState targetState = level.getBlockState(hit.getBlockPos());
        Item targetItem = targetState.getBlock().asItem();
        ConstructionSupplier supplier = new ConstructionSupplier(player, options);
        supplier.gather(targetItem instanceof BlockItem blockItem ? blockItem : null);
        if (!player.getAbilities().instabuild) {
            limit = Math.min(limit, supplier.available());
        }
        if (limit <= 0) {
            return List.of();
        }
        if (!canKeepPlacing(stack, player)) {
            return List.of();
        }

        Direction face = hit.getDirection();
        LinkedList<BlockPos> candidates = new LinkedList<>();
        HashSet<BlockPos> seen = new HashSet<>();
        List<PlacedBlock> result = new ArrayList<>();
        if (canStart(face, options)) {
            candidates.add(hit.getBlockPos().relative(face));
        }

        while (!candidates.isEmpty() && result.size() < limit) {
            BlockPos candidate = candidates.removeFirst();
            if (!seen.add(candidate) || !inRange(player, candidate)) {
                continue;
            }
            BlockPos supportingPos = candidate.relative(face.getOpposite());
            BlockState supporting = level.getBlockState(supportingPos);
            if (!options.matchBlocks(targetState.getBlock(), supporting.getBlock())) {
                continue;
            }
            if (!isPositionPlaceable(player, level, hand, candidate, face, options.getReplace())) {
                continue;
            }
            BlockItem placeItem = supplier.takePreview(item -> placementState(player, level, hand, candidate, face, item, supporting, options) != null);
            if (placeItem == null) {
                continue;
            }
            BlockState placement = placementState(player, level, hand, candidate, face, placeItem, supporting, options);
            if (placement == null) {
                continue;
            }
            result.add(new PlacedBlock(candidate, placeItem, placement));
            enqueue(candidates, candidate, face, options);
        }
        return result;
    }

    private static boolean canStart(Direction face, ConstructionOptions options) {
        if (face == Direction.UP || face == Direction.DOWN) {
            return options.testLock(ConstructionOptions.Lock.NORTHSOUTH) || options.testLock(ConstructionOptions.Lock.EASTWEST);
        }
        return options.testLock(ConstructionOptions.Lock.HORIZONTAL) || options.testLock(ConstructionOptions.Lock.VERTICAL);
    }

    private static void enqueue(LinkedList<BlockPos> candidates, BlockPos current, Direction face, ConstructionOptions options) {
        switch (face) {
            case DOWN, UP -> {
                if (options.testLock(ConstructionOptions.Lock.NORTHSOUTH)) {
                    candidates.add(current.relative(Direction.NORTH));
                    candidates.add(current.relative(Direction.SOUTH));
                }
                if (options.testLock(ConstructionOptions.Lock.EASTWEST)) {
                    candidates.add(current.relative(Direction.EAST));
                    candidates.add(current.relative(Direction.WEST));
                }
                if (options.testLock(ConstructionOptions.Lock.NORTHSOUTH) && options.testLock(ConstructionOptions.Lock.EASTWEST)) {
                    candidates.add(current.relative(Direction.NORTH).relative(Direction.EAST));
                    candidates.add(current.relative(Direction.NORTH).relative(Direction.WEST));
                    candidates.add(current.relative(Direction.SOUTH).relative(Direction.EAST));
                    candidates.add(current.relative(Direction.SOUTH).relative(Direction.WEST));
                }
            }
            case NORTH, SOUTH -> {
                if (options.testLock(ConstructionOptions.Lock.HORIZONTAL)) {
                    candidates.add(current.relative(Direction.EAST));
                    candidates.add(current.relative(Direction.WEST));
                }
                if (options.testLock(ConstructionOptions.Lock.VERTICAL)) {
                    candidates.add(current.relative(Direction.UP));
                    candidates.add(current.relative(Direction.DOWN));
                }
                if (options.testLock(ConstructionOptions.Lock.HORIZONTAL) && options.testLock(ConstructionOptions.Lock.VERTICAL)) {
                    candidates.add(current.relative(Direction.UP).relative(Direction.EAST));
                    candidates.add(current.relative(Direction.UP).relative(Direction.WEST));
                    candidates.add(current.relative(Direction.DOWN).relative(Direction.EAST));
                    candidates.add(current.relative(Direction.DOWN).relative(Direction.WEST));
                }
            }
            case EAST, WEST -> {
                if (options.testLock(ConstructionOptions.Lock.HORIZONTAL)) {
                    candidates.add(current.relative(Direction.NORTH));
                    candidates.add(current.relative(Direction.SOUTH));
                }
                if (options.testLock(ConstructionOptions.Lock.VERTICAL)) {
                    candidates.add(current.relative(Direction.UP));
                    candidates.add(current.relative(Direction.DOWN));
                }
                if (options.testLock(ConstructionOptions.Lock.HORIZONTAL) && options.testLock(ConstructionOptions.Lock.VERTICAL)) {
                    candidates.add(current.relative(Direction.UP).relative(Direction.NORTH));
                    candidates.add(current.relative(Direction.UP).relative(Direction.SOUTH));
                    candidates.add(current.relative(Direction.DOWN).relative(Direction.NORTH));
                    candidates.add(current.relative(Direction.DOWN).relative(Direction.SOUTH));
                }
            }
        }
    }

    private static boolean isPositionPlaceable(Player player, Level level, InteractionHand hand, BlockPos pos, Direction face, boolean replace) {
        if (!level.getWorldBorder().isWithinBounds(pos) || level.isOutsideBuildHeight(pos) || !level.mayInteract(player, pos)) {
            return false;
        }
        if (level.isEmptyBlock(pos)) {
            return true;
        }
        if (!replace) {
            return false;
        }
        BlockPlaceContext context = makePlaceContext(player, hand, pos, face, (BlockItem) Items.STONE);
        return context.canPlace();
    }

    @Nullable
    private static BlockState placeOne(Player player, Level level, InteractionHand hand, BlockPos pos, BlockItem item, Direction face, BlockState supporting, ConstructionOptions options) {
        BlockState state = placementState(player, level, hand, pos, face, item, supporting, options);
        if (state == null) {
            return null;
        }
        if (!level.setBlockAndUpdate(pos, state)) {
            return null;
        }
        BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, pos);
        BlockEvent.EntityPlaceEvent placeEvent = new BlockEvent.EntityPlaceEvent(snapshot, state, player);
        MinecraftForge.EVENT_BUS.post(placeEvent);
        if (placeEvent.isCanceled()) {
            level.removeBlock(pos, false);
            return null;
        }
        state.getBlock().setPlacedBy(level, pos, state, player, new ItemStack(item));
        return level.getBlockState(pos);
    }

    @Nullable
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static BlockState placementState(Player player, Level level, InteractionHand hand, BlockPos pos, Direction face, BlockItem item, BlockState supporting, ConstructionOptions options) {
        BlockPlaceContext context = makePlaceContext(player, hand, pos, face, item);
        if (!player.mayUseItemAt(pos, face, context.getItemInHand()) || !context.canPlace()) {
            return null;
        }
        BlockState state = item.getBlock().getStateForPlacement(context);
        if (state == null || !state.canSurvive(level, pos) || collides(level, state, pos)) {
            return null;
        }
        if (options.getDirection() == ConstructionOptions.DirectionMode.TARGET) {
            for (Property property : COPIED_PROPERTIES) {
                if (supporting.hasProperty(property) && state.hasProperty(property)) {
                    state = state.setValue(property, supporting.getValue(property));
                }
            }
            if (supporting.hasProperty(BlockStateProperties.SLAB_TYPE) && state.hasProperty(BlockStateProperties.SLAB_TYPE)) {
                SlabType slabType = supporting.getValue(BlockStateProperties.SLAB_TYPE);
                if (slabType != SlabType.DOUBLE) {
                    state = state.setValue(BlockStateProperties.SLAB_TYPE, slabType);
                }
            }
        }
        return state;
    }

    private static BlockPlaceContext makePlaceContext(Player player, InteractionHand hand, BlockPos pos, Direction face, BlockItem blockItem) {
        Vec3 click = Vec3.atCenterOf(pos);
        BlockHitResult hit = new BlockHitResult(click, face, pos, false);
        return new BlockPlaceContext(player, hand, new ItemStack(blockItem), hit);
    }

    private static boolean collides(Level level, BlockState state, BlockPos pos) {
        VoxelShape shape = state.getCollisionShape(level, pos);
        if (shape.isEmpty()) {
            return false;
        }
        AABB box = shape.bounds().move(pos);
        return !level.getEntitiesOfClass(LivingEntity.class, box, entity -> !entity.isSpectator() && entity.isAlive()).isEmpty();
    }

    private static boolean inRange(Player player, BlockPos pos) {
        return Math.max(Math.abs(player.blockPosition().getX() - pos.getX()), Math.abs(player.blockPosition().getZ() - pos.getZ())) <= MAX_RANGE;
    }
    private static boolean canKeepPlacing(ItemStack stack, Player player) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        return stack.getItem() instanceof IModularItem modularItem && !modularItem.isBroken(stack);
    }

    public record PlacedBlock(BlockPos pos, BlockItem item, BlockState state) {
    }
}
