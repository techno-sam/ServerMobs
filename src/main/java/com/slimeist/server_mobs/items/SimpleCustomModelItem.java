package com.slimeist.server_mobs.items;

import eu.pb4.polymer.api.item.SimplePolymerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class SimpleCustomModelItem extends SimplePolymerItem implements CustomModelItem {
    private int customModelData;

    public SimpleCustomModelItem(Settings settings, Item polymerItem) {
        this(settings, polymerItem, -1);
    }

    public SimpleCustomModelItem(Settings settings, Item polymerItem, int customModelData) {
        super(settings, polymerItem);
        this.customModelData = customModelData;
    }

    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.customModelData;
    }
}
