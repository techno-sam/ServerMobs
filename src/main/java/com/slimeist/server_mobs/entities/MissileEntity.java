package com.slimeist.server_mobs.entities;

import com.slimeist.server_mobs.ServerMobsMod;
import com.slimeist.server_mobs.mixin.FireworkRocketEntityAccessor;
import com.slimeist.server_mobs.server_rendering.entity.IServerRenderedEntity;
import com.slimeist.server_mobs.server_rendering.model.BakedServerEntityModel;
import com.slimeist.server_mobs.util.VectorUtil;
import eu.pb4.polymer.api.entity.PolymerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

//Sort of a port of Pneumaticraft's Micromissile
public class MissileEntity extends ThrownEntity implements PolymerEntity, IServerRenderedEntity {

    private static Supplier<BakedServerEntityModel> bakedModelSupplier;
    private BakedServerEntityModel.Instance modelInstance;

    @Nullable
    private Entity target;
    @Nullable
    private UUID targetUuid;

    private double targetX;
    private double targetY;
    private double targetZ;

    //Values from Pneumaticraft
    private float maxVelocitySq = 0.5f;
    private float accel = 1.05f; // straight line acceleration
    private float turnSpeed = 0.1f;
    private float explosionPower = 2f; //TODO config
    private boolean outOfFuel = false;
    //End values from Pneumaticraft

    private MissileColor colorData = MissileColor.EMPTY;
    private ItemStack launchStack = ItemStack.EMPTY;

    //Constructor
    public MissileEntity(EntityType<? extends MissileEntity> entityType, World world) {
        super(entityType, world);
    }

    protected MissileEntity(EntityType<? extends MissileEntity> entityType, LivingEntity owner, World world) {
        super(entityType, owner, world);
    }

    public static MissileEntity targeting(World world, LivingEntity owner, Entity target) {
        MissileEntity missile = new MissileEntity(ServerMobsMod.MISSILE, owner, world);
        missile.target = target;
        return missile;
    }

    public void setColorData(int[] colors, int[] fadeColors) {
        this.setColorData(new MissileColor(colors, fadeColors));
    }

    public void setColorData(MissileColor colorData) {
        this.colorData = colorData;
    }

    public void setLaunchStack(ItemStack launchStack) {
        this.launchStack = launchStack;
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
    public void updateAngles() {
        this.getModelInstance().setPartPivot("base", Vec3d.ZERO);
        this.getModelInstance().setPartRotation("base", new EulerAngle(-this.getPitch(), -this.getYaw(), 0));

        if (!this.isAlive()) {
            this.getModelInstance().setDamageFlash(true);
        }
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
                this.target = ((ServerWorld) this.world).getEntity(this.targetUuid);
                if (this.target == null) {
                    this.targetUuid = null;
                }
            }

            if (age > 300) {
                this.outOfFuel = true;
            }

            //Following block from Pneumaticraft
            if (!outOfFuel) {
                // negate default slowdown of projectiles applied in superclass
                if (this.isTouchingWater()) {
                    setVelocity(getVelocity().multiply(1.25));
                } else {
                    setVelocity(getVelocity().multiply(1 / 0.99));
                }

                if (target != null && target.isAlive() && !target.isSpectator()) {
                    // turn toward the target
                    Vec3d diff = target.getPos().add(0, target.getStandingEyeHeight(), 0).subtract(getPos()).normalize().multiply(turnSpeed);
                    setVelocity(getVelocity().add(diff));
                } else {
                    this.outOfFuel = true;
                }

                // accelerate up to max velocity but cap there
                double velSq = getVelocity().lengthSquared();//motionX * motionX + motionY * motionY + motionZ * motionZ;
                double mul = velSq > maxVelocitySq ? maxVelocitySq / velSq : accel;
                setVelocity(getVelocity().multiply(mul));
                if (getEntityWorld() instanceof ServerWorld serverWorld && !colorData.equals(MissileColor.EMPTY)) {
                    Vec3d m = getVelocity();
                    Vec3f fromCol = VectorUtil.fireworkColor(colorData.colors()[random.nextInt(colorData.colors().length)]);
                    Vec3f toCol = fromCol;
                    if (colorData.fadeColors().length>0) {
                        toCol = VectorUtil.fireworkColor(colorData.fadeColors()[random.nextInt(colorData.fadeColors().length)]);
                    }
                    serverWorld.spawnParticles(new DustColorTransitionParticleEffect(fromCol, toCol, 4.0f), getX(), getY(), getZ(), 0, -m.x / 2, -m.y / 2, -m.z / 2, 1);
                } else if (getEntityWorld() instanceof ServerWorld serverWorld && getEntityWorld().random.nextBoolean()) {
                    Vec3d m = getVelocity();
                    serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(), 0, -m.x / 2, -m.y / 2, -m.z / 2, 1);
                }
            }//End Pneumaticraft block

