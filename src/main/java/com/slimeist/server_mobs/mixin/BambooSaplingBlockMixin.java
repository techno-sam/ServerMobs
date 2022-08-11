package com.slimeist.server_mobs.mixin;

import com.slimeist.server_mobs.ServerMobsMod;
import net.minecraft.block.BambooSaplingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BambooSaplingBlock.class)
public class BambooSaplingBlockMixin {
    @Inject(method="getStateForNeighborUpdate", at=@At("RETURN"))
    public void inject_state(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        if (direction == Direction.UP && neighborState.isOf(ServerMobsMod.CROCODILE_FLUTE_BLOCK)) {
            world.setBlockState(pos, Blocks.BAMBOO.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
    }
}
