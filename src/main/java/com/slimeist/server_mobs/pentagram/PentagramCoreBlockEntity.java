package com.slimeist.server_mobs.pentagram;

import com.slimeist.server_mobs.ServerMobsMod;
import com.slimeist.server_mobs.mixin.EntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PentagramCoreBlockEntity extends BlockEntity {
    // entity : possession start
    private final List<LivingEntity> possessedEntities = new ArrayList<>();

    @Nullable
    private PentagramParticles particles;
    private int lazyTickCounter = 100;
    private long ticks = 0;

    public PentagramCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ServerMobsMod.PENTAGRAM_CORE_BE, pos, state);
    }

    public boolean startPossessing(LivingEntity entity) {
        if (entity instanceof PlayerEntity player && player.getAbilities().invulnerable) return false;
        if (!possessedEntities.contains(entity)) {
            possessedEntities.add(entity);
            entity.removeStatusEffect(StatusEffects.LEVITATION);
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 280, 255));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 280, 255));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 200, 0));

            entity.extinguish();
            ((EntityAccessor) entity).setHasVisualFire(true);
            entity.setOnFire(false); // just to update fire flag
        }
        return true;
    }

    public static void tick(World world, BlockPos pos, BlockState state, PentagramCoreBlockEntity be) {
        be.tick();
    }

    private @Nullable ItemEntity dropItem(ItemConvertible item) {
        return dropItem(item.asItem().getDefaultStack());
    }

    private @Nullable ItemEntity dropItem(ItemStack stack) {
        if (this.world == null) return null;
        int torchPosition = this.world.random.nextInt(4);
        int torchX = (torchPosition % 2 == 0) ? (torchPosition - 1) * 2 : 0; // -2, 0, 2
        int torchZ = (torchPosition % 2 == 1) ? (torchPosition - 2) * 2 : 0; // -2, 0, 2
        ItemEntity itemEntity = new ItemEntity(this.world, this.pos.getX() + 0.5 + torchX,
            this.pos.getY() + 1.5, this.pos.getZ() + 0.5 + torchZ, stack);
        itemEntity.setToDefaultPickupDelay();
        this.world.spawnEntity(itemEntity);
        return itemEntity;
    }

    private void tick() {
        ticks++;
        if (lazyTickCounter-- <= 0) {
            lazyTickCounter = 100;
            lazyTick();
        }
        if (particles == null && world instanceof ServerWorld serverWorld) {
            particles = new PentagramParticles(serverWorld, this.pos);
        }

        if (particles != null) {
            particles.spawn();
        }

        Iterator<LivingEntity> iter = possessedEntities.iterator();
        while (iter.hasNext()) {
            LivingEntity possessedEntity = iter.next();
            if (!possessedEntity.isAlive()) {
                iter.remove();
                continue;
            }
            possessedEntity.setPosition(this.pos.getX()+0.5, possessedEntity.getY(), this.pos.getZ()+0.5);

            if (!possessedEntity.hasStatusEffect(StatusEffects.LEVITATION)) { // time ran out
                iter.remove();
                possessedEntity.damage(DamageSource.MAGIC, Float.MAX_VALUE);

                LightningEntity lightningEntity = EntityType.LIGHTNING_BOLT.create(world);
                lightningEntity.refreshPositionAfterTeleport(possessedEntity.getPos());
                lightningEntity.setCosmetic(true);
                world.spawnEntity(lightningEntity);
                world.playSound(null, possessedEntity.getX(), possessedEntity.getY(), possessedEntity.getZ(),
                    SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 5.0F, 1.0F);

                ItemStack headStack = MobHeadsUtils.getHead(possessedEntity);
                if (!headStack.isEmpty())
                    dropItem(headStack);
            }
        }
    }

    private void lazyTick() {
        if (this.world instanceof ServerWorld serverWorld) {
            //noinspection deprecation
            this.getCachedState().getBlock().randomTick(this.getCachedState(), serverWorld, this.getPos(), this.world.random);
        }
    }
}
