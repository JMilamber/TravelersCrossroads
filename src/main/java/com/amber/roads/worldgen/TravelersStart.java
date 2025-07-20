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

import java.util.Optional;

import static com.amber.roads.util.TravelersTags.Biomes.PATH_START_AVOID_BIOME;
import static com.amber.roads.util.TravelersTags.Blocks.PATH_ABOVE;
import static com.amber.roads.util.TravelersTags.Blocks.PATH_BELOW;

public class TravelersStart extends Feature<NoneFeatureConfiguration> {

    public TravelersStart(Codec<NoneFeatureConfiguration> codec) {
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

        for (int x = originChunk.x - 1; x <= originChunk.x + 1; x++) {
            for (int z = originChunk.z - 1; z <= originChunk.z + 1; z++) {
                if (level.getBiome(new ChunkPos(x, z).getMiddleBlockPosition(origin.getY())).is(PATH_START_AVOID_BIOME)) {
                    TravelersCrossroads.LOGGER.debug("Bad Biome at: {}", origin);
                    return false;
                }
            }
        }

        TravelersCrossroads.LOGGER.debug("Spawning Feature at: {}", origin);
        Optional<BlockPos> cairnPos;
        int tries = 0;
        do {
            tries++;
            cairnPos = Optional.of(origin.offset(random.nextInt(4) - 2, 0, random.nextInt(4) - 2));
            cairnPos = findY(level, cairnPos.get());
        } while (cairnPos.isEmpty() && tries < 16);

        cairnPos.ifPresent(blockPos -> {
            level.setBlock(blockPos.above(), TravelersInit.CAIRN.get().defaultBlockState().setValue(
                    CairnBlock.FACING, Direction.from2DDataValue(random.nextInt(4))
            ), 3);
            TravelersCrossroads.LOGGER.debug("Cairn Placed at: {}", blockPos.above());
            TravelersCrossroads.WATCHER.addCrossroadToCreate(blockPos);
        });

        if (cairnPos.isEmpty()) {
            TravelersCrossroads.LOGGER.debug("Removed Pos {}", originChunk);
            TravelersCrossroads.WATCHER.removeDistanceFilterNode(originChunk);
            return false;
        }

        return true;
    }

    public static Optional<BlockPos> findY(WorldGenLevel level, BlockPos origin) {

        while (!level.getBlockState(origin.above()).is(PATH_ABOVE) || !level.getBlockState(origin).is(PATH_BELOW)) {
            if (level.getBlockState(origin.above()).getFluidState().isSource() || level.getBlockState(origin).getFluidState().isSource()){
                return Optional.empty();
            } else if (level.getBlockState(origin).is(PATH_ABOVE)) {
                origin = origin.below();
            } else if (level.getBlockState(origin.above()).is(PATH_BELOW)) {
                origin = origin.above();
            } else {
                break;
            }
        }
        return Optional.of(origin);
    }
}
