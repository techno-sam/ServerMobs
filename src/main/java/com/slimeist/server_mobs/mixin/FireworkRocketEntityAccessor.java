package com.slimeist.server_mobs.mixin;

import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.EulerAngle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FireworkRocketEntity.class)
public interface FireworkRocketEntityAccessor {
    @Accessor
    int getLife();
    @Accessor
    int getLifeTime();
    @Accessor
    void setLife(int life);
    @Accessor
    void setLifeTime(int lifeTime);
}
