package net.yiran.tetrajs.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.yiran.tetrajs.core.mixins.GuiStatBarAccessor;
import net.yiran.tetrajs.core.mixins.GuiStatBarToolAccessor;
import net.yiran.tetrajs.core.mixins.StatIndicatorStoreAccessor;
import net.yiran.tetrajs.core.mixins.WorkbenchStatsGuiAccessor;
import se.mickelus.tetra.blocks.workbench.gui.GuiTool;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.bar.GuiStatBarTool;
import se.mickelus.tetra.gui.stats.bar.GuiStatIndicator;
import se.mickelus.tetra.gui.stats.data.StatIndicatorStore;

/**
 * Bridges stat indicators defined under {@code assets/tetra/stat_indicators/} into stat bars that
 * already exist in Tetra, including the tool level bars that are created in code.
 * <p>
 * Client-side only. Requests are queued and applied whenever a {@code WorkbenchStatsGui} is constructed,
 * so scripts may call the API before the stat bars are ready. Resource changes require a client restart
 * before they can be injected into existing bars.
 */
public final class StatBarInjector {

    private static final Map<String, List<String>> KEY_REQUESTS = new LinkedHashMap<>();
    private static final List<String> TOOL_LEVEL_REQUESTS = new ArrayList<>();
    private static final Map<String, List<String>> TOOL_ACTION_REQUESTS = new LinkedHashMap<>();
    private static final Map<GuiStatBar, Set<String>> APPLIED = new IdentityHashMap<>();

    private StatBarInjector() {
    }

    /**
     * Appends the indicators found under {@code indicatorPrefix} to the bar with the given label key.
     *
     * @param barKey          bar label key, for example {@code tetra.stats.attack_damage}
     * @param indicatorPrefix resource prefix, for example {@code tetra:flat_extraction}
     */
    public static void inject(String barKey, String indicatorPrefix) {
        if (barKey == null || barKey.isEmpty() || indicatorPrefix == null || indicatorPrefix.isEmpty()) {
            return;
        }
        KEY_REQUESTS.computeIfAbsent(barKey, key -> new ArrayList<>()).add(indicatorPrefix);
        applyPending();
    }

    /**
     * Appends the indicators found under {@code indicatorPrefix} to every tool level bar
     * (pickaxe, axe, shovel, ...). Tool level bars do not expose a label key, so they are matched by type.
     *
     * @param indicatorPrefix resource prefix, for example {@code tetra:flat_extraction}
     */
    public static void injectToolLevels(String indicatorPrefix) {
        if (indicatorPrefix == null || indicatorPrefix.isEmpty()) {
            return;
        }
        if (!TOOL_LEVEL_REQUESTS.contains(indicatorPrefix)) {
            TOOL_LEVEL_REQUESTS.add(indicatorPrefix);
        }
        applyPending();
    }

    /**
     * Appends the indicators found under {@code indicatorPrefix} to the tool level bar of one ToolAction,
     * for example only the pickaxe bar.
     *
     * @param toolAction      ToolAction name, for example {@code pickaxe_dig}
     * @param indicatorPrefix resource prefix, for example {@code tetra:flat_extraction}
     */
    public static void injectToolLevel(String toolAction, String indicatorPrefix) {
        if (toolAction == null || toolAction.isEmpty() || indicatorPrefix == null || indicatorPrefix.isEmpty()) {
            return;
        }
        List<String> prefixes = TOOL_ACTION_REQUESTS.computeIfAbsent(toolAction, key -> new ArrayList<>());
        if (!prefixes.contains(indicatorPrefix)) {
            prefixes.add(indicatorPrefix);
        }
        applyPending();
    }

    /** Applies every queued request. Called from the {@code WorkbenchStatsGui} constructor. */
    public static void applyPending() {
        if (KEY_REQUESTS.isEmpty() && TOOL_LEVEL_REQUESTS.isEmpty() && TOOL_ACTION_REQUESTS.isEmpty()) {
            return;
        }
        try {
            applyInternal();
        } catch (RuntimeException exception) {
            exception.printStackTrace(System.err);
        }
    }

