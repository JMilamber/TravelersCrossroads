package com.amber_roads.block;

import com.amber_roads.TravelersCrossroads;
import com.amber_roads.entity.blockentity.CairnBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static com.amber_roads.util.TravelersUtil.rotateShape;

public class CairnBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty CREATED_PATHS = BooleanProperty.create("created_paths");
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

    private int tickCount = 0;
    protected boolean init;
    public ArrayList<ChunkPos> cairnPaths = new ArrayList<>();

    public CairnBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(CREATED_PATHS, Boolean.FALSE)
        );
        this.init = false;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        boolean flag = !level.isClientSide && player != null && !player.isCreative();
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(CREATED_PATHS, flag);
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
        builder.add(FACING, CREATED_PATHS);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CairnBlockEntity(pos, state);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        this.tickCount ++;

        if (level.isClientSide) {
            return;
        }

        if (!this.init && this.tickCount > 20) {
            TravelersCrossroads.WATCHER.addPoiToMap(
                    new ChunkPos(pos)
            );
            ((CairnBlockEntity) level.getBlockEntity(pos)).addConnections(random);

            this.init = true;
        }
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
