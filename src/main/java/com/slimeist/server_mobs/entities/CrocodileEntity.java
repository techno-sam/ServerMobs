package com.slimeist.server_mobs.entities;

import com.google.common.collect.ImmutableSet;
import com.slimeist.server_mobs.ModBlockTags;
import com.slimeist.server_mobs.ServerMobsMod;
import com.slimeist.server_mobs.api.server_rendering.entity.IServerRenderedEntity;
import com.slimeist.server_mobs.api.server_rendering.model.BakedServerEntityModel;
import com.slimeist.server_mobs.mixin.EntityAccessor;
import eu.pb4.polymer.api.entity.PolymerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.AquaticMoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

//use HologramAPI for model display

public class CrocodileEntity extends HostileEntity implements PolymerEntity, IServerRenderedEntity {

    private static final EntityAttribute CHOMP_CHANCE = Registry.register(Registry.ATTRIBUTE, "server_mobs.crocodile.chomp_chance", new ClampedEntityAttribute("attribute.server_mobs.crocodile.chomp_chance", 0.0, 0.0, 1.0));

    private static Supplier<BakedServerEntityModel> bakedModelSupplier;
    private BakedServerEntityModel.Instance modelInstance;
    protected int chompTicks = 0;
    private double mouthAngleFactor = 0;
    protected int stunTicks;
    protected LivingEntity forcedTarget;

    private static final float MOVEMENT_SPEED = 0.3f;

    public CrocodileEntity(World world) {
        this(ServerMobsMod.CROCODILE, world);
    }

    public CrocodileEntity(EntityType<? extends CrocodileEntity> entityType, World world) {
        super(entityType, world);
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0f);
        this.experiencePoints = Monster.STRONG_MONSTER_XP;

