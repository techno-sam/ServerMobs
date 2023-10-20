package com.slimeist.server_mobs.pentagram;

import com.slimeist.server_mobs.ServerMobsMod;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;

@SuppressWarnings("SameParameterValue")
public final class StructureTester {
    private final Block bone = Blocks.BONE_BLOCK;
    private final Block soulFire = Blocks.SOUL_FIRE;
    private final Block pentagramCore = ServerMobsMod.PENTAGRAM_CORE_BLOCK;
    private final TagKey<Block> soulSand = BlockTags.SOUL_FIRE_BASE_BLOCKS;
    private final Block soulTorch = Blocks.SOUL_TORCH;
    private final Block soulLantern = Blocks.SOUL_LANTERN;

    private final ServerWorld world;
    private final BlockPos centerBottom;

    public StructureTester(ServerWorld world, BlockPos centerBottom) {
        this.world = world;
        this.centerBottom = centerBottom;
    }

    private boolean ok = true;

    public boolean test() {
        ok = ExpectedBlock.of(0, 0, 0, soulSand).test(world, centerBottom);
        ok &= ExpectedBlock.of(0, 1, 0, soulFire).test(world, centerBottom)
            || ExpectedBlock.of(0, 1, 0, pentagramCore).test(world, centerBottom);

        // Layer 0
        ct(1, 0, 0, Axis.X, bone);
        ct(2, 0, 0, Axis.Y, bone);
        ct(3, 0, 0, Axis.X, bone);
        ct(4, 0, 0, soulSand);

        ct(1, 0, 1, soulSand);
        ct(2, 0, 1, soulSand);
        ct(3, 0, 1, Axis.Z, bone);
        ct(4, 0, 1, soulSand);

        ct(1, 0, 2, soulSand);
        ct(2, 0, 2, Axis.Y, bone);
        ct(3, 0, 2, soulSand);
        ct(4, 0, 2, soulSand);

        ct(1, 0, 3, Axis.X, bone);
        ct(2, 0, 3, soulSand);
        ct(3, 0, 3, soulSand);

        ct(1, 0, 4, soulSand);
        ct(2, 0, 4, soulSand);

        // Layer 1
        ct(2, 1, 0, soulTorch);
        candle(4, 1, 0, 1, true);

        ct(4, 1, 1, soulFire);

        ct(2, 1, 2, soulLantern);
        candle(4, 1, 2, 1, true);

        candle(3, 1, 3, 4, true);

        ct(4, 1, 1, soulFire);
        candle(4, 1, 2, 1, true);

        return ok;
    }

    /*
        rotations:
                   |
            -y, x  |    x, y
                   |
                   |
        ----------------------- flip and invert
                   |
                   |   y, -x
                   |
                   |
                   |
         */
    private void ct(int xOffset, final int yOffset, int zOffset, TagKey<Block> tag) {
        if (!ok) return;

        for (int rotation = 0; rotation < 4; rotation++) {
            ok = ExpectedBlock.of(xOffset, yOffset, zOffset, tag).test(world, centerBottom);
            if (!ok) return;

            int xTemp = xOffset;
            xOffset = zOffset;
            zOffset = -xTemp;
        }
    }

    private void ct(int xOffset, final int yOffset, int zOffset, Block... blocks) {
        if (!ok) return;

        for (int rotation = 0; rotation < 4; rotation++) {
            ok = ExpectedBlock.of(xOffset, yOffset, zOffset, blocks).test(world, centerBottom);
            if (!ok) return;

            int xTemp = xOffset;
            xOffset = zOffset;
            zOffset = -xTemp;
        }
    }

    private void ct(int xOffset, final int yOffset, int zOffset, Axis axis, Block... blocks) {
        if (!ok) return;

        for (int rotation = 0; rotation < 4; rotation++) {
            ok = ExpectedBlock.of(xOffset, yOffset, zOffset, axis, blocks).test(world, centerBottom);
            if (!ok) return;

            int xTemp = xOffset;
            xOffset = zOffset;
            zOffset = -xTemp;
            axis = switch (axis) {
                case X -> Axis.Z;
                case Y -> Axis.Y;
                case Z -> Axis.X;
            };
        }
    }

    private void candle(int xOffset, final int yOffset, int zOffset, int count, boolean lit) {
        if (!ok) return;

        for (int rotation = 0; rotation < 4; rotation++) {
            ok = ExpectedBlock.candle(xOffset, yOffset, zOffset, count, lit).test(world, centerBottom);
            if (!ok) return;

            int xTemp = xOffset;
            xOffset = zOffset;
            zOffset = -xTemp;
        }
    }

    @Override
    public String toString() {
        return "StructureTester[" +
            "world=" + world + ", " +
            "centerBone=" + centerBottom + ']';
    }
}
