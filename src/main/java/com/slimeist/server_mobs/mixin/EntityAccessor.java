package com.slimeist.server_mobs.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor
    static TrackedData<Integer> getAIR() {
        throw new AssertionError();
    }

    @Accessor
    static TrackedData<Optional<Text>> getCUSTOM_NAME() {
        throw new AssertionError();
    }

    @Accessor
    static TrackedData<Boolean> getNAME_VISIBLE() {
        throw new AssertionError();
    }

    @Accessor
    static TrackedData<Boolean> getSILENT() {
        throw new AssertionError();
    }

    @Accessor
    static TrackedData<Boolean> getNO_GRAVITY() {
        throw new AssertionError();
    }

    @Accessor
    static TrackedData<EntityPose> getPOSE() {
        throw new AssertionError();
    }

    @Accessor
    static TrackedData<Integer> getFROZEN_TICKS() {
        throw new AssertionError();
    }

    @Accessor
    void setHasVisualFire(boolean hasVisualFire);
}
