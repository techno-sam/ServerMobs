package com.slimeist.server_mobs.api.server_rendering.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public abstract class AbstractLivingServerRenderedEntity extends LivingEntity implements IServerRenderedEntity {
    protected AbstractLivingServerRenderedEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
}
