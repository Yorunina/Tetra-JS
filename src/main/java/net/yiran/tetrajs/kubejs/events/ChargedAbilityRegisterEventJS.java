package net.yiran.tetrajs.kubejs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import net.yiran.tetrajs.abilities.KubeChargedAbilityEffect;

import java.util.ArrayList;
import java.util.List;

public class ChargedAbilityRegisterEventJS extends EventJS {
    private final List<KubeChargedAbilityEffect> abilities = new ArrayList<>();

    public KubeChargedAbilityEffect register(String effect) {
        var ability = new KubeChargedAbilityEffect(effect);
        abilities.add(ability);
        return ability;
    }

    public List<KubeChargedAbilityEffect> getAbilities() {
        return abilities;
    }
}
