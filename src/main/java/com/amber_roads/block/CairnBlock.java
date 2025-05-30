package com.amber_roads.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static com.amber_roads.util.TravelersUtil.rotateShape;

public class CairnBlock extends Block {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    protected static final MapCodec<CairnBlock> CODEC = simpleCodec(CairnBlock::new);

    public static final VoxelShape SHAPE = Shapes.or(
            Block.box(5, 0, 8, 9, 1, 12),
            Block.box(7, 0, 4, 11, 1, 8),
            Block.box(3, 0, 5, 9, 1, 9),
            Block.box(8, 0, 7, 12, 1, 11),

            Block.box(4, 1, 4.25, 7, 2, 7.25),
            Block.box(8, 1, 7.75, 11, 2, 10.75),
            Block.box(4.5, 1, 8.25, 7.5, 2, 11.25),
            Block.box(7, 1, 4.75, 10, 2, 7.75),

            Block.box(7.5, 2, 7.25, 10, 3, 9.75),
            Block.box(5.5, 2, 5.25, 8, 3, 7.75),
            Block.box(5, 2, 7.75, 7.5, 3, 10.25),

            Block.box(6.5, 3, 6.75, 9, 4, 9.25),
            Block.box(6.1, 4, 7.15, 8.6, 5, 9.65),
            Block.box(6.6, 5, 7.65, 8.1, 5.75, 9.15)
            );


    public CairnBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

       @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getRotatedShape(state, level, pos);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getRotatedShape(state, level, pos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getRotatedShape(state, level, pos);
    }

    protected VoxelShape getRotatedShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(FACING) == Direction.SOUTH ? SHAPE : rotateShape(Direction.SOUTH, state.getValue(FACING), SHAPE);

    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed blockstate.
     */
    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed blockstate.
     */
    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
    }


    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos blockpos = pos.below();
        BlockState blockstate = level.getBlockState(blockpos);
        return blockstate.isFaceSturdy(level, blockpos, Direction.UP);
    }
}
