package com.slimeist.server_mobs.mixin;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BambooBlock.class)
public class BambooBlockMixin {
    @Inject(at = @At("RETURN"), method = "updateLeaves")
    private void updateLeavesPost(BlockState state, World world, BlockPos pos, Random random, int height, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld) {
            for (BlockPos blockPos : new BlockPos[]{pos, pos.up()}) {
                BlockState upState = world.getBlockState(blockPos);
                for (ServerPlayerEntity player : PlayerLookup.tracking(serverWorld, blockPos)) {
                    BlockState clientState = PolymerBlockUtils.getPolymerBlockState(upState, player);
                    player.networkHandler.sendPacket(new BlockUpdateS2CPacket(blockPos, clientState));
                }
            }
        }
    }
}