        this.moveControl = new AquaticMoveControl(this, 40, 10, 0.13f, 0.4f, false);
        this.stepHeight = 1.0f;
    }

    public CrocodileEntity(World world, LivingEntity forcedTarget) {
        this(world);
        this.forcedTarget = forcedTarget;
        this.setTarget(this.forcedTarget);
    }

    @Override
    public EntityType<?> getPolymerEntityType() {
        return EntityType.TURTLE;
    }

    @Override
    protected void updateDespawnCounter() {
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (this.canMoveVoluntarily() && this.isTouchingWater()) {

            Vec3d velMul = new Vec3d(1, 1, 1); //prevent wild bobbing out of the water

            this.updateVelocity(this.getMovementSpeed(), movementInput);
            this.move(MovementType.SELF, this.getVelocity().multiply(velMul));
            this.setVelocity(this.getVelocity().multiply(0.9).multiply(velMul));
        } else {
            super.travel(movementInput);
        }
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.stunTicks > 0;
    }

    @Override
    public boolean canSee(Entity entity) {
        if (this.stunTicks > 0) {
            return false;
        }
        return super.canSee(entity);
    }

    @Override
    protected void knockback(LivingEntity target) {
        if (this.random.nextDouble() < 0.3) {
            this.stunTicks = 25 + this.random.nextInt(15);
            this.playSound(SoundEvents.ENTITY_RAVAGER_STUNNED, 1.0f, 1.0f);
            target.pushAwayFrom(this);
        } else {
            super.knockback(target);
        }
    }

    private Optional<BlockPos> findNearestFlute() {
        return BlockPos.findClosest(getBlockPos(), 8, 4, pos -> world.getBlockState((BlockPos)pos).isIn(ModBlockTags.CROCODILES_FEAR));
    }

    private boolean isFluteAround(BlockPos pos) {
        Optional<BlockPos> optional = findNearestFlute();
        return optional.isPresent() && optional.get().isWithinDistance(pos, 8.0);
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        if (isFluteAround(pos)) {
            return -1.0f;
        }
        return 0.0f;
    }

    @Override
    protected void initGoals() {
        //goalSelector.add(0, new BreatheAirGoal(this));
        goalSelector.add(0, new CrocodileSwimGoal(this));
        goalSelector.add(0, new CrocodileMoveIntoWaterGoal());

        goalSelector.add(1, new LookAtTargetGoal());
        goalSelector.add(1, new PounceAtTargetGoal(this, 0.16f));

        goalSelector.add(2, new CrocodileMeleeAttackGoal(this, 1.0d, true));
        goalSelector.add(2, new ChompGoal());

        goalSelector.add(3, new CrocodileWanderAroundGoal(this, 1.0d));
        goalSelector.add(3, new CrocodileSwimAroundGoal(this, 1.0d, 10));

        goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        goalSelector.add(5, new LookAroundGoal(this));


        targetSelector.add(1, new ForcedTargetGoal(this));

        if (forcedTarget == null) {
            targetSelector.add(2, new RevengeGoal(this));

            targetSelector.add(3, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, true, this::shouldTarget));

            targetSelector.add(4, new ActiveTargetGoal<>(this, WanderingTraderEntity.class, 10, false, true, this::shouldTargetAny));

            targetSelector.add(5, new ActiveTargetGoal<>(this, SheepEntity.class, 10, false, true, this::shouldTargetAny));
            targetSelector.add(5, new ActiveTargetGoal<>(this, PigEntity.class, 10, false, true, this::shouldTargetAny));
        }
    }

    private boolean shouldTargetAny(LivingEntity entity) {
        return this.forcedTarget == null;
    }

    private boolean shouldTarget(LivingEntity entity) {
        if (!shouldTargetAny(entity)) {
            return false;
        }
        double div = 1;
        if (entity.getEquippedStack(EquipmentSlot.HEAD).isOf(ServerMobsMod.CROCODILE_HEAD)) {
            div = 2;
        }
        return this.getBoundingBox().expand(entity.isTouchingWater() ? 12.0d / div : 7.0d / div).intersects(entity.getBoundingBox());
    }

    public static DefaultAttributeContainer.Builder createCrocodileAttributes() {
        return createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 17.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
                .add(CHOMP_CHANCE);
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        entityData = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
        this.getAttributeInstance(CHOMP_CHANCE).setBaseValue(random.nextDouble());
        return entityData;
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new CrocodileMobNavigation(this, world);
    }

    boolean isTargetingOnLand() {
        LivingEntity target = this.getTarget();
        return target != null && !target.isTouchingWater();
    }

    @Override
    public void tick() {
        super.tick();
        this.getModelInstance().updateHologram();
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.isAlive())
            return;

        if (this.isImmobile()) {
            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.0);
        } else {
            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(MathHelper.lerp(
                    0.1,
                    this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).getBaseValue(),
                    MOVEMENT_SPEED
            ));
        }

        if (this.stunTicks > 0) {
            --this.stunTicks;
            this.spawnStunnedParticles();
        }
    }

    private void spawnStunnedParticles() {
        if (this.random.nextInt(6) == 0 && this.world instanceof ServerWorld serverWorld) {
            double distance = this.getWidth();
            double x = this.getX() - distance * Math.sin(this.bodyYaw * ((float) Math.PI / 180)) + (this.random.nextDouble() * 0.6 - 0.3);
            double y = this.getY() + (double) this.getHeight() - 0;
            double z = this.getZ() + distance * Math.cos(this.bodyYaw * ((float) Math.PI / 180)) + (this.random.nextDouble() * 0.6 - 0.3);
            for (int i = 0; i < 5; i++) {
                float innerAngle = (0.01745329251F * (this.bodyYaw + age * 5) * (i + 1));
                double extraX = 0.5F * MathHelper.sin((float) (Math.PI + innerAngle));
                double extraZ = 0.5F * MathHelper.cos(innerAngle);
                serverWorld.spawnParticles(ParticleTypes.CRIT, x + extraX, y, z + extraZ, 0, 0, 0, 0, 0);
            }
        }
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return super.getActiveEyeHeight(pose, dimensions);
    }

    protected static byte modifyFlag(byte flag, int index, boolean value) {
        byte out;
        if (value) {
            out = (byte) (flag | 1 << index);
        } else {
            out = (byte) (flag & ~(1 << index));
        }
        return out;
    }

    @Override
    public void modifyTrackedData(List<DataTracker.Entry<?>> data) {
        byte baseFlag = 0;
        baseFlag = modifyFlag(baseFlag, 5, true); //set invisible
        baseFlag = modifyFlag(baseFlag, 6, this.isGlowing()); //set glowing
        baseFlag = modifyFlag(baseFlag, ON_FIRE_FLAG_INDEX, this.doesRenderOnFire());
        data.add(new DataTracker.Entry<>(FLAGS, baseFlag));
        data.add(new DataTracker.Entry<>(EntityAccessor.getSILENT(), true));
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
        // Body Rotation
        this.getModelInstance().setPartPivot("base.body", Vec3d.ZERO);
        this.getModelInstance().setPartRotation("base.body", new EulerAngle(0, this.bodyYaw, 0));

        this.getModelInstance().setPartRelativeRotation("base.body.head", new EulerAngle(this.getPitch(), this.headYaw - this.bodyYaw, 0));

        //Leg movement
        float g = 0.5f;
        float limbDistance = MathHelper.lerp(g, this.lastLimbDistance, this.limbDistance);
        float limbAngle = this.limbAngle - this.limbDistance * (1.0f - g);

        if (limbDistance > 1.0f) {
            limbDistance = 1.0f;
        }

        float rightHindLeg = (float) Math.toDegrees(MathHelper.cos(limbAngle * 0.6662f) * 1.4f * limbDistance);
        float leftHindLeg = (float) Math.toDegrees(MathHelper.cos(limbAngle * 0.6662f + (float) Math.PI) * 1.4f * limbDistance);
        float rightFrontLeg = (float) Math.toDegrees(MathHelper.cos(limbAngle * 0.6662f + (float) Math.PI) * 1.4f * limbDistance);
        float leftFrontLeg = (float) Math.toDegrees(MathHelper.cos(limbAngle * 0.6662f) * 1.4f * limbDistance);

        this.getModelInstance().setPartRelativeRotation("base.body.tail.leg_back_right", new EulerAngle(rightHindLeg, 0, 0));
        this.getModelInstance().setPartRelativeRotation("base.body.tail.leg_back_left", new EulerAngle(leftHindLeg, 0, 0));
        this.getModelInstance().setPartRelativeRotation("base.body.leg_front_right", new EulerAngle(rightFrontLeg, 0, 0));
        this.getModelInstance().setPartRelativeRotation("base.body.leg_front_left", new EulerAngle(leftFrontLeg, 0, 0));

        //Mouth
        this.getModelInstance().setPartRelativeRotation("base.body.head.jaw", new EulerAngle((float) this.mouthAngleFactor * -50, 0, 0));

        // Damage flash
        this.getModelInstance().handleDamageFlash(this);
    }

    @Override
    public void initAngles() {
        String[] parent_locals = new String[]{
                "base.body.head",
                "base.body.head.jaw",
                "base.body.leg_front_left",
                "base.body.leg_front_right",
                "base.body.tail",
                "base.body.tail.leg_back_left",
                "base.body.tail.leg_back_right",
                "base.body.tail.tail2"
        };
        for (String path : parent_locals) {
            this.getModelInstance().setPartParentLocal(path, true);
        }
    }

    public static void setBakedModelSupplier(Supplier<BakedServerEntityModel> bakedModel) {
        bakedModelSupplier = bakedModel;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("chompTicks", this.chompTicks);
        nbt.putInt("stunTicks", this.stunTicks);
        nbt.putDouble("mouthAngleFactor", this.mouthAngleFactor);
        if (this.forcedTarget != null) {
            nbt.putUuid("forcedTarget", this.forcedTarget.getUuid());
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("chompTicks", NbtCompound.INT_TYPE)) {
            this.chompTicks = nbt.getInt("chompTicks");
        }
        if (nbt.contains("stunTicks", NbtCompound.INT_TYPE)) {
            this.stunTicks = nbt.getInt("stunTicks");
        }
        if (nbt.contains("mouthAngleFactor", NbtCompound.DOUBLE_TYPE)) {
            this.mouthAngleFactor = nbt.getInt("mouthAngleFactor");
        }
        if (nbt.containsUuid("forcedTarget")) {
            UUID forcedTargetId = nbt.getUuid("forcedTarget");
            if (this.world instanceof ServerWorld serverWorld) {
                Entity forcedTarget = serverWorld.getEntity(forcedTargetId);
                if (forcedTarget instanceof LivingEntity livingForcedTarget && livingForcedTarget.isAlive()) {
                    this.forcedTarget = livingForcedTarget;
                } else {
                    this.dissolve();
                }
            }
        }
    }

    public boolean isChomping() {
        return this.chompTicks > 0;
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (this.chompTicks > 0) {
            this.chompTicks--;
        }
        if (this.forcedTarget != null && (this.age > ServerMobsMod.getConfig().fluteCrocodileSurvivalTicks || !this.forcedTarget.isAlive())) {
            this.forcedTarget = null;
            this.dissolve();
            return;
        }
        if (this.forcedTarget != null) {
            this.setTarget(this.forcedTarget);
            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED * ServerMobsMod.getConfig().fluteCrocodileSpeedMultiplier);
        }
    }

    protected void dissolve() {
        this.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 1.0f);
        if (this.world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 0, 0, 0, 0, 0);
        }
        this.discard();
    }

    @Override
    public boolean tryAttack(Entity target) {
        this.playSound(SoundEvents.ENTITY_EVOKER_FANGS_ATTACK, 1.0f, 0.75f);
        return super.tryAttack(target);
    }

    protected static class ForcedTargetGoal extends TrackTargetGoal {

        public ForcedTargetGoal(CrocodileEntity crocodileEntity) {
            super(crocodileEntity, false, false);
        }

        @Override
        public boolean canStart() {
            return ((CrocodileEntity) mob).forcedTarget != null;
        }

        @Override
        public void start() {
            this.mob.setTarget(((CrocodileEntity) mob).forcedTarget);
            this.target = this.mob.getTarget();
            super.start();
        }
    }

    protected class CrocodileSwimAroundGoal extends SwimAroundGoal {

        public CrocodileSwimAroundGoal(CrocodileEntity crocodile, double d, int i) {
            super(crocodile, d, i);
        }

        @Override
        public boolean canStart() {
            return super.canStart() && CrocodileEntity.this.isTouchingWater() && !CrocodileEntity.this.isTargetingOnLand();
        }

        @Override
        public boolean shouldContinue() {
            return super.shouldContinue() && CrocodileEntity.this.isTouchingWater() && !CrocodileEntity.this.isTargetingOnLand();
        }
    }

    protected class CrocodileWanderAroundGoal extends WanderAroundGoal {

        public CrocodileWanderAroundGoal(CrocodileEntity crocodile, double speed) {
            super(crocodile, speed);
        }

        @Override
        public boolean canStart() {
            return super.canStart() && (!CrocodileEntity.this.isTouchingWater() || CrocodileEntity.this.isTargetingOnLand());
        }

        @Override
        public boolean shouldContinue() {
            return super.shouldContinue() && (!CrocodileEntity.this.isTouchingWater() || CrocodileEntity.this.isTargetingOnLand());
        }
    }

    protected class ChompGoal extends Goal {
        protected int chompCooldown;
        protected int startTime;

        protected ChompGoal() {
        }

        @Override
        public boolean canStart() {
            if (CrocodileEntity.this.stunTicks > 0) {
                return false;
            }
            LivingEntity target = CrocodileEntity.this.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if (CrocodileEntity.this.isChomping()) {
                return false;
            }
            if (CrocodileEntity.this.squaredDistanceTo(target) < 5 * CrocodileEntity.this.squaredAttackRange(target)) {
                return false;
            }
            if (CrocodileEntity.this.getAttributeValue(CHOMP_CHANCE) < 0.27 || CrocodileEntity.this.getAttributeValue(CHOMP_CHANCE) > 0.83) {
                return false;
            }
            return CrocodileEntity.this.age > this.startTime && CrocodileEntity.this.getRandom().nextDouble()*7 < CrocodileEntity.this.getAttributeValue(CHOMP_CHANCE);
        }

        @Override
        public boolean shouldContinue() {
            LivingEntity target = CrocodileEntity.this.getTarget();
            return target != null && target.isAlive() && this.chompCooldown > 0;
        }

        @Override
        public void start() {
            this.chompCooldown = this.getTickCount(this.getInitialCooldown());
            CrocodileEntity.this.chompTicks = this.getChompTicks();
            this.startTime = CrocodileEntity.this.age + this.getStartTimeDelay();
            SoundEvent soundEvent = this.getSoundPrepare();
            if (soundEvent != null) {
                CrocodileEntity.this.playSound(soundEvent, 1.0f, 1.0f);
            }
        }

        protected int getInitialCooldown() {
            return 20;
        }

        protected int getChompTicks() {
            return 40;
        }

        protected int getStartTimeDelay() {
            return 100;
        }

        protected SoundEvent getSoundPrepare() {
            return SoundEvents.ENTITY_RAVAGER_ROAR;
        }

        @Override
        public void stop() {
            super.stop();
            CrocodileEntity.this.mouthAngleFactor = 0;
        }

        @Override
        public void tick() {
            --this.chompCooldown;
            double initialCooldown = this.getTickCount(this.getInitialCooldown());
            double progress = (initialCooldown - this.chompCooldown) / initialCooldown;
            double mouthAngleFactor;
            if (progress < 0.25) {
                mouthAngleFactor = progress * 4;
            } else if (progress >= 0.85) {
                mouthAngleFactor = (1 - progress) * (1 / 0.15);
            } else {
                mouthAngleFactor = 1.0;
            }
            mouthAngleFactor = Math.max(0, Math.min(mouthAngleFactor, 1));
            CrocodileEntity.this.mouthAngleFactor = mouthAngleFactor;
            if (this.chompCooldown == 0) {
                this.doChomp();
            }
        }

        protected void doChomp() {
            LivingEntity livingEntity = CrocodileEntity.this.getTarget();
            double d = Math.min(livingEntity.getY(), CrocodileEntity.this.getY());
            double e = Math.max(livingEntity.getY(), CrocodileEntity.this.getY()) + 1.0;
            float f = (float) MathHelper.atan2(livingEntity.getZ() - CrocodileEntity.this.getZ(), livingEntity.getX() - CrocodileEntity.this.getX());
            if (CrocodileEntity.this.squaredDistanceTo(livingEntity) < 9.0) {
                float g;
                int i;
                for (i = 0; i < 5; ++i) {
                    g = f + (float) i * (float) Math.PI * 0.4f;
                    this.conjureFangs(CrocodileEntity.this.getX() + (double) MathHelper.cos(g) * 1.5, CrocodileEntity.this.getZ() + (double) MathHelper.sin(g) * 1.5, d, e, g, 0);
                }
                for (i = 0; i < 8; ++i) {
                    g = f + (float) i * (float) Math.PI * 2.0f / 8.0f + 1.2566371f;
                    this.conjureFangs(CrocodileEntity.this.getX() + (double) MathHelper.cos(g) * 2.5, CrocodileEntity.this.getZ() + (double) MathHelper.sin(g) * 2.5, d, e, g, 3);
                }
            } else {
                for (int i = 0; i < 16; ++i) {
                    double h = 1.25 * (double) (i + 1);
                    this.conjureFangs(CrocodileEntity.this.getX() + (double) MathHelper.cos(f) * h, CrocodileEntity.this.getZ() + (double) MathHelper.sin(f) * h, d, e, f, i);
                }
            }
        }

        private void conjureFangs(double x, double z, double maxY, double y, float yaw, int warmup) {
            BlockPos blockPos = new BlockPos(x, y, z);
            boolean bl = false;
            double d = 0.0;
            do {
                VoxelShape voxelShape;
                BlockPos blockPos2;
                if (!CrocodileEntity.this.world.getBlockState(blockPos2 = blockPos.down()).isSideSolidFullSquare(CrocodileEntity.this.world, blockPos2, Direction.UP))
                    continue;
                if (!CrocodileEntity.this.world.isAir(blockPos) && !(voxelShape = CrocodileEntity.this.world.getBlockState(blockPos).getCollisionShape(CrocodileEntity.this.world, blockPos)).isEmpty()) {
                    d = voxelShape.getMax(Direction.Axis.Y);
                }
                bl = true;
                break;
            } while ((blockPos = blockPos.down()).getY() >= MathHelper.floor(maxY) - 1);
            if (bl) {
                CrocodileEntity.this.world.spawnEntity(new EvokerFangsEntity(CrocodileEntity.this.world, x, (double) blockPos.getY() + d, z, yaw, warmup, CrocodileEntity.this));
            }
        }
    }

    protected class LookAtTargetGoal extends Goal {
        public LookAtTargetGoal() {
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return CrocodileEntity.this.chompTicks > 0;
        }

        @Override
        public void start() {
            super.start();
            CrocodileEntity.this.navigation.stop();
        }

        @Override
        public void tick() {
            if (CrocodileEntity.this.getTarget() != null) {
                CrocodileEntity.this.getLookControl().lookAt(CrocodileEntity.this.getTarget(), CrocodileEntity.this.getMaxHeadRotation(), CrocodileEntity.this.getMaxLookPitchChange());
            }
        }
    }

    protected class CrocodileMeleeAttackGoal extends MeleeAttackGoal {

        public CrocodileMeleeAttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle) {
            super(mob, speed, pauseWhenMobIdle);
        }

        @Override
        public void tick() {
            super.tick();
            double progress = (this.getMaxCooldown() - this.getCooldown()) / (float) this.getMaxCooldown();
            double mouthAngleFactor;
            if (progress < 0.25) {
                mouthAngleFactor = progress * 4;
            } else if (progress >= 0.95) {
                mouthAngleFactor = (1 - progress) * (1 / 0.05);
            } else {
                mouthAngleFactor = 1.0;
            }
            mouthAngleFactor = Math.max(0, Math.min(mouthAngleFactor, 1));
            CrocodileEntity.this.mouthAngleFactor = mouthAngleFactor * 0.75;
        }

        @Override
        public void stop() {
            super.stop();
            CrocodileEntity.this.mouthAngleFactor = 0;
        }
    }

    protected class CrocodileSwimGoal extends SwimGoal {

        public CrocodileSwimGoal(MobEntity mob) {
            super(mob);
        }

        @Override
        public boolean canStart() {
            return super.canStart() && (CrocodileEntity.this.getAir() < 280 || CrocodileEntity.this.isTargetingOnLand());
        }

        @Override
        public boolean canStop() {
            return !super.canStart() && CrocodileEntity.this.getAir() >= CrocodileEntity.this.getMaxAir();
        }

        @Override
        public boolean shouldContinue() {
            return !this.canStop();
        }
    }

    protected class CrocodileMoveIntoWaterGoal extends Goal {

        private BlockPos targetPos;

        public CrocodileMoveIntoWaterGoal() {
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        private boolean crocValid() {
            return CrocodileEntity.this.isOnGround() && !CrocodileEntity.this.world.getFluidState(CrocodileEntity.this.getBlockPos()).isIn(FluidTags.WATER) && CrocodileEntity.this.getTarget() == null;
        }

        @Override
        public boolean canStart() {
            if (crocValid()) {
                targetPos = generateTarget();
                return targetPos != null;
            }
            return false;
        }

        @Override
        public void start() {
            if (targetPos != null) {
                CrocodileEntity.this.navigation.startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1D);
            }
        }

        @Override
        public void tick() {
            if (targetPos != null) {
                CrocodileEntity.this.navigation.startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1D);
            }
        }

        @Override
        public boolean shouldContinue() {
            return !CrocodileEntity.this.navigation.isIdle() && targetPos != null && crocValid();
        }

        @Nullable
        private BlockPos generateTarget() {
            BlockPos blockpos = null;
            final Random random = Random.create();
            final int range = 45;
            for (int i = 0; i < 15; i++) {
                BlockPos blockPos = CrocodileEntity.this.getBlockPos().add(random.nextInt(range) - range / 2, 3, random.nextInt(range) - range / 2);
                while (CrocodileEntity.this.world.isAir(blockPos) && blockPos.getY() > 1) {
                    blockPos = blockPos.down();
                }

                if (CrocodileEntity.this.world.getFluidState(blockPos).isIn(FluidTags.WATER)) {
                    blockpos = blockPos;
                }
            }
            return blockpos;
        }
    }

    protected static class CrocodileMobNavigation extends MobNavigation {

        public CrocodileMobNavigation(CrocodileEntity crocodile, World world) {
            super(crocodile, world);
        }

        @Override
        protected boolean isAtValidPosition() {
            return true;
        }

        @Override
        protected PathNodeNavigator createPathNodeNavigator(int range) {
            this.nodeMaker = new AmphibiousPathNodeMaker(true);
            this.nodeMaker.setCanSwim(true);
            return new PathNodeNavigator(this.nodeMaker, range);
        }

        @Override
        protected Vec3d getPos() {
            return new Vec3d(this.entity.getX(), this.entity.getBodyY(0.5), this.entity.getZ());
        }

        @Override
        protected double adjustTargetY(Vec3d pos) {
            return pos.y;
        }

        @Override
        public Path findPathTo(BlockPos target, int distance) {
            return this.findPathTo(ImmutableSet.of(target), 8, false, distance);
        }

        @Override
        public Path findPathTo(Entity entity, int distance) {
            return this.findPathTo(ImmutableSet.of(entity.getBlockPos()), 16, true, distance);
        }

        @Override
        public boolean isValidPosition(BlockPos pos) {
            if (this.entity.getTarget() == null) {
                return this.world.getBlockState(pos).isOf(Blocks.WATER);
            }
            return !this.world.getBlockState(pos.down()).isAir();
        }

        @Override
        public void setCanSwim(boolean canSwim) {
        }
    }

    protected static class CrocodileSwimNavigation extends SwimNavigation {

        CrocodileSwimNavigation(CrocodileEntity crocodile, World world) {
            super(crocodile, world);
        }

        @Override
        protected boolean isAtValidPosition() {
            return true;
        }

        @Override
        protected PathNodeNavigator createPathNodeNavigator(int range) {
            this.nodeMaker = new AmphibiousPathNodeMaker(true);
            return new PathNodeNavigator(this.nodeMaker, range);
        }

        @Override
        public boolean isValidPosition(BlockPos pos) {
            if (this.entity.getTarget() == null) {
                return this.world.getBlockState(pos).isOf(Blocks.WATER);
            }
            return !this.world.getBlockState(pos.down()).isAir();
        }
    }

    @Override
    public int getMaxAir() {
        return 400;
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return world.doesNotIntersectEntities(this);
    }

    @SuppressWarnings("deprecation")
    public static boolean canSpawn(EntityType<? extends CrocodileEntity> type, WorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        int minHeight = world.getSeaLevel() - 5;
        return world.getDifficulty() != Difficulty.PEACEFUL && pos.getY() >= minHeight && world.getBlockState(pos.down()).isIn(ModBlockTags.CROCODILES_SPAWNABLE_ON);
    }

    public static void registerSpawnRestrictions(EntityType<? extends CrocodileEntity> type) {
        SpawnRestriction.register(type, SpawnRestriction.Location.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, CrocodileEntity::canSpawn);
    }
}