    /** Returns the label keys of the known stat bars, useful for discovery. */
    public static List<String> listBars() {
        List<?> bars = WorkbenchStatsGuiAccessor.tetrajs$getStaticBars();
        if (bars == null) {
            return Collections.emptyList();
        }
        List<String> keys = new ArrayList<>();
        for (Object element : bars) {
            if (!(element instanceof GuiStatBar bar)) {
                continue;
            }
            String labelKey = ((GuiStatBarAccessor) bar).tetrajs$getLabelKey();
            keys.add(labelKey != null ? labelKey : bar.getClass().getSimpleName());
        }
        return keys;
    }

    /** Returns the resource ids of the loaded stat indicators, useful for discovery. */
    public static List<String> listIndicators() {
        StatIndicatorStore store = StatIndicatorStoreAccessor.tetrajs$getInstance();
        if (store == null) {
            return Collections.emptyList();
        }
        Map<ResourceLocation, GuiStatIndicator> indicators =
            ((StatIndicatorStoreAccessor) store).tetrajs$getIndicatorMap();
        if (indicators == null) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<>();
        for (ResourceLocation location : indicators.keySet()) {
            ids.add(location.toString());
        }
        return ids;
    }

    private static void applyInternal() {
        List<?> bars = WorkbenchStatsGuiAccessor.tetrajs$getStaticBars();
        StatIndicatorStore store = StatIndicatorStoreAccessor.tetrajs$getInstance();
        if (bars == null || bars.isEmpty() || store == null) {
            return;
        }

        for (Object element : bars) {
            if (!(element instanceof GuiStatBar bar)) {
                continue;
            }
            GuiStatBarAccessor accessor = (GuiStatBarAccessor) bar;

            if (bar instanceof GuiStatBarTool) {
                for (String prefix : TOOL_LEVEL_REQUESTS) {
                    append(store, bar, accessor, prefix);
                }
                if (!TOOL_ACTION_REQUESTS.isEmpty()) {
                    String toolAction = resolveToolAction(bar);
                    if (toolAction != null) {
                        List<String> prefixes = TOOL_ACTION_REQUESTS.get(toolAction);
                        if (prefixes != null) {
                            for (String prefix : prefixes) {
                                append(store, bar, accessor, prefix);
                            }
                        }
                    }
                }
            }

            if (KEY_REQUESTS.isEmpty()) {
                continue;
            }
            String labelKey = accessor.tetrajs$getLabelKey();
            if (labelKey == null) {
                continue;
            }
            List<String> prefixes = KEY_REQUESTS.get(labelKey);
            if (prefixes == null) {
                continue;
            }
            for (String prefix : prefixes) {
                append(store, bar, accessor, prefix);
            }
        }
    }

    private static String resolveToolAction(GuiStatBar bar) {
        try {
            GuiTool icon = ((GuiStatBarToolAccessor) bar).tetrajs$getIcon();
            if (icon == null || icon.getToolAction() == null) {
                return null;
            }
            return icon.getToolAction().name();
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static void append(StatIndicatorStore store, GuiStatBar bar, GuiStatBarAccessor accessor, String prefix) {
        Set<String> applied = APPLIED.computeIfAbsent(bar, key -> new LinkedHashSet<>());
        if (applied.contains(prefix)) {
            return;
        }

        ResourceLocation location = parseLocation(prefix);
        if (location == null) {
            return;
        }

        GuiStatIndicator[] extra = store.getIndicatorsIn(location);
        if (extra == null || extra.length == 0) {
            return;
        }

        GuiStatIndicator[] current = accessor.tetrajs$getIndicators();
        GuiStatIndicator[] merged = new GuiStatIndicator[current.length + extra.length];
        System.arraycopy(current, 0, merged, 0, current.length);
        System.arraycopy(extra, 0, merged, current.length, extra.length);
        bar.setIndicators(merged);
        applied.add(prefix);
    }

    private static ResourceLocation parseLocation(String value) {
        int separator = value.indexOf(':');
        try {
            if (separator >= 0) {
                return new ResourceLocation(value.substring(0, separator), value.substring(separator + 1));
            }
            return new ResourceLocation("minecraft", value);
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
