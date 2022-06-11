package com.slimeist.server_mobs.mixin;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.SnowGolemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SnowGolemEntity.class)
public interface SnowGolemEntityAccessor {
    @Accessor
    static TrackedData<Byte> getSNOW_GOLEM_FLAGS() {
        throw new AssertionError();
    }
}
