package com.slimeist.server_mobs.entities;

import com.slimeist.server_mobs.mixin.EntityAccessor;
import com.slimeist.server_mobs.mixin.SnowGolemEntityAccessor;
import com.slimeist.server_mobs.server_rendering.entity.IServerRenderedEntity;
import com.slimeist.server_mobs.server_rendering.model.BakedServerEntityModel;
import eu.pb4.polymer.api.entity.PolymerEntity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Supplier;

//use HologramAPI for model display

public class CrocodileEntity extends GolemEntity implements PolymerEntity, IServerRenderedEntity {
    private static final float EYE_HEIGHT = 1.7f;
    private static Supplier<BakedServerEntityModel> bakedModelSupplier;
    private BakedServerEntityModel.Instance modelInstance;

    public CrocodileEntity(EntityType<? extends CrocodileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public EntityType<?> getPolymerEntityType() {
        return EntityType.BAT;
    }

    @Override
    protected void initGoals() {
        /*this.goalSelector.add(1, new ProjectileAttackGoal(this, 1.25, 20, 10.0f));
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 1.0, 1.0000001E-5f));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, MobEntity.class, 10, true, false, entity -> entity instanceof Monster));*/
    }

    public static DefaultAttributeContainer.Builder createCrocodileAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 5.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4f);
    }

    @Override
    public void tick() {
        super.tick();
        this.getModelInstance().updateHologram();
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return EYE_HEIGHT;
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
        data.add(new DataTracker.Entry<>(SnowGolemEntityAccessor.getSNOW_GOLEM_FLAGS(), (byte) 0));
        byte baseFlag = 0;
        baseFlag = modifyFlag(baseFlag, 5, true); //set invisible
        baseFlag = modifyFlag(baseFlag, 6, false); //set glowing
        data.add(new DataTracker.Entry<>(FLAGS, baseFlag));
        data.add(new DataTracker.Entry<>(EntityAccessor.getSILENT(), true));
        //data.add(new DataTracker.Entry<>(EntityAccessor.getCUSTOM_NAME(), Optional.of(new LiteralText("Gold Golem"))));
        //data.add(new DataTracker.Entry<>(EntityAccessor.getNAME_VISIBLE(), true));
    }

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
    public void updateAngles() {
        //this.getModelInstance().setPartRotation("base.main", new EulerAngle(this.getPitch(), this.headYaw, 0));
        /*float cycle_time = 40;
        boolean backwards = (this.age % (cycle_time*2)) >= cycle_time;
        float pitch = MathHelper.lerp((this.age%cycle_time)/cycle_time, 0, 45);
        if (backwards)
            pitch = MathHelper.lerp((this.age%cycle_time)/cycle_time, 45, 0);
//        pitch = 0;
        this.getModelInstance().setPartRotation("base.main", new EulerAngle(pitch, 0, 0));*/

        if (!this.isAlive()) {
            this.getModelInstance().defaultDeath();
        } else {
            this.getModelInstance().setDamageFlash(this.hurtTime>0);
        }
    }

    @Override
    public void initAngles() {
        String[] parent_locals = new String[]{
                "base.head",
                "base.head.jaw",
                "base.tail",
                "base.tail.tail2",
                "base.tail.tail2.tail3"
        };
        for (String path : parent_locals) {
            this.getModelInstance().setPartParentLocal(path, true);
        }
        //this.getModelInstance().setPartParentLocal("base.main.oversize", true);
        //this.getModelInstance().setPartParentLocal("base.main.oversize.leaves", true);
    }

    public static void setBakedModelSupplier(Supplier<BakedServerEntityModel> bakedModel) {
        bakedModelSupplier = bakedModel;
    }
}
