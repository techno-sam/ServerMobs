package com.slimeist.server_mobs.blocks;

import com.slimeist.server_mobs.ServerMobsMod;
import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.x.BlockMapper;
import eu.pb4.polymer.ext.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.ext.blocks.impl.BlockExtBlockMapper;
import net.minecraft.block.*;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.SwordItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class CrocodileFluteBlock extends Block implements PolymerTexturedBlock, PolymerBlock {

    private static boolean registeredBambooStates = false;

    protected static final VoxelShape OUTLINE_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
    protected static final VoxelShape COLLISION_SHAPE = Block.createCuboidShape(6.5, 0.0, 6.5, 9.5, 16.0, 9.5);

    private BlockState modelState;

    public CrocodileFluteBlock(Settings settings) {
        super(settings);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.BAMBOO;
    }

    @SuppressWarnings("UnstableApiUsage")
    public void registerModel() {
        if (!registeredBambooStates) {
            BlockMapper.DEFAULT_MAPPER_EVENT.register((player, mapper) -> BlockExtBlockMapper.INSTANCE);
            for (BlockState state : Blocks.BAMBOO.getStateManager().getStates()) {
                BlockExtBlockMapper.INSTANCE.stateMap.put(state, state.with(BambooBlock.STAGE, 0));
            }
            registeredBambooStates = true;
        }

        modelState = Blocks.BAMBOO.getDefaultState()
                .with(BambooBlock.AGE, 0)
                .with(BambooBlock.LEAVES, BambooLeaves.NONE)
                .with(BambooBlock.STAGE, 1);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return modelState;
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Vec3d vec3d = state.getModelOffset(world, pos);
        return COLLISION_SHAPE.offset(vec3d.x, vec3d.y, vec3d.z);
    }

    @Override
    public boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        if (!fluidState.isEmpty()) {
            return null;
        }
        BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos().down());
        if (blockState.isIn(BlockTags.BAMBOO_PLANTABLE_ON)) {
            return ServerMobsMod.CROCODILE_FLUTE_BLOCK.getDefaultState();
        }
        return null;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos.down()).isIn(BlockTags.BAMBOO_PLANTABLE_ON);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!state.canPlaceAt(world, pos)) {
            world.createAndScheduleBlockTick(pos, this, 1);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        if (player.getMainHandStack().getItem() instanceof SwordItem) {
            return 1.0f;
        }
        return super.calcBlockBreakingDelta(state, player, world, pos);
    }
}
