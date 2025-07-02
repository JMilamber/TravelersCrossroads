package com.amber.roads.worldgen.custom.pathstyle;

import com.amber.roads.init.TravelersInit;
import com.amber.roads.util.TravelersDirection;
import com.amber.roads.world.PathPos;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.List;

public class SparseStyle extends PathStyle {

    public static final MapCodec<SparseStyle> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    settingsCodec(instance),
                    BlockStateProvider.CODEC.fieldOf("sub_path_block").forGetter(sparseStyle -> sparseStyle.subPathBlock)
            ).apply(instance, SparseStyle::new)
    );

    private final BlockStateProvider subPathBlock;

    public SparseStyle(PathSettings settings, BlockStateProvider subPathBlock) {
        super(settings);
        this.subPathBlock = subPathBlock;
    }

    @Override
    public void setPathBlock(ServerLevel level, BlockPos originPos) {
        RandomSource randomSource = level.getRandom();

        int blockValue = randomSource.nextInt(100);
        BlockState setState = null;

        if (blockValue <= 40) {
            setState = settings.mainPathBlock().getState(randomSource, originPos);
        } else if (blockValue <= 70) {
            setState = subPathBlock.getState(randomSource, originPos);
        }

        if (setState != null) {
            level.setBlock(originPos, setState, 2);
        }
    }

    @Override
    public void placeExtraBlocks(ServerLevel level, PathPos pos1, TravelersDirection direction, List<Pair<Integer, Integer>> extraBlockPositions) {}

    @Override
    MapCodec<? extends PathStyle> codec() {
        return null;
    }

    @Override
    public StyleModifierType<?> type() {
        return TravelersInit.SPARSE_STYLE.get();
    }
}
