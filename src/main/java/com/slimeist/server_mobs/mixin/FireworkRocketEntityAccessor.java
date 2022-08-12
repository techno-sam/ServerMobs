package com.slimeist.server_mobs.mixin;

import net.minecraft.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

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
