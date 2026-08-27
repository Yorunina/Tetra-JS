package net.yiran.tetrajs.kubejs.builders.statbar;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.yiran.tetrajs.api.StatBarHelper;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.StatGetterAttribute;
import se.mickelus.tetra.gui.stats.getter.StatGetterAttributeAddition;
import se.mickelus.tetra.gui.stats.getter.StatGetterMultiply;

public class AttributeStatBarBuilder extends AbstractStatBarBuilder<AttributeStatBarBuilder> {
    public Attribute attribute;
    public boolean ignoreBase = false;
    public boolean ignoreBonuses = false;
    public double offset = 0;
    public double multiplier = 1;
    public TYPE type = TYPE.COMMON;


    public AttributeStatBarBuilder(Attribute attribute) {
        this.attribute = attribute;
    }

    public AttributeStatBarBuilder setMultiplier(double multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    public AttributeStatBarBuilder setAttributeGetterType(TYPE type) {
        this.type = type;
        return this;
    }

    public AttributeStatBarBuilder setIgnoreBase(boolean ignoreBase) {
        this.ignoreBase = ignoreBase;
        return this;
    }

    public AttributeStatBarBuilder setIgnoreBonuses(boolean ignoreBonuses) {
        this.ignoreBonuses = ignoreBonuses;
        return this;
    }

    public AttributeStatBarBuilder setOffset(double offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public void checkBuildPre() {
        if (getStatGetter() == null) {
            IStatGetter attributeStatGetter = switch (type) {
                case COMMON -> new StatGetterAttribute(attribute, ignoreBase, ignoreBonuses, offset);
                case ADDITION -> new StatGetterAttributeAddition(attribute, true, false);
                case MULTIPLY -> StatBarHelper.INSTANCE.attributeMultiply(attribute);
            };
            if (multiplier != 1) {
                attributeStatGetter = new StatGetterMultiply(multiplier, attributeStatGetter);
            }
            setStatGetter(attributeStatGetter);
        }
        if (statGetters.isEmpty()) {
            addOneDecimalTooltip(getStatGetter());
        }
    }

    @Override
    public String getKey() {
        return "tetra.stats." + attribute.getDescriptionId();
    }

    public enum TYPE {
        COMMON,
        ADDITION,
        MULTIPLY
    }
}
