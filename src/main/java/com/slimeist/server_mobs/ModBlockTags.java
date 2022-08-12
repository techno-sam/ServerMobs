package com.slimeist.server_mobs;

import net.minecraft.block.Block;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

public class ModBlockTags {
    public static final TagKey<Block> CROCODILES_SPAWNABLE_ON = TagKey.of(Registry.BLOCK_KEY, ServerMobsMod.id("crocodiles_spawnable_on"));
    public static final TagKey<Block> CROCODILES_FEAR = TagKey.of(Registry.BLOCK_KEY, ServerMobsMod.id("crocodiles_fear"));
}
