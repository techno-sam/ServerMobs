package com.slimeist.server_mobs.items;

import eu.pb4.polymer.api.item.PolymerSpawnEggItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class CustomPolymerSpawnEggItem extends PolymerSpawnEggItem implements CustomModelItem {
    private int customModelData;

    public CustomPolymerSpawnEggItem(EntityType<? extends MobEntity> type, Item visualItem, Settings settings) {
        super(type, visualItem, settings);
    }

    @Override
    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return customModelData;
    }
}
