package com.slimeist.server_mobs.entities;

import com.slimeist.server_mobs.mixin.EntityAccessor;
import com.slimeist.server_mobs.mixin.SnowGolemEntityAccessor;
import com.slimeist.server_mobs.server_rendering.entity.IServerRenderedEntity;
import com.slimeist.server_mobs.server_rendering.model.BakedServerEntityModel;
import eu.pb4.holograms.mixin.accessors.SlimeEntityAccessor;
import eu.pb4.polymer.api.entity.PolymerEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
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
    public void updateAngles() {
        this.getModelInstance().setPartRotation("base.top_spin", new EulerAngle(0, (360*(this.age/(20*5f)))%360, 0));
        if (this.isAttacking()) {
            this.getModelInstance().setPartRotation("base.middle_spin", new EulerAngle(0, (360 * (this.age / (20 * 3.5f))) % 360, 0));
        } else {
            this.getModelInstance().setPartRotation("base.middle_spin", new EulerAngle(0, -(360 * (this.age / (20 * 2.5f))) % 360, 0));
        }
        this.getModelInstance().setPartRotation("base.bottom_spin", new EulerAngle(0, (360*(this.age/(20*1.25f)))%360, 0));

        this.getModelInstance().handleDamageFlash(this);
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
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.5);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, VillagerEntity.class, true, true));
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

    @Override
    public void onAttacking(Entity target) {
        super.onAttacking(target);
        target.addVelocity(0.0d, 2.0d, 0.0d);
    }

    //Sounds

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLOCK_AMETHYST_BLOCK_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK;
    }

    protected SoundEvent getStepSound() {
        return SoundEvents.BLOCK_AMETHYST_BLOCK_STEP;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(this.getStepSound(), 0.15f, 1.0f);
    }
}
