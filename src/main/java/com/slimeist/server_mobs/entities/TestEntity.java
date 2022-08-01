package com.slimeist.server_mobs.entities;

import com.slimeist.server_mobs.ServerMobsMod;
import com.slimeist.server_mobs.mixin.EntityAccessor;
import com.slimeist.server_mobs.mixin.SnowGolemEntityAccessor;
import com.slimeist.server_mobs.server_rendering.entity.IServerRenderedEntity;
import com.slimeist.server_mobs.server_rendering.model.BakedServerEntityModel;
import eu.pb4.polymer.api.entity.PolymerEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

//use HologramAPI for model display

public class TestEntity extends GolemEntity implements PolymerEntity, RangedAttackMob, IServerRenderedEntity {
    private static final float EYE_HEIGHT = 1.7f;
    private static Supplier<BakedServerEntityModel> bakedModelSupplier;
    private BakedServerEntityModel.Instance modelInstance;

    public TestEntity(EntityType<? extends TestEntity> entityType, World world) {
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

    public static DefaultAttributeContainer.Builder createTestAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 5.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4f);
    }

    @Override
    public void tick() {
        super.tick();
        this.getModelInstance().updateHologram();
    }

    @Override
    public void attack(LivingEntity target, float pullProgress) {
        SnowballEntity snowballEntity = new SnowballEntity(this.world, this);
        double d = target.getEyeY() - (double)1.1f;
        double e = target.getX() - this.getX();
        double f = d - snowballEntity.getY();
        double g = target.getZ() - this.getZ();
        double h = Math.sqrt(e * e + g * g) * (double)0.2f;
        snowballEntity.setVelocity(e, f + h, g, 1.6f, 12.0f);
        this.playSound(SoundEvents.ENTITY_SNOW_GOLEM_SHOOT, 1.0f, 0.4f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.world.spawnEntity(snowballEntity);

        //ItemEntity itemEntity = new ItemEntity(this.world, this.getX(), this.getY(), this.getZ(), new ItemStack(Items.GOLDEN_APPLE, 1));
        //this.world.spawnEntity(itemEntity);
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
        float cycle_time = 40;
        boolean backwards = (this.age % (cycle_time*2)) >= cycle_time;
        float rot = MathHelper.lerp((this.age%cycle_time)/cycle_time, 0, 45);
        if (backwards)
            rot = MathHelper.lerp((this.age%cycle_time)/cycle_time, 45, 0);
//        rot = 0;
        this.getModelInstance().setPartRotation("base.main", new EulerAngle(rot, 0, 0));
        this.getModelInstance().setPartRelativeRotation("base.main.oversize", new EulerAngle(0, rot, 0));

        this.getModelInstance().handleDamageFlash(this);
    }

    @Override
    public void initAngles() {
        this.getModelInstance().setPartParentLocal("base.main.oversize", true);
        this.getModelInstance().setPartParentLocal("base.main.oversize.branch", true);
        this.getModelInstance().setPartParentLocal("base.main.oversize.leaves", true);
    }

    public static void setBakedModelSupplier(Supplier<BakedServerEntityModel> bakedModel) {
        bakedModelSupplier = bakedModel;
    }
}
