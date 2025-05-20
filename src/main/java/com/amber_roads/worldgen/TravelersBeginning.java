package com.amber_roads.worldgen;

import com.amber_roads.TravelersCrossroads;
import com.amber_roads.block.CairnBlock;
import com.amber_roads.init.TravelersInit;
import com.amber_roads.util.TravelersDirection;
import com.amber_roads.util.TravelersTags;
import com.amber_roads.util.TravelersUtil;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.Structure;


import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.amber_roads.util.TravelersTags.Blocks.PATH_ABOVE;
import static com.amber_roads.util.TravelersTags.Blocks.PATH_BELOW;
import static com.amber_roads.util.TravelersTags.Structures.PATH_AVOID;


public class TravelersBeginning extends Feature<TravelersConfiguration> {

    protected static final List<Block> PATHBLOCKS = List.of(
            Blocks.DIRT_PATH, Blocks.DIRT_PATH, Blocks.DIRT_PATH, Blocks.DIRT_PATH,
            Blocks.GRAVEL, Blocks.GRAVEL, Blocks.GRAVEL,
            Blocks.COBBLESTONE, Blocks.COARSE_DIRT
    );

    private static final Map<TravelersDirection, List<Pair<Integer, Integer>>> CONNECTION_POS = Map.of(
            TravelersDirection.NORTH, List.of(Pair.of(-2, -3), Pair.of(-1, -3), Pair.of(0, -3), Pair.of(1, -3)),
            TravelersDirection.NORTHEAST, List.of(Pair.of(0, -3), Pair.of(1, -3), Pair.of(2, -3), Pair.of(3, -3), Pair.of(4, -3), Pair.of(2, -2), Pair.of(3, -2), Pair.of(2, -1)),
            TravelersDirection.EAST, List.of(Pair.of(2, 1), Pair.of(2, 0), Pair.of(2, -1), Pair.of(2, -2)),
            TravelersDirection.SOUTHEAST, List.of(Pair.of(2, 0), Pair.of(2, 1), Pair.of(3, 1), Pair.of(0, 2), Pair.of(1, 2), Pair.of(2, 2), Pair.of(3, 2), Pair.of(4, 2)),
            TravelersDirection.SOUTH, List.of(Pair.of(1, 2), Pair.of(0, 2), Pair.of(-1, 2), Pair.of(-2, 2)),
            TravelersDirection.SOUTHWEST, List.of(Pair.of(-1, 2), Pair.of(-2, 2), Pair.of(-3, 2), Pair.of(-4, 2), Pair.of(-5, 2), Pair.of(-3, 1), Pair.of(-4, 1), Pair.of(-3, 0)),
            TravelersDirection.WEST, List.of(Pair.of(-3, -2), Pair.of(-3, -1), Pair.of(-3, 0), Pair.of(-3, 1)),
            TravelersDirection.NORTHWEST, List.of(Pair.of(-3, -1), Pair.of(-3, -2), Pair.of(-4, -2), Pair.of(-1, -3), Pair.of(-2, -3), Pair.of(-3, -3), Pair.of(-4, -3), Pair.of(-5, -3))
    );

    public TravelersBeginning(Codec<TravelersConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext context) {
        return this.place((TravelersConfiguration) context.config(), context.level(), context.chunkGenerator(), context.random(), context.origin());
    }

    @Override
    public boolean place(TravelersConfiguration config, WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        // Convert origin blockpos into chunkpos center
        ChunkPos originChunk = new ChunkPos(origin);
        origin = originChunk.getMiddleBlockPosition(origin.getY());

        if (level.getBiome(origin).is(TravelersTags.Biomes.PATH_AVOID)) {
            return false;
        }
        TravelersCrossroads.LOGGER.info("Spawning Feature at: {}", origin);

        
        BlockPos cairnPos = origin.offset(random.nextInt(5) - 2, 0, random.nextInt(5) - 2);
        cairnPos = findY(level, cairnPos);
        level.setBlock(
                cairnPos.above(),
                TravelersInit.CAIRN.get().defaultBlockState().setValue(CairnBlock.FACING, Direction.from2DDataValue(random.nextInt(4))),
                3
        );
        // TravelersCrossroads.LOGGER.info("Cairn Placed at: {}", cairnPos.above());

        return true;
    }
    
    public static void placeCenter(ServerLevel level, RandomSource random, BlockPos origin) {
        BlockPos mutable$blockPos;
        // TravelersCrossroads.LOGGER.debug("In center place");

        for (int x = -2; x < 2; x++) {
            // TravelersCrossroads.LOGGER.debug("X: {} ", x);

            for (int z = - 2; z < 2; z++) {
                // TravelersCrossroads.LOGGER.debug("Z: {} ", z);
                mutable$blockPos = origin.offset(x, 0, z);
                // TravelersCrossroads.LOGGER.debug("Above: {} | At: {}| Y {}", level.getBlockState(mutable$blockPos.above()), level.getBlockState(mutable$blockPos), mutable$blockPos.getY());
                mutable$blockPos = findY(level, mutable$blockPos);
                level.setBlock(mutable$blockPos, PATHBLOCKS.get(random.nextInt(PATHBLOCKS.size())).defaultBlockState(), 2);
            }
        }
    }

