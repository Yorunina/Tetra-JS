package net.yiran.tetrajs.api;

import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.yiran.tetrajs.util.StatGetterWrapper;
import net.yiran.tetrajs.util.TooltipGetterMultiValueWrapper;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.stats.getter.*;

import java.util.ArrayList;
import java.util.List;

public class StatBarHelper {
    public static StatBarHelper INSTANCE = new StatBarHelper();

    public IStatGetter effectLevel(ItemEffect effect, double multiplier, double base) {
        return new StatGetterEffectLevel(effect, multiplier, base);
    }

    public IStatGetter effectLevel(ItemEffect effect, double multiplier) {
        return effectLevel(effect, multiplier, 0);
    }

    public IStatGetter effectLevel(ItemEffect effect) {
        return effectLevel(effect, 1);
    }

    public IStatGetter effectEfficiency(ItemEffect effect, double multiplier, double base) {
        return new StatGetterEffectEfficiency(effect, multiplier, base);
    }

    public IStatGetter effectEfficiency(ItemEffect effect, double multiplier) {
        return effectEfficiency(effect, multiplier, 0);
    }

    public IStatGetter effectEfficiency(ItemEffect effect) {
        return effectEfficiency(effect, 1);
    }

    public IStatGetter sum(double amount, IStatGetter... statGetter) {
        return new StatGetterAdd(amount, statGetter);
    }

    public IStatGetter sum(IStatGetter... statGetter) {
        return sum(0, statGetter);
    }

    public IStatGetter multiply(double multiplier, IStatGetter... statGetter) {
        return new StatGetterMultiply(multiplier, statGetter);
    }

    public IStatGetter multiply(IStatGetter... statGetter) {
        return multiply(1, statGetter);
    }

    public IStatGetter attribute(Attribute attribute, boolean ignoreBase, boolean ignoreBonuses, double offset) {
        return new StatGetterAttribute(attribute, ignoreBase, ignoreBonuses, offset);
    }

    public IStatGetter attribute(Attribute attribute, boolean ignoreBase, boolean ignoreBonuses) {
        return attribute(attribute, ignoreBase, ignoreBonuses, 0);
    }

    public IStatGetter attribute(Attribute attribute, boolean ignoreBase) {
        return attribute(attribute, ignoreBase, false);
    }

    public IStatGetter attribute(Attribute attribute) {
        return attribute(attribute, false);
    }

    public IStatGetter attributeAddition(Attribute attribute) {
        return new StatGetterAttributeAddition(attribute, true, false);
    }

    public IStatGetter attributeMultiply(Attribute attribute) {
        return sum(-1, new StatGetterAttributeMultiply(attribute, false));
    }

    public IStatGetter integrity() {
        return new StatGetterIntegrity();
    }

    public IStatGetter magicCapacity(IStatGetter... statGetter) {
        return new StatGetterMagicCapacity();
    }

    public IStatGetter showWrapper(IStatGetter statGetter, StatGetterWrapper.ShowCheck showCheck) {
        return new StatGetterWrapper(statGetter, showCheck);
    }

    @Info("获取一个TooltipGetter的Builder，用法与注册StatBar一样")
    public TooltipGetterBuilder newTooltipGetter(String langKey) {
        return new TooltipGetterBuilder(langKey);
    }

    public static class TooltipGetterBuilder {
        public String langKey;
        public List<IStatGetter> statGetters = new ArrayList<>();
        public List<IStatGetter> extendStatGetters = new ArrayList<>();
        public List<IStatFormat> statFormats = new ArrayList<>();

        public TooltipGetterBuilder(String langKey) {
            this.langKey = langKey;
        }

        public TooltipGetterBuilder addTooltipStatGetter(IStatGetter statGetter, IStatFormat statFormat) {
            statGetters.add(statGetter);
            statFormats.add(statFormat);
            return this;
        }

        public TooltipGetterBuilder addIntegerTooltip(IStatGetter statGetter) {
            return addTooltipStatGetter(statGetter, StatFormat.noDecimal);
        }

        public TooltipGetterBuilder addOneDecimalTooltip(IStatGetter statGetter) {
            return addTooltipStatGetter(statGetter, StatFormat.oneDecimal);
        }

        public TooltipGetterBuilder addTowDecimalTooltip(IStatGetter statGetter) {
            return addTooltipStatGetter(statGetter, StatFormat.twoDecimal);
        }

        public TooltipGetterBuilder addAbbreviateTooltip(IStatGetter statGetter) {
            return addTooltipStatGetter(statGetter, StatFormat.abbreviate);
        }

        public TooltipGetterBuilder addExtendGetter(IStatGetter statGetter) {
            extendStatGetters.add(statGetter);
            return this;
        }

        public ITooltipGetter build() {
            return new TooltipGetterMultiValueWrapper(
                    langKey,
                    statGetters.toArray(new IStatGetter[0]),
                    statFormats.toArray(new IStatFormat[0]),
                    extendStatGetters.toArray(new IStatGetter[0])
            );
        }
    }

}
