package net.yiran.tetrajs.kubejs.builders.statbar;

import net.yiran.tetrajs.util.StatGetterWrapper;
import net.yiran.tetrajs.util.TooltipGetterMultiValueWrapper;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.bar.GuiStatIndicator;
import se.mickelus.tetra.gui.stats.getter.*;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStatBarBuilder<THIS extends IStatBarBuilder<?>> implements IStatBarBuilder<THIS> {
    public int max = 100;
    public int min = 0;
    public boolean segmented = false;
    public boolean split = false;
    public boolean inverted = false;
    public ILabelGetter labelGetter = LabelGetterBasic.integerLabel;
    public IStatGetter statGetter;
    public List<IStatGetter> statGetters = new ArrayList<>();
    public List<IStatGetter> extendStatGetters = new ArrayList<>();
    public List<IStatFormat> statFormats = new ArrayList<>();
    public List<GuiStatIndicator> statIndicators = new ArrayList<>();
    public StatGetterWrapper.ShowCheck showCheck;

    public THIS setShowCheck(StatGetterWrapper.ShowCheck showCheck) {
        this.showCheck = showCheck;
        return (THIS) this;
    }

    public THIS addTooltipStatGetter(IStatGetter statGetter, IStatFormat statFormat) {
        statGetters.add(statGetter);
        statFormats.add(statFormat);
        return (THIS) this;
    }

    public THIS addIntegerTooltip(IStatGetter statGetter) {
        return addTooltipStatGetter(statGetter, StatFormat.noDecimal);
    }

    public THIS addOneDecimalTooltip(IStatGetter statGetter) {
        return addTooltipStatGetter(statGetter, StatFormat.oneDecimal);
    }

    public THIS addTowDecimalTooltip(IStatGetter statGetter) {
        return addTooltipStatGetter(statGetter, StatFormat.twoDecimal);
    }

    public THIS addAbbreviateTooltip(IStatGetter statGetter) {
        return addTooltipStatGetter(statGetter, StatFormat.abbreviate);
    }

    public THIS addExtendGetter(IStatGetter statGetter) {
        extendStatGetters.add(statGetter);
        return (THIS) this;
    }

    public THIS addStatIndicator(GuiStatIndicator statIndicator) {
        statIndicators.add(statIndicator);
        return (THIS) this;
    }

    @Override
    public IStatGetter getStatGetter() {
        return statGetter;
    }

    @Override
    public THIS setStatGetter(IStatGetter statGetter) {
        this.statGetter = statGetter;
        return (THIS) this;
    }

    @Override
    public int getMax() {
        return max;
    }

    @Override
    public THIS setMax(int max) {
        this.max = max;
        return (THIS) this;
    }

    @Override
    public int getMin() {
        return min;
    }

    @Override
    public THIS setMin(int min) {
        this.min = min;
        return (THIS) this;
    }

    @Override
    public boolean getSegmented() {
        return segmented;
    }

    @Override
    public THIS setSegmented(boolean segmented) {
        this.segmented = segmented;
        return (THIS) this;
    }

    @Override
    public boolean getSplit() {
        return split;
    }

    @Override
    public THIS setSplit(boolean split) {
        this.split = split;
        return (THIS) this;
    }

    @Override
    public boolean getInverted() {
        return inverted;
    }

    @Override
    public THIS setInverted(boolean inverted) {
        this.inverted = inverted;
        return (THIS) this;
    }

    @Override
    public ILabelGetter getLabelGetter() {
        return labelGetter;
    }

    @Override
    public THIS setLabelGetter(ILabelGetter labelGetter) {
        this.labelGetter = labelGetter;
        return (THIS) this;
    }

    public THIS setLabelGetter(LABELS label) {
        this.labelGetter = label.getLabelGetter();
        return (THIS) this;
    }

    public abstract void checkBuildPre();

    @Override
    public GuiStatBar build() {
        checkBuildPre();
        GuiStatBar result = new GuiStatBar(
                0, 0, 59, getKey(),
                min, max, segmented, split, inverted,
                showCheck == null ? statGetter : new StatGetterWrapper(statGetter, showCheck), labelGetter, new TooltipGetterMultiValueWrapper(
                getLangKey(),
                statGetters.toArray(new IStatGetter[0]),
                statFormats.toArray(new IStatFormat[0]),
                extendStatGetters.toArray(new IStatGetter[0])
        ));
        result.setIndicators(statIndicators.toArray(new GuiStatIndicator[0]));
        return result;
    }

    public enum LABELS {
        integerLabel(LabelGetterBasic.integerLabel),
        integerLabelInverted(LabelGetterBasic.integerLabelInverted),
        decimalLabel(LabelGetterBasic.decimalLabel),
        singleDecimalLabel(LabelGetterBasic.singleDecimalLabel),
        decimalLabelInverted(LabelGetterBasic.decimalLabelInverted),
        percentageLabel(LabelGetterBasic.percentageLabel),
        percentageLabelInverted(LabelGetterBasic.percentageLabelInverted),
        percentageLabelDecimal(LabelGetterBasic.percentageLabelDecimal),
        percentageLabelDecimalInverted(LabelGetterBasic.percentageLabelDecimalInverted);
        final ILabelGetter labelGetter;

        LABELS(ILabelGetter labelGetter) {
            this.labelGetter = labelGetter;
        }

        public ILabelGetter getLabelGetter() {
            return this.labelGetter;
        }

    }
}
