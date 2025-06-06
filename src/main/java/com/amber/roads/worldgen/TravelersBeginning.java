package com.amber.roads.worldgen;

import com.amber.roads.TravelersCrossroads;
import com.amber.roads.block.CairnBlock;
import com.amber.roads.init.TravelersInit;
import com.mojang.serialization.Codec;

import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import static com.amber.roads.util.TravelersTags.Biomes.PATH_AVOID;
import static com.amber.roads.util.TravelersTags.Blocks.PATH_ABOVE;
import static com.amber.roads.util.TravelersTags.Blocks.PATH_BELOW;

public class TravelersBeginning extends Feature<NoneFeatureConfiguration> {

    public TravelersBeginning(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext context) {
        return this.place(context.level(), context.random(), context.origin());
    }

    public boolean place(WorldGenLevel level, RandomSource random, BlockPos origin) {
        // Convert origin blockpos into chunkpos center
        ChunkPos originChunk = new ChunkPos(origin);
        origin = originChunk.getMiddleBlockPosition(origin.getY());

        if (level.getBiome(origin).is(PATH_AVOID)) {
            return false;
        }
        TravelersCrossroads.LOGGER.debug("Spawning Feature at: {}", origin);
        BlockPos cairnPos;
        int tries = 0;
        do {
            tries++;
            cairnPos = origin.offset(random.nextInt(5) - 2, 0, random.nextInt(5) - 2);
            cairnPos = findY(level, cairnPos);
        } while (level.getBlockState(cairnPos).getFluidState().isSource() && tries < 25);

        level.setBlock(
                cairnPos.above(),
                TravelersInit.CAIRN.get().defaultBlockState().setValue(CairnBlock.FACING, Direction.from2DDataValue(random.nextInt(4))),
                3
        );
        TravelersCrossroads.LOGGER.debug("Cairn Placed at: {}", cairnPos.above());


        TravelersCrossroads.WATCHER.addCrossroadToCreate(origin);
        return true;
    }

    public static BlockPos findY(WorldGenLevel level, BlockPos origin) {
        while (!level.getBlockState(origin.above()).is(PATH_ABOVE) || !level.getBlockState(origin).is(PATH_BELOW)) {
            if (level.getBlockState(origin).is(PATH_ABOVE)) {
                origin = origin.below();
            } else if (level.getBlockState(origin.above()).is(PATH_BELOW)){
                origin = origin.above();
            } else {
                break;
            }
        }
        return origin;
    }
}
