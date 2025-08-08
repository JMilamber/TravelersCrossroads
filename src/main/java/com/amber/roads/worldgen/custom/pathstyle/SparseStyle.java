package com.amber.roads.worldgen.custom.pathstyle;

import com.amber.roads.init.TravelersInit;
import com.amber.roads.util.TravelersDirection;
import com.amber.roads.world.PathNode;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class SparseStyle extends PercentStyle {

    public static final MapCodec<SparseStyle> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    settingsCodec(instance),
                    ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("texture_blocks").forGetter(style -> style.textureBlocks)
            ).apply(instance, SparseStyle::new)
    );

    public SparseStyle(PathSettings settings, List<BlockState> textureBlocks) {
        super(settings, textureBlocks, 50, 20, 30);
    }

    @Override
    public void placeExtraBlocks(ServerLevel level, PathNode pos1, TravelersDirection direction, List<BlockPos> extraBlockPositions) {

    }

    @Override
    public MapCodec<? extends PathStyle> codec() {
        return this.codec();
    }

    @Override
    public StyleModifierType<?> type() {
        return TravelersInit.SPARSE_STYLE.get();
    }
}
