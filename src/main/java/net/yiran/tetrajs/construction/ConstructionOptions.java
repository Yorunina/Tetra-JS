package net.yiran.tetrajs.construction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class ConstructionOptions {
    public static final String TAG = "tetrajs:construction";
    public static final String[] KEYS = {"lock", "direction", "replace", "match"};

    public enum Lock {
        HORIZONTAL, VERTICAL, NORTHSOUTH, EASTWEST, NOLOCK
    }

    public enum DirectionMode {
        TARGET, PLAYER
    }

    public enum Match {
        EXACT, SIMILAR, ANY
    }

    private final ItemStack stack;

    public ConstructionOptions(ItemStack stack) {
        this.stack = stack;
    }

    public Lock getLock() {
        return readEnum("lock", Lock.class, Lock.NOLOCK);
    }

    public DirectionMode getDirection() {
        return readEnum("direction", DirectionMode.class, DirectionMode.TARGET);
    }

    public boolean getReplace() {
        CompoundTag tag = readTag();
        if (tag == null || !tag.contains("replace")) {
            return true;
        }
        return tag.getBoolean("replace");
    }

    public Match getMatch() {
        return readEnum("match", Match.class, Match.SIMILAR);
    }

    public boolean testLock(Lock lock) {
        Lock current = getLock();
        return current == Lock.NOLOCK || current == lock;
    }

    public boolean matchBlocks(Block first, Block second) {
        return switch (getMatch()) {
            case EXACT -> first == second;
            case SIMILAR -> SimilarBlocks.match(first, second);
            case ANY -> first != Blocks.AIR && second != Blocks.AIR;
        };
    }

    public boolean set(String key, String value) {
        return switch (key) {
            case "lock" -> setEnum("lock", parseEnum(Lock.class, value, getLock()));
            case "direction" -> setEnum("direction", parseEnum(DirectionMode.class, value, getDirection()));
            case "replace" -> {
                writeTag().putBoolean("replace", "yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value));
                yield true;
            }
            case "match" -> setEnum("match", parseEnum(Match.class, value, getMatch()));
            default -> false;
        };
    }

    public String getValueString(String key) {
        return switch (key) {
            case "lock" -> getLock().name().toLowerCase();
            case "direction" -> getDirection().name().toLowerCase();
            case "replace" -> getReplace() ? "yes" : "no";
            case "match" -> getMatch().name().toLowerCase();
            default -> "";
        };
    }

    public void next(String key) {
        switch (key) {
            case "lock" -> cycleEnum("lock", Lock.class, getLock());
            case "direction" -> cycleEnum("direction", DirectionMode.class, getDirection());
            case "replace" -> writeTag().putBoolean("replace", !getReplace());
            case "match" -> cycleEnum("match", Match.class, getMatch());
            default -> {
            }
        }
    }

    public String fingerprint() {
        return getLock() + "|" + getDirection() + "|" + getReplace() + "|" + getMatch();
    }

    private CompoundTag readTag() {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(TAG)) {
            return null;
        }
        return root.getCompound(TAG);
    }

    private CompoundTag writeTag() {
        return stack.getOrCreateTagElement(TAG);
    }

    private <E extends Enum<E>> E readEnum(String key, Class<E> type, E fallback) {
        CompoundTag tag = readTag();
        if (tag == null || !tag.contains(key)) {
            return fallback;
        }
        return parseEnum(type, tag.getString(key), fallback);
    }

    private <E extends Enum<E>> boolean setEnum(String key, E value) {
        writeTag().putString(key, value.name().toLowerCase());
        return true;
    }
    private <E extends Enum<E>> void cycleEnum(String key, Class<E> type, E current) {
        E[] values = type.getEnumConstants();
        setEnum(key, values[(current.ordinal() + 1) % values.length]);
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String value, E fallback) {
        if (value == null || value.isEmpty()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}
