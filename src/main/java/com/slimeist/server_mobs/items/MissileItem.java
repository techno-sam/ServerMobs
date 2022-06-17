package com.slimeist.server_mobs.items;

import com.slimeist.server_mobs.util.CommandUtils;
import com.slimeist.server_mobs.entities.MissileEntity;
import eu.pb4.polymer.api.item.SimplePolymerItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MissileItem extends SimplePolymerItem {
    private int customModelData;
    public MissileItem(Settings settings, Item polymerItem) {
        this(settings, polymerItem, -1);
    }

    public MissileItem(Settings settings, Item polymerItem, int customModelData) {
        super(settings, polymerItem);
        this.customModelData = customModelData;
    }

    private HitResult playerCast(Entity entity) {
        double distance = 700;
        Vec3d start = entity.getCameraPosVec(0);
        Vec3d rot = entity.getRotationVec(0);
        Box box = entity.getBoundingBox().stretch(rot.multiply(distance)).expand(1.0, 1.0, 1.0);
        return ProjectileUtil.raycast(entity, start, start.add(rot.x * distance, rot.y * distance, rot.z * distance), box, e -> !e.isSpectator() && e.collides(), distance);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (selected) {
            if (entity instanceof PlayerEntity player) {
                HitResult hitResult = playerCast(entity);
                if (hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() != null) {
                    player.sendMessage(new TranslatableText("server_mobs.missile.target_locked"), true);
                } else {
                    player.sendMessage(new TranslatableText("server_mobs.missile.no_target"), true);
                }
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        HitResult hitResult = playerCast(user);
        if (hitResult instanceof EntityHitResult entityHitResult) {
            MissileEntity missile = MissileEntity.targeting(world, user, entityHitResult.getEntity());
            missile.setPos(user.getX(), user.getY()+user.getEyeHeight(user.getPose()), user.getZ());
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
}
