package com.slimeist.server_mobs.items;

import com.slimeist.server_mobs.ServerMobsMod;
import com.slimeist.server_mobs.entities.CrocodileEntity;
import eu.pb4.polymer.api.item.PolymerItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class CrocodileFluteItem extends BlockItem implements PolymerItem, CustomModelItem {
    public final Item visualItem;
    private int customModelData;

    public CrocodileFluteItem(Block block, Settings settings, Item visualItem) {
        super(block, settings);
        this.visualItem = visualItem;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return visualItem;
    }

    @Override
    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return customModelData;
    }

    public static void setTarget(ItemStack stack, @Nullable LivingEntity entity) {
        if (entity == null) {
            stack.removeSubNbt("CrocodileTarget");
        } else {
            NbtCompound nbt = stack.getOrCreateSubNbt("CrocodileTarget");
            nbt.putUuid("uuid", entity.getUuid());
            nbt.putLong("expiration", entity.getWorld().getTime() + ServerMobsMod.getConfig().fluteTargetExpirationTicks);
        }
    }

    public static long getExpiration(ItemStack stack) {
        NbtCompound nbt = stack.getSubNbt("CrocodileTarget");
        if (nbt != null) {
            if (nbt.contains("expiration", NbtElement.LONG_TYPE)) {
                return nbt.getLong("expiration");
            }
        }
        return 0;
    }

    public static boolean isExpired(ItemStack stack, World world) {
        return world.getTime() > getExpiration(stack);
    }

    @Nullable
    public static UUID getTargetUuid(ItemStack stack, ServerWorld world) {
        NbtCompound nbt = stack.getSubNbt("CrocodileTarget");
        if (nbt != null) {
            if (isExpired(stack, world)) {
                setTarget(stack, null);
                return null;
            }
            if (nbt.containsUuid("uuid")) {
                return nbt.getUuid("uuid");
            }
        }
        return null;
    }

    @Nullable
    public static LivingEntity getTarget(ItemStack stack, World world) {
        if (world instanceof ServerWorld serverWorld)
            return getTarget(stack, serverWorld);
        return null;
    }

    @Nullable
    public static LivingEntity getTarget(ItemStack stack, ServerWorld world) {
        UUID uuid = getTargetUuid(stack, world);
        Entity entity = world.getEntity(uuid);
        if (entity instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    public static boolean hasTarget(ItemStack stack, World world) {
        return getTarget(stack, world) != null;
    }

    @Override
    protected boolean canPlace(ItemPlacementContext context, BlockState state) {
        return getTarget(context.getStack(), context.getWorld())==null && super.canPlace(context, state);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        setTarget(stack, target);
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world instanceof ServerWorld serverWorld) {
            LivingEntity target = getTarget(stack, serverWorld);
            if (target != null) {
                if (target.getWorld().getDimension() == world.getDimension()) {
                    CrocodileEntity crocodileEntity = new CrocodileEntity(world, target);
                    crocodileEntity.refreshPositionAndAngles(user.getBlockPos(), user.getHeadYaw(), 0);
                    world.spawnEntity(crocodileEntity);
                    setTarget(stack, null);

                    return TypedActionResult.success(stack);
                }
            }
        }
        return TypedActionResult.fail(stack);
    }

    static String str(int seconds) {
        return (seconds < 10 ? "0" : "") + seconds;
    }

    static TranslatableText expirationText(ItemStack stack, World world) {
        int ticks = (int) (getExpiration(stack) - world.getTime());
        int raw_seconds = ticks/20;

        if (raw_seconds <= 60)
            return new TranslatableText("tooltip.server_mobs.crocodile_flute.expiration_time.seconds", str(raw_seconds));

        int minutes = raw_seconds / 60;
        int seconds = raw_seconds % 60;

        return new TranslatableText("tooltip.server_mobs.crocodile_flute.expiration_time.minutes_seconds", minutes, str(seconds));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (world != null) {
            if (hasTarget(stack, world)) {
                tooltip.add(expirationText(stack, world));
            } else {
                setTarget(stack, null);
            }
            if (context.isAdvanced() && world instanceof ServerWorld serverWorld) {
                UUID uuid = getTargetUuid(stack, serverWorld);
                if (uuid != null) {
                    tooltip.add(new TranslatableText("tooltip.server_mobs.crocodile_flute.target_uuid", uuid.toString()));
                }
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (world instanceof ServerWorld) {
            if (!isExpired(stack, world)) {
                NbtCompound nbt = stack.getOrCreateSubNbt("CrocodileTarget");
                int ste = (int) (getExpiration(stack)-world.getTime()) / 20;
                if (!nbt.contains("ste", NbtElement.INT_TYPE) || nbt.getInt("ste") != ste) {
                    nbt.putInt("ste", ste);
                }
            }
        }
    }
}
