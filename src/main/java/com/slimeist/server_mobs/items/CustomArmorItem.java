package com.slimeist.server_mobs.items;

import eu.pb4.polymer.api.item.PolymerItem;
import net.minecraft.entity.Entity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CustomArmorItem extends ArmorItem implements PolymerItem, CustomModelItem {
    private int customModelData;
    private int customArmorColor;
    private final Item visualItem;

    public CustomArmorItem(ArmorItem armorItem, ArmorMaterial material, Settings settings) {
        this(armorItem, armorItem, material, settings);
    }

    public CustomArmorItem(ArmorItem armorItem, Item visualItem, ArmorMaterial material, Settings settings) {
        super(material, armorItem.getSlotType(), settings);
        this.visualItem = visualItem;
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
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
    }
}
