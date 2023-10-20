package com.slimeist.server_mobs.mixin;

import com.slimeist.server_mobs.ServerMobsMod;
import com.slimeist.server_mobs.pentagram.StructureTester;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoulFireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoulFireBlock.class)
public abstract class SoulFireBlockMixin extends AbstractFireBlock {
    private SoulFireBlockMixin(Settings settings, float damage) {
        super(settings, damage);
    }

    @Inject(method = "getStateForNeighborUpdate", at = @At("RETURN"), cancellable = true)
    private void maybeSpawnPentagram(BlockState state, Direction direction, BlockState neighborState, WorldAccess world,
                                     BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        if (!cir.getReturnValue().isAir()) {
            if (world instanceof ServerWorld serverWorld && new StructureTester(serverWorld, pos.down()).test()) {
                cir.setReturnValue(ServerMobsMod.PENTAGRAM_CORE_BLOCK.getDefaultState());
            }
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        if (world instanceof ServerWorld serverWorld && new StructureTester(serverWorld, pos.down()).test()) {
            serverWorld.setBlockState(pos, ServerMobsMod.PENTAGRAM_CORE_BLOCK.getDefaultState());
        }
    }
}
