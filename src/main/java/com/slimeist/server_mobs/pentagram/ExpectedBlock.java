package com.slimeist.server_mobs.pentagram;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.CandleBlock;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;

public record ExpectedBlock(int xOffset, int yOffset, int zOffset, BlockPredicate predicate) {
    public static ExpectedBlock of(int xOffset, int yOffset, int zOffset, TagKey<Block> tag) {
        return new ExpectedBlock(xOffset, yOffset, zOffset, new BlockPredicate(tag, null, StatePredicate.ANY, NbtPredicate.ANY));
    }

    public static ExpectedBlock of(int xOffset, int yOffset, int zOffset, Block... blocks) {
        return new ExpectedBlock(xOffset, yOffset, zOffset, new BlockPredicate(null, ImmutableSet.copyOf(blocks), StatePredicate.ANY, NbtPredicate.ANY));
    }

    public static ExpectedBlock of(int xOffset, int yOffset, int zOffset, Axis axis, Block... blocks) {
        return new ExpectedBlock(xOffset, yOffset, zOffset, new BlockPredicate(null, ImmutableSet.copyOf(blocks),
            StatePredicate.Builder.create().exactMatch(Properties.AXIS, axis).build(), NbtPredicate.ANY));
    }

    public static ExpectedBlock candle(int xOffset, int yOffset, int zOffset, int count, boolean lit) {
        return new ExpectedBlock(xOffset, yOffset, zOffset, new BlockPredicate(BlockTags.CANDLES, null,
            StatePredicate.Builder.create().exactMatch(CandleBlock.CANDLES, count).exactMatch(CandleBlock.LIT, lit).build(),
            NbtPredicate.ANY));
    }

    public boolean test(ServerWorld world, BlockPos basePos) {
        return predicate.test(world, basePos.add(xOffset, yOffset, zOffset));
    }
}
