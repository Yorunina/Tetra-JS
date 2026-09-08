package net.yiran.tetrajs.kubejs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class StealthVisibilityEventJS extends EventJS {
    public LivingEntity sneaker;
    public Entity lookingEntity;
    public double baseModifier;
    public double guiseModifier;
    public double visibility;
    public List<String> matchedGuises;
    private boolean visibilityModified;

    public StealthVisibilityEventJS(
            LivingEntity sneaker,
            Entity lookingEntity,
            double baseModifier,
            double guiseModifier,
            List<String> matchedGuises
    ) {
        this.sneaker = sneaker;
        this.lookingEntity = lookingEntity;
        this.baseModifier = baseModifier;
        this.guiseModifier = guiseModifier;
        this.matchedGuises = new ArrayList<>(matchedGuises);
        this.visibility = baseModifier * guiseModifier;
    }

    public LivingEntity getSneaker() {
        return sneaker;
    }

    public Entity getLookingEntity() {
        return lookingEntity;
    }

    public double getBaseModifier() {
        return baseModifier;
    }

    public double getGuiseModifier() {
        return guiseModifier;
    }

    public void setGuiseModifier(double guiseModifier) {
        this.guiseModifier = guiseModifier;
        if (!this.visibilityModified) {
            this.visibility = this.baseModifier * this.guiseModifier;
        }
    }

    public void addGuiseModifier(double extraMultiplier) {
        setGuiseModifier(this.guiseModifier * extraMultiplier);
    }

    public double getVisibility() {
        return visibility;
    }

    public void setVisibility(double visibility) {
        this.visibility = visibility;
        this.visibilityModified = true;
    }

    public List<String> getMatchedGuises() {
        return matchedGuises;
    }
}
