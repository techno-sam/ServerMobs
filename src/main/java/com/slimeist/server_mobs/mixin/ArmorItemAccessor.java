package com.slimeist.server_mobs.mixin;

import net.minecraft.item.ArmorItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(ArmorItem.class)
public interface ArmorItemAccessor {
    @Accessor("MODIFIERS")
    static UUID[] getMODIFIERS() {
        throw new AssertionError("Should have been mixed in");
    }
}