            if (this.target != null) {
                if (this.target.getPos().isInRange(this.getPos(), 0.75)) {
                    this.onCollision(new EntityHitResult(this.target));
                }
            }
        }
        this.getModelInstance().updateHologram();
    }



    @Override
    protected boolean canHit(Entity entity) {
        return super.canHit(entity) && !entity.noClip && !entity.getType().equals(this.getType());
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

    //Following method copied from Pneumaticraft
    @Override
    protected void onCollision(HitResult hitResult) {
        if (age > 5 && isAlive()) {
            goBoom(hitResult instanceof EntityHitResult ? ((EntityHitResult) hitResult).getEntity() : null);
        }
    }

    //Folowing method inspired by Pneumaticraft
    private void goBoom(Entity e) {
        discard();
        DamageSource source = null;
        if (this.getOwner() instanceof LivingEntity) {
            source = DamageSource.explosion((LivingEntity) this.getOwner());
        }
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        //Following if block is from Pneumaticraft
        if (e != null) {
            x = MathHelper.lerp(0.25f, e.getX(), getX());
            y = MathHelper.lerp(0.25f, e.getY(), getY());
            z = MathHelper.lerp(0.25f, e.getZ(), getZ());
        }
        this.world.createExplosion(this, source, null, x, y, z, this.explosionPower, false, Explosion.DestructionType.NONE);
        if (!this.launchStack.equals(ItemStack.EMPTY)) {
            if (this.launchStack.hasNbt()) {
                NbtCompound explosionData = this.launchStack.getSubNbt("Explosion");
                if (explosionData != null) {
                    ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET, 1);
                    NbtCompound fireworksData = new NbtCompound();
                    NbtList explosionList = new NbtList();
                    explosionList.add(explosionData);
                    fireworksData.put("Explosions", explosionList);
                    fireworksData.putInt("Flight", 1);
                    fireworkStack.setSubNbt("Fireworks", fireworksData);
                    FireworkRocketEntity rocket = new FireworkRocketEntity(this.world, x, y, z, fireworkStack);
                    this.world.spawnEntity(rocket);
                    rocket.explodeAndRemove();
                    //((FireworkRocketEntityAccessor) rocket).setLife(((FireworkRocketEntityAccessor) rocket).getLifeTime()-20);
                }
            }
        }
    }

    @Override
    public boolean collides() {
        return true;
    }

    //Pneumaticraft MicroMissile code for rest of class
    @Override
    public void setVelocity(Entity entityThrower, float pitch, float yaw, float pitchOffset, float velocity, float inaccuracy) {
        float x = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        float y = -MathHelper.sin(pitch * 0.017453292F);
        float z = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        this.setVelocity(x, y, z, velocity, 0f);
        this.setVelocity(this.getVelocity().add(entityThrower.getVelocity().x, 0, entityThrower.getVelocity().z));
    }

    @Override
    public void setVelocity(double x, double y, double z, float velocity, float inaccuracy) {
        double f = Math.sqrt(x * x + y * y + z * z);
        x = x / f * velocity;
        y = y / f * velocity;
        z = z / f * velocity;
        this.setVelocity(x, y, z);

        float f1 = MathHelper.sqrt((float) (x * x + z * z));
        this.setYaw((float)(MathHelper.atan2(x, z) * (180D / Math.PI)));
        this.setPitch((float)(MathHelper.atan2(y, f1) * (180D / Math.PI)));
        this.prevYaw = this.getYaw();
        this.prevPitch = this.getPitch();
    }

    @Override
    protected float getGravity() {
        return outOfFuel ? super.getGravity() : 0F;
    }

    @Override
    public boolean hasNoGravity() {
        return !outOfFuel;
    }
}
