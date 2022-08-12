package com.slimeist.server_mobs.mixin;

import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.EulerAngle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArmorStandEntity.class)
public interface ArmorStandEntityAccessor {
    @Accessor
    static EulerAngle getDEFAULT_HEAD_ROTATION() {
        throw new AssertionError();
    }

    @Accessor
    static EulerAngle getDEFAULT_BODY_ROTATION() {
        throw new AssertionError();
    }

    @Accessor
    static EulerAngle getDEFAULT_LEFT_ARM_ROTATION() {
        throw new AssertionError();
    }

    @Accessor
    static EulerAngle getDEFAULT_RIGHT_ARM_ROTATION() {
        throw new AssertionError();
    }

    @Accessor
    static EulerAngle getDEFAULT_LEFT_LEG_ROTATION() {
        throw new AssertionError();
    }

    @Accessor
    static EulerAngle getDEFAULT_RIGHT_LEG_ROTATION() {
        throw new AssertionError();
    }

    @Invoker("setMarker")
    void invokeSetMarker(boolean marker);

    @Invoker("setSmall")
    void invokeSetSmall(boolean small);
}
