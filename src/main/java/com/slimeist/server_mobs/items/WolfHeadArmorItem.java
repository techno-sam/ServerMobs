package com.slimeist.server_mobs.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.slimeist.server_mobs.mixin.ArmorItemAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WolfHeadArmorItem extends CustomArmorItem {
    private int angryData = -1;
    private final Multimap<EntityAttribute, EntityAttributeModifier> angryAttributeModifiers;

    public WolfHeadArmorItem(ArmorItem armorItem, Item visualItem, ArmorMaterial material, Settings settings) {
        super(armorItem, visualItem, material, settings);

        Multimap<EntityAttribute, EntityAttributeModifier> superModifiers = super.getAttributeModifiers(super.getSlotType());
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(superModifiers);

        UUID uUID = ArmorItemAccessor.getMODIFIERS()[slot.getEntitySlotId()];
        builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(uUID, "Wolf Head attack damage", 2.0D, EntityAttributeModifier.Operation.ADDITION));

        angryAttributeModifiers = builder.build();
    }

    public void setAngryData(int angryData) {
        this.angryData = angryData;
    }

    private void setAngry(ItemStack stack, boolean angry) {
        if (angry) {
            stack.getOrCreateNbt().putBoolean("Angry", true);
        } else {
            stack.removeSubNbt("Angry");
        }
    }

    private boolean isAngry(ItemStack stack) {
        return stack.getOrCreateNbt().getBoolean("Angry");
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (entity instanceof ServerPlayerEntity serverPlayer) {
            boolean shouldBeAngry = serverPlayer.getAttacker() != null || (serverPlayer.getAttacking() != null && serverPlayer.getLastAttackTime()-serverPlayer.age < 200);
            boolean isAngry = isAngry(stack);
            if (shouldBeAngry != isAngry) {
                setAngry(stack, shouldBeAngry);

                if (slot == getSlotType().getEntitySlotId()) {
                    serverPlayer.world.playSound(
                        null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        shouldBeAngry ? SoundEvents.ENTITY_WOLF_HOWL : SoundEvents.ENTITY_WOLF_WHINE,
                        SoundCategory.PLAYERS, 1.0f, 1.0f
                    );
                }
            }
        }
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        if (isAngry(itemStack)) {
            return angryData;
        }
        return super.getPolymerCustomModelData(itemStack, player);
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        if (isAngry(stack) && slot == getSlotType())
            return angryAttributeModifiers;
        return super.getAttributeModifiers(stack, slot);
    }
}
