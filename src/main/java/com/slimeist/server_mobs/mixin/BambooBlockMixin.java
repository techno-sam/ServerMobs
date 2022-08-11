package com.slimeist.server_mobs.mixin;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.client.PolymerClientUtils;
import eu.pb4.polymer.api.networking.PolymerPacketUtils;
import eu.pb4.polymer.api.networking.PolymerSyncUtils;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(BambooBlock.class)
public class BambooBlockMixin {
    @Inject(at = @At("RETURN"), method = "updateLeaves")
    private void updateLeavesPost(BlockState state, World world, BlockPos pos, Random random, int height, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld) {
            BlockState upState = world.getBlockState(pos.up());
            for (ServerPlayerEntity player : PlayerLookup.tracking(serverWorld, pos.up())) {
                BlockState clientState = PolymerBlockUtils.getPolymerBlockState(upState, player);
                player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, clientState));
            }
        }
    }
}
