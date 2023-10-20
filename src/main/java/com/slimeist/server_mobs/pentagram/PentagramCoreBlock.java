package com.slimeist.server_mobs.pentagram;

import com.slimeist.server_mobs.ServerMobsMod;
import eu.pb4.polymer.api.block.SimplePolymerBlock;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.block.SoulFireBlock.isSoulBase;

public class PentagramCoreBlock extends SimplePolymerBlock implements BlockEntityProvider {
    protected static final VoxelShape BASE_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

    public PentagramCoreBlock(Settings settings) {
        super(settings, Blocks.SOUL_FIRE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return BASE_SHAPE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        boolean skipFire = false;
        if (entity instanceof LivingEntity livingEntity) {
            if (world.getBlockEntity(pos) instanceof PentagramCoreBlockEntity be) {
                skipFire = be.startPossessing(livingEntity);
            }
        }

        if (!skipFire) {
            if (!entity.isFireImmune()) {
                entity.setFireTicks(entity.getFireTicks() + 1);
                if (entity.getFireTicks() == 0) {
                    entity.setOnFireFor(8);
                }
            }

            entity.damage(DamageSource.IN_FIRE, 2);
        }
        super.onEntityCollision(state, world, pos, entity);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient()) {
            world.syncWorldEvent(null, WorldEvents.FIRE_EXTINGUISHED, pos, 0);
        }

        super.onBreak(world, pos, state, player);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!this.canPlaceAt(state, world, pos))
            world.setBlockState(pos, Blocks.SOUL_FIRE.getDefaultState());
        super.randomTick(state, world, pos, random);
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return this.canPlaceAt(state, world, pos)
            ? this.getDefaultState()
            : (isSoulBase(world.getBlockState(pos.down())) ? Blocks.SOUL_FIRE : Blocks.AIR).getDefaultState();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (world instanceof ServerWorld serverWorld) {
            return new StructureTester(serverWorld, pos.down()).test();
        }
        return isSoulBase(world.getBlockState(pos.down()));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PentagramCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ServerMobsMod.PENTAGRAM_CORE_BE, PentagramCoreBlockEntity::tick);
    }

    @SuppressWarnings("SameParameterValue")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> checkType(BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        //noinspection unchecked
        return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
    }
}
