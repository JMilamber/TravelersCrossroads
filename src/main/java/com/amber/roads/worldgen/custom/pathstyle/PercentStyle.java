package com.amber.roads.worldgen.custom.pathstyle;

import com.amber.roads.init.TravelersInit;
import com.amber.roads.util.TravelersDirection;
import com.amber.roads.world.PathNode;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class PercentStyle extends PathStyle {

    public static final MapCodec<PercentStyle> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(settingsCodec(instance),
                    ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("texture_blocks").forGetter(style -> style.textureBlocks),
                    Codec.intRange(0, 100).fieldOf("main_block_chance").forGetter(style -> style.minBlockChance),
                    Codec.intRange(0, 100).fieldOf("texture_block_chance").forGetter(style -> style.textureBlockChance),
                    Codec.intRange(0, 100).fieldOf("skip_block_chance").forGetter(style -> style.skipBlockChance)
            ).apply(instance, PercentStyle::new)
    );

    protected final List<BlockState> textureBlocks;
    protected final int minBlockChance;
    protected final int textureBlockChance;
    protected final int skipBlockChance;
    protected final int totalChance;

    public PercentStyle(PathSettings settings,  List<BlockState> textureBlocks, int mainBlockChance, int textureBlockChance, int skipBlockChance) {
        super(settings);
        this.textureBlocks = textureBlocks;
        this.minBlockChance = mainBlockChance;
        this.textureBlockChance = textureBlockChance;
        this.skipBlockChance = skipBlockChance;
        this.totalChance = mainBlockChance + textureBlockChance + skipBlockChance;
    }


    @Override
    public void setPathBlock(ServerLevel level, BlockPos originPos) {
        RandomSource randomSource = level.getRandom();

        int blockValue = randomSource.nextInt(totalChance);
        BlockState setState = null;

        if (blockValue <= minBlockChance) {
            setState = settings.mainPathBlock().getState(randomSource, originPos);
        } else if (blockValue - minBlockChance <= textureBlockChance) {
            setState = textureBlocks.get(randomSource.nextInt(textureBlocks.size()));
        }

        if (setState != null) {
            level.setBlock(originPos, setState.rotate(level, originPos, Rotation.getRandom(randomSource)), 2);
        }
    }

    @Override
    public void placeExtraBlocks(ServerLevel level, PathNode pos1, TravelersDirection direction, List<BlockPos> extraBlockPositions) {

    }

    @Override
    MapCodec<? extends PathStyle> codec() {
        return this.codec();
    }

    @Override
    public StyleModifierType<?> type() {
        return TravelersInit.PERCENT_STYLE.get();
    }
}
