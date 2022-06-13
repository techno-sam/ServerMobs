package com.slimeist.server_mobs.entities;

import com.slimeist.server_mobs.mixin.EntityAccessor;
import com.slimeist.server_mobs.mixin.SnowGolemEntityAccessor;
import com.slimeist.server_mobs.server_rendering.entity.IServerRenderedEntity;
import com.slimeist.server_mobs.server_rendering.model.BakedServerEntityModel;
import eu.pb4.holograms.mixin.accessors.SlimeEntityAccessor;
import eu.pb4.polymer.api.entity.PolymerEntity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class GustEntity extends HostileEntity implements PolymerEntity, IServerRenderedEntity {
    private static Supplier<BakedServerEntityModel> bakedModelSupplier;
    private BakedServerEntityModel.Instance modelInstance;

    public GustEntity(EntityType<? extends GustEntity> entityType, World world) {
        super(entityType, world);
    }

    //Server Rendered Entity

    @Override
    public BakedServerEntityModel.Instance createModelInstance() {
        return getBakedModel().createInstance(this);
    }

    @Override
    public BakedServerEntityModel.Instance getModelInstance() {
        if (modelInstance == null) {
            modelInstance = createModelInstance();
        }
        return modelInstance;
    }

    @Override
    public BakedServerEntityModel getBakedModel() {
        return bakedModelSupplier.get();
    }

    @Override
    public void setupAngles() {
        this.getModelInstance().setPartRotation("base.top_spin", new EulerAngle(0, (360*(this.age/(20*5f)))%360, 0));
        this.getModelInstance().setPartRotation("base.middle_spin", new EulerAngle(0, -(360*(this.age/(20*2.5f)))%360, 0));
        this.getModelInstance().setPartRotation("base.bottom_spin", new EulerAngle(0, (360*(this.age/(20*1.25f)))%360, 0));
    }

    public static void setBakedModelSupplier(Supplier<BakedServerEntityModel> bakedModel) {
        bakedModelSupplier = bakedModel;
    }

    //Polymer Entity

    @Override
    public EntityType<?> getPolymerEntityType() {
        return EntityType.SLIME;
    }

    protected static byte modifyFlag(byte flag, int index, boolean value) {
        byte out;
        if (value) {
            out = (byte)(flag | 1 << index);
        } else {
            out = (byte)(flag & ~(1 << index));
        }
        return out;
    }

    @Override
    public void modifyTrackedData(List<DataTracker.Entry<?>> data) {
        data.add(new DataTracker.Entry<>(SlimeEntityAccessor.getSlimeSize(), 2));
        byte baseFlag = 0;
        baseFlag = modifyFlag(baseFlag, 5, true); //set invisible
        baseFlag = modifyFlag(baseFlag, 6, false); //set glowing
        data.add(new DataTracker.Entry<>(FLAGS, baseFlag));
        data.add(new DataTracker.Entry<>(EntityAccessor.getSILENT(), false));
        data.add(new DataTracker.Entry<>(EntityAccessor.getCUSTOM_NAME(), Optional.of(new LiteralText("Gust"))));
        data.add(new DataTracker.Entry<>(EntityAccessor.getNAME_VISIBLE(), true));
    }

    //Actual Mob

    public static DefaultAttributeContainer.Builder createGustAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 16.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f);
    }

    @Override
    public void tick() {
        super.tick();
        this.getModelInstance().updateHologram();
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height*0.75f;
    }
}