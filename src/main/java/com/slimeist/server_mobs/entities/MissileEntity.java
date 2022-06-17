package com.slimeist.server_mobs.entities;

import com.google.common.base.MoreObjects;
import com.slimeist.server_mobs.ServerMobsMod;
import com.slimeist.server_mobs.server_rendering.entity.IServerRenderedEntity;
import com.slimeist.server_mobs.server_rendering.model.BakedServerEntityModel;
import eu.pb4.polymer.api.entity.PolymerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public class MissileEntity extends ProjectileEntity implements PolymerEntity, IServerRenderedEntity {
    private static Supplier<BakedServerEntityModel> bakedModelSupplier;
    private BakedServerEntityModel.Instance modelInstance;

    @Nullable
    private Entity target;
    @Nullable
    private UUID targetUuid;

    private double targetX;
    private double targetY;
    private double targetZ;
    //Constructor
    public MissileEntity(EntityType<? extends MissileEntity> entityType, World world) {
        super(entityType, world);
        this.noClip = true;
    }

    protected MissileEntity(World world, LivingEntity owner, Entity target) {
        this(ServerMobsMod.MISSILE, world);
        this.setOwner(owner);
        this.target = target;
    }

    public static MissileEntity targeting(World world, LivingEntity owner, Entity target) {
        return new MissileEntity(world, owner, target);
    }

    //IServerRenderedEntity

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
        this.getModelInstance().setPartRotation("base", new EulerAngle(this.getPitch(), this.getYaw()+180, 0));
        this.getModelInstance().setPartPivot("base", Vec3d.ZERO);
    }

    public static void setBakedModelSupplier(Supplier<BakedServerEntityModel> bakedModel) {
        bakedModelSupplier = bakedModel;
    }

    //PolymerEntity

    @Override
    public EntityType<?> getPolymerEntityType() {
        return EntityType.MARKER;
    }

    @Override
    protected void initDataTracker() {

    }

    //MissileEntity


    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.target != null) {
            nbt.putUuid("Target", this.target.getUuid());
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.containsUuid("Target")) {
            this.targetUuid = nbt.getUuid("Target");
        }
    }

    private Vec3d normalize(Vec3d in) {
        Vec3d tmp = new Vec3d(in.getX(), in.getY(), in.getZ());
        double biggest = Math.max(Math.abs(tmp.getX()), Math.max(Math.abs(tmp.getY()), Math.abs(tmp.getZ())));
        tmp.multiply(1/biggest);
        return tmp;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3d vec3d;
        if (!this.world.isClient) {
            if (this.target == null && this.targetUuid != null) {
                this.target = ((ServerWorld)this.world).getEntity(this.targetUuid);
                if (this.target == null) {
                    this.targetUuid = null;
                }
            }
            if (this.target != null && this.target.isAlive() && this.target instanceof LivingEntity && !this.target.isSpectator()) {
                Vec3d center = this.target.getBoundingBox().getCenter();
                this.targetX = center.getX() - this.getX();
                this.targetY = center.getY() - this.getY();
                this.targetZ = center.getZ() - this.getZ();

                /*this.targetX = MathHelper.clamp(this.targetX * 1.025, -1.0, 1.0);
                this.targetY = MathHelper.clamp(this.targetY * 1.025, -1.0, 1.0);
                this.targetZ = MathHelper.clamp(this.targetZ * 1.025, -1.0, 1.0);*/

                Vec3d target = new Vec3d(this.targetX, this.targetY, this.targetZ);
                target.multiply(1.025);
                target = normalize(target);

                vec3d = this.getVelocity();
                double scale = 0.1;
                this.setVelocity(new Vec3d(target.getX()*scale, target.getY()*scale, target.getZ()*scale));//vec3d.add((this.targetX - vec3d.x) * 0.2, (this.targetY - vec3d.y) * 0.2, (this.targetZ - vec3d.z) * 0.2));
            } else if (!this.hasNoGravity()) {
                this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
            }
            HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
            if (hitResult.getType() != HitResult.Type.MISS) {
                this.onCollision(hitResult);
            }
            this.checkBlockCollision();
            vec3d = this.getVelocity();
            this.setPosition(this.getX() + vec3d.x, this.getY() + vec3d.y, this.getZ() + vec3d.z);
            ProjectileUtil.setRotationFromVelocity(this, 0.5f);
            if (!this.world.isClient && (this.target == null || this.target.isRemoved())) {
                this.goBoom();
                this.discard();
            }
        }
        this.getModelInstance().updateHologram();
    }

    @Override
    protected boolean canHit(Entity entity) {
        return super.canHit(entity) && !entity.noClip;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < 16384.0;
    }

    @Override
    public float getBrightnessAtEyes() {
        return 1.0f;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (entityHitResult.getEntity() != this.getOwner()) {
            goBoom();
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        //goBoom();
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        this.discard();
    }

    private void goBoom() {
        DamageSource source = null;
        if (this.getOwner() instanceof LivingEntity) {
            source = DamageSource.explosion((LivingEntity) this.getOwner());
        }
        this.world.createExplosion(this, source, null, this.getX(), this.getY(), this.getZ(), 1.5f, false, Explosion.DestructionType.NONE);
    }

    @Override
    public boolean collides() {
        return true;
    }
}
