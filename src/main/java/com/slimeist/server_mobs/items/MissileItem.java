package com.slimeist.server_mobs.items;

import com.slimeist.server_mobs.ServerMobsMod;
import com.slimeist.server_mobs.util.CommandUtils;
import com.slimeist.server_mobs.entities.MissileEntity;
import eu.pb4.polymer.api.item.SimplePolymerItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.FireworkStarItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MissileItem extends SimplePolymerItem implements CustomModelItem {
    private int customModelData;
    public MissileItem(Settings settings, Item polymerItem) {
        this(settings, polymerItem, -1);
    }

    public MissileItem(Settings settings, Item polymerItem, int customModelData) {
        super(settings, polymerItem);
        this.customModelData = customModelData;
    }

    private HitResult playerCast(Entity source) {
        double distance = 700;
        Vec3d start = source.getCameraPosVec(0);
        Vec3d rot = source.getRotationVec(0);
        Box box = source.getBoundingBox().stretch(rot.multiply(distance)).expand(1.0, 1.0, 1.0);
        boolean targetMissiles = false;
        EntityHitResult entityResult = ProjectileUtil.raycast(source, start, start.add(rot.x * distance, rot.y * distance, rot.z * distance), box, e -> !e.isSpectator() && e.collides() && (!e.getType().equals(ServerMobsMod.MISSILE) || targetMissiles), distance*distance);
        if (entityResult != null && entityResult.getEntity() != null && !entityResult.getEntity().isSpectator() && entityResult.getEntity().isAlive()) {
            return entityResult;
        } else {
            return null;
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (selected) {
            if (entity instanceof PlayerEntity player) {
                HitResult hitResult = playerCast(entity);
                if (hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() != null) {
                    Text dispName = entityHitResult.getEntity().getDisplayName();
                    if (dispName != null) {
                        player.sendMessage(new TranslatableText("server_mobs.missile.target_locked_named", dispName), true);
                    } else {
                        player.sendMessage(new TranslatableText("tootip.server_mobs.missile.target_locked"), true);
                    }
                    if (entityHitResult.getEntity() instanceof LivingEntity living) {
                        living.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 10, 0, false, false, false));
                    }
                } else {
                    player.sendMessage(new TranslatableText("server_mobs.missile.no_target"), true);
                }
            }
        }
    }

    //Missile launch code inside the following method is from Pneumaticraft
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        HitResult hitResult = playerCast(user);
        if (hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() != null) {
            MissileEntity missile = MissileEntity.targeting(world, user, entityHitResult.getEntity());
            Vec3d newPos = missile.getPos().add(user.getRotationVector().normalize());
            missile.setPos(newPos.x, newPos.y, newPos.z);
            missile.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1/3f, 0.0F);

            if (stack.hasNbt()) {
                NbtCompound explosionData = stack.getSubNbt("Explosion");
                if (explosionData != null) {
                    int[] colors = explosionData.getIntArray("Colors");
                    if (colors.length>0) {
                        int[] fadeColors = explosionData.getIntArray("FadeColors");
                        missile.setColorData(colors, fadeColors);
                        missile.setLaunchStack(stack.copy());
                    }
                }
            }

            if (false) {
                ServerMobsMod.LOGGER.info("Missile fired at entity "+entityHitResult.getEntity().getName().getString());
                String launchText = "Launched by " + user.getName().asString() + ", in direction:\n\tPitch: " + user.getPitch() + "\n\tYaw: " + user.getYaw();
                ServerMobsMod.LOGGER.info(launchText);
            }
            //user.sendMessage(new LiteralText(launchText.replace("\t", "    ")).setStyle(Style.EMPTY.withColor(Formatting.GOLD)), false);

            world.spawnEntity(missile);

            stack.decrement(1);
            return TypedActionResult.success(stack);
        } else {
            user.sendMessage(new TranslatableText("server_mobs.missile.no_target"), true);
            return TypedActionResult.fail(stack);
        }
    }

    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.customModelData;
    }

    //FireworkStarItem tooltip
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbtCompound = stack.getSubNbt("Explosion");
        if (nbtCompound != null) {
            FireworkStarItem.appendFireworkTooltip(nbtCompound, tooltip);
        }
    }
}