    public static void placeRoadPiece(ServerLevel level, RandomSource random, BlockPos origin, @Nullable TravelersDirection previous, @Nullable TravelersDirection next) {
        placeCenter(level, random, origin);
        if (previous != null) {
            placeCenterConnection(level, random, origin, previous);
        }
        if (next != null) {
            placeCenterConnection(level, random, origin, next);
        }
    }

    public static void placeCenterConnection(ServerLevel level, RandomSource random, BlockPos origin, TravelersDirection direction) {
        BlockPos mutable$blockPos;
        for (Pair<Integer, Integer> pair : CONNECTION_POS.get(direction)) {
            mutable$blockPos = origin.offset(pair.getFirst(), 0 , pair.getSecond());
            mutable$blockPos = findY(level, mutable$blockPos);
            level.setBlock(mutable$blockPos, PATHBLOCKS.get(random.nextInt(PATHBLOCKS.size())).defaultBlockState(), 2);
        }
        switch (direction) {
            case NORTH -> placeVerticalConnection(level, random, origin.offset(0, 0, direction.getZ() * 5));
            case NORTHEAST -> placeDiagonalLR(level, random, origin.offset(direction.getX() * 5, 0, direction.getZ() * 5));
            case EAST -> placeHorizontalConnection(level, random, origin.offset(direction.getX() * 6, 0, 0));
            case SOUTHEAST -> placeDiagonalRL(level, random, origin.offset(direction.getX() * 6, 0, direction.getZ() * 6));
            case SOUTH -> placeVerticalConnection(level, random, origin.offset(0, 0, direction.getZ() * 5));
            case SOUTHWEST -> placeDiagonalLR(level, random, origin.offset(direction.getX() * 6, 0, direction.getZ() * 6));
            case WEST -> placeHorizontalConnection(level, random, origin.offset(direction.getX() * 5, 0, 0));
            case NORTHWEST -> placeDiagonalRL(level, random, origin.offset(direction.getX() * 5, 0, direction.getZ() * 5));
        }
    }

    public static void placeVerticalConnection(ServerLevel level, RandomSource random, BlockPos origin) {
        BlockPos mutable$blockPos;
        for (int x = -2; x < 2; x++) {
            for (int z = -3; z < 3; z++) {
                mutable$blockPos = origin.offset(x, 0, z);
                mutable$blockPos = findY(level, mutable$blockPos);
                level.setBlock(mutable$blockPos, PATHBLOCKS.get(random.nextInt(PATHBLOCKS.size())).defaultBlockState(), 2);
            }
        }
    }

    public static void placeHorizontalConnection(ServerLevel level, RandomSource random, BlockPos origin) {
        BlockPos mutable$blockPos;
        for (int x = -3; x < 2; x++) {
            for (int z = -2; z < 2; z++) {
                mutable$blockPos = origin.offset(x, 0, z);
                mutable$blockPos = findY(level, mutable$blockPos);
                level.setBlock(mutable$blockPos, PATHBLOCKS.get(random.nextInt(PATHBLOCKS.size())).defaultBlockState(), 2);
            }
        }
    }

    public static void placeDiagonalLR(ServerLevel level, RandomSource random, BlockPos origin) {
        int xStart = 0;
        BlockPos mutable$blockPos;
        for (int z = -3; z < 2 ; z++) {
            for (int x = xStart; x < xStart + 5; x++) {
                mutable$blockPos = origin.offset(x, 0, z);
                mutable$blockPos = findY(level, mutable$blockPos);
                level.setBlock(mutable$blockPos, PATHBLOCKS.get(random.nextInt(PATHBLOCKS.size())).defaultBlockState(), 2);
            }
            xStart --;
        }
    }

    public static void placeDiagonalRL(ServerLevel level, RandomSource random, BlockPos origin) {
        int xStart = -5;
        BlockPos mutable$blockPos;
        for (int z = -3; z < 2 ; z++) {
            for (int x = xStart; x < xStart + 5; x++) {
                mutable$blockPos = origin.offset(x, 0, z);
                mutable$blockPos = findY(level, mutable$blockPos);
                level.setBlock(mutable$blockPos, PATHBLOCKS.get(random.nextInt(PATHBLOCKS.size())).defaultBlockState(), 2);
            }
            xStart ++;
        }
    }

    public static BlockPos findY(ServerLevel level, BlockPos origin) {
        while (!level.getBlockState(origin.above()).is(PATH_ABOVE) || !level.getBlockState(origin).is(PATH_BELOW)) {
            //TravelersCrossroads.LOGGER.info(
            //        "Blockstates: {} {} {} {}",
            //        level.getBlockState(origin.above()), level.getBlockState(origin.above()).is(PATH_ABOVE),
            //        level.getBlockState(origin), level.getBlockState(origin).is(PATH_BELOW)
            //);
            if (level.getBlockState(origin).is(PATH_ABOVE)) {
                origin = origin.below();
            } else if (level.getBlockState(origin.above()).is(PATH_BELOW)) {
                origin = origin.above();
            } else {
                break;
            }
        }
        return origin;
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


