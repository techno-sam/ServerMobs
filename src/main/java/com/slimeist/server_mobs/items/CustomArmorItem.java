package com.slimeist.server_mobs.items;

import eu.pb4.polymer.api.item.PolymerItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class CustomArmorItem extends ArmorItem implements PolymerItem, CustomModelItem {
    private int customModelData;
    private int customArmorColor;
    private final Item visualItem;
    private final boolean disabled;

    public CustomArmorItem(ArmorItem armorItem, ArmorMaterial material, Settings settings) {
        this(armorItem, armorItem, material, settings, false);
    }

    public CustomArmorItem(ArmorItem armorItem, ArmorMaterial material, Settings settings, boolean disabled) {
        this(armorItem, armorItem, material, settings, disabled);
    }

    public CustomArmorItem(ArmorItem armorItem, Item visualItem, ArmorMaterial material, Settings settings) {
        this(armorItem, visualItem, material, settings, false);
    }

    public CustomArmorItem(ArmorItem armorItem, Item visualItem, ArmorMaterial material, Settings settings, boolean disabled) {
        super(new DisabledArmorMaterial(material, disabled), armorItem.getSlotType(), settings);
        this.visualItem = visualItem;
        this.disabled = disabled;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (this.disabled) {
            tooltip.add(Text.translatable("tooltip.server_mobs.disabled"));
        }
    }

    @Override
    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
    }

    public void setCustomArmorColor(int customArmorColor) {
        this.customArmorColor = customArmorColor;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return visualItem;
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return customModelData;
    }

    @Override
    public int getPolymerArmorColor(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return customArmorColor;
    }

    @Override
    public EquipmentSlot getSlotType() {
        return disabled ? EquipmentSlot.OFFHAND : super.getSlotType();
    }

    @Override
    public ArmorMaterial getMaterial() {
        return super.getMaterial();
    }

    private static class DisabledArmorMaterial implements ArmorMaterial {

        private final ArmorMaterial parent;
        private final boolean disabled;

        private DisabledArmorMaterial(ArmorMaterial parent, boolean disabled) {
            this.parent = parent;
            this.disabled = disabled;
        }

        @Override
        public int getDurability(EquipmentSlot slot) {
            return parent.getDurability(slot);
        }

        @Override
        public int getProtectionAmount(EquipmentSlot slot) {
            return disabled ? 0 : parent.getProtectionAmount(slot);
        }

        @Override
        public int getEnchantability() {
            return disabled ? 0 : parent.getEnchantability();
        }

        @Override
        public SoundEvent getEquipSound() {
            return parent.getEquipSound();
        }

        @Override
        public Ingredient getRepairIngredient() {
            return disabled ? null : parent.getRepairIngredient();
        }

        @Override
        public String getName() {
            return parent.getName();
        }

        @Override
        public float getToughness() {
            return disabled ? 0 : parent.getToughness();
        }

        @Override
        public float getKnockbackResistance() {
            return disabled ? 0 : parent.getKnockbackResistance();
        }
    }
}
