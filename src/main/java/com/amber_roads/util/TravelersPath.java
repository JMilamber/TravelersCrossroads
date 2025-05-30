package com.amber_roads.util;

import com.amber_roads.TravelersCrossroads;
import com.amber_roads.worldgen.custom.StyleModifier;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.amber_roads.util.TravelersTags.Blocks.PATH_ABOVE;
import static com.amber_roads.util.TravelersTags.Blocks.PATH_BELOW;
import static com.amber_roads.util.TravelersUtil.chunkMatch;
import static java.lang.Math.abs;

public class TravelersPath {
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

    private final StyleModifier pathStyle;
    
    private final ChunkPos start;
    private final ChunkPos end;
    private boolean completed;
    private ArrayList<ChunkPos> path;
    private int pathIndex;
    private TravelersDirection initialDirection;

    public TravelersPath(
            ChunkPos start, ChunkPos end, RandomSource randomSource, TravelersDirection initialDirection,
            StyleModifier pathStyle
    ) {
        this.start = start;
        this.end = end;
        this.completed = false;
        this.path = new ArrayList<>();
        this.pathIndex = 0;
        this.initialDirection = initialDirection;
        this.pathStyle = pathStyle;
        this.createPath(randomSource);
        TravelersCrossroads.LOGGER.debug("New path start: {} end {} || Path {}", start, end, path);
    }

    public TravelersPath(CompoundTag tag, int index) {

        CompoundTag data = tag.getCompound("path" + index);
        this.completed = data.getBoolean("complete");
        this.pathIndex = data.getInt("index");
        CompoundTag startData = data.getCompound("start");
        this.start = new ChunkPos(startData.getInt("x"), startData.getInt("z"));
        CompoundTag endData = data.getCompound("end");
        this.end = new ChunkPos(endData.getInt("x"), endData.getInt("z"));
        this.path = new ArrayList<>();
        CompoundTag pathData = data.getCompound("path");
        for (int i = 0; i < data.getInt("length"); i++) {
            CompoundTag chunkData = pathData.getCompound(String.valueOf(i));
            this.path.add(new ChunkPos(chunkData.getInt("x"), chunkData.getInt("z")));
        }
        this.pathStyle = StyleModifier.DIRECT_CODEC.parse(NbtOps.INSTANCE ,data.get("style")).getOrThrow();
    }

    public CompoundTag save(CompoundTag tag, int index) {
        CompoundTag data = new CompoundTag();
        CompoundTag startData = new CompoundTag();
        startData.putInt("x", this.start.x);
        startData.putInt("z", this.start.z);
        CompoundTag endData = new CompoundTag();
        endData.putInt("x", this.end.x);
        endData.putInt("z", this.end.z);
        CompoundTag pathData = new CompoundTag();

        for (int i = 0; i < this.path.size(); i++) {
            ChunkPos pos = this.path.get(i);
            CompoundTag tag1 = new CompoundTag();
            tag1.putInt("x", pos.x);
            tag1.putInt("z", pos.z);
            pathData.put(String.valueOf(i), tag1);
            i++;
        }

        data.putBoolean("complete", this.completed);
        data.put("start", startData);
        data.put("end", endData);
        data.put("path", pathData);
        data.putInt("length", this.path.size());
        data.putInt("index", this.pathIndex);
        data.put("style", StyleModifier.DIRECT_CODEC.encodeStart(NbtOps.INSTANCE, this.pathStyle).getOrThrow());
        tag.put("path" + index, data);

        return tag;
    }

    public ArrayList<ChunkPos> getPath() {
        return path;
    }

    public ChunkPos getEnd() {
        return end;
    }

    public TravelersDirection getInitialDirection() {
        return initialDirection;
    }

    public void createPath(RandomSource randomSource) {
        ChunkPos pathPos = new ChunkPos(this.start.x + this.initialDirection.getX(), this.start.z + this.initialDirection.getZ());
        TravelersDirection direction;
        TravelersDirection randDirection;
        int pathState = 0;
        this.path.add(this.start);
        this.path.add(pathPos);

        while (!chunkMatch(pathPos, this.end)) {
            if (abs(pathPos.x - this.end.x) <= 1 && abs(pathPos.z - this.end.z) <= 1) {
                pathPos = end;
            } else {
                direction = TravelersDirection.directionFromChunks(pathPos, this.end);
                switch (pathState) {
                    case 1 -> {
                        randDirection = TravelersDirection.getRandomForDirection(randomSource, direction);
                        pathPos = new ChunkPos(pathPos.x + randDirection.getX(), pathPos.z + randDirection.getZ());
                    }
                    case 2, 3, 4 -> {
                        randDirection = TravelersDirection.getRandomNarrowForDirection(randomSource, direction);
                        pathPos = new ChunkPos(pathPos.x + randDirection.getX(), pathPos.z + randDirection.getZ());
                    }
                    case 6 -> pathState = 0;
                    default -> pathPos = new ChunkPos(pathPos.x + direction.getX(), pathPos.z + direction.getZ());
                }
                pathState++;
            }
            this.path.add(pathPos);
        }
    }

    public void placeNextChunk(ServerLevel level) {
        ChunkPos placeChunk = this.path.get(this.pathIndex);

        if (!level.hasChunk(placeChunk.x, placeChunk.z)) {
            return;
        }
        // TravelersCrossroads.LOGGER.debug("Placing Chunk: {}" , placeChunk);
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        RandomSource randomSource = level.getRandom();
        int generationY = generator.getFirstOccupiedHeight(
                placeChunk.getMiddleBlockX(), placeChunk.getMiddleBlockZ(), Heightmap.Types.WORLD_SURFACE,
                level, level.getChunkSource().randomState());
        BlockPos center = placeChunk.getMiddleBlockPosition(generationY);


        TravelersDirection previousChunk;
        try {
            previousChunk = TravelersDirection.directionFromChunks(placeChunk, this.path.get(this.pathIndex - 1));
        } catch (Exception ignored) {
            previousChunk = null;
        }
        TravelersDirection nextChunk;
        try {
             nextChunk = TravelersDirection.directionFromChunks(placeChunk, this.path.get(this.pathIndex + 1));
        } catch (Exception ignored) {
            nextChunk = null;
        }

        placeRoadPiece(level, randomSource, center, previousChunk, nextChunk);

        if (this.pathIndex == this.path.size() - 1) {
            this.completed = true;
        }

        this.pathIndex++;
    }
    
    public void placeRoadPiece(ServerLevel level, RandomSource random, BlockPos origin, @Nullable TravelersDirection previous, @Nullable TravelersDirection next) {
        if (!level.getBiome(origin).is(TravelersTags.Biomes.PATH_AVOID)) {
            placeCenter(level, random, origin);
        }
        if (previous != null ) {
            placeCenterConnection(level, random, origin, previous);
        }
        if (next != null) {
            placeCenterConnection(level, random, origin, next);
        }
    }

    public void placeCenter(ServerLevel level, RandomSource random, BlockPos origin) {
        BlockPos mutable$blockPos;
        // TravelersCrossroads.LOGGER.debug("In center place");

        for (int x = -2; x < 2; x++) {
            // TravelersCrossroads.LOGGER.debug("X: {} ", x);

            for (int z = - 2; z < 2; z++) {
                // TravelersCrossroads.LOGGER.debug("Z: {} ", z);
                mutable$blockPos = origin.offset(x, 0, z);
                // TravelersCrossroads.LOGGER.debug("Above: {} | At: {}| Y {}", level.getBlockState(mutable$blockPos.above()), level.getBlockState(mutable$blockPos), mutable$blockPos.getY());
                mutable$blockPos = findY(level, mutable$blockPos);
                this.setBlock(level, mutable$blockPos, random);
            }
        }
    }

    public void placeCenterConnection(ServerLevel level, RandomSource random, BlockPos origin, TravelersDirection direction) {
        BlockPos mutable$blockPos;
        for (Pair<Integer, Integer> pair : CONNECTION_POS.get(direction)) {
            mutable$blockPos = origin.offset(pair.getFirst(), 0 , pair.getSecond());
            mutable$blockPos = findY(level, mutable$blockPos);
            this.setBlock(level, mutable$blockPos, random);
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

    public void placeVerticalConnection(ServerLevel level, RandomSource random, BlockPos origin) {
        if (level.getBiome(origin).is(TravelersTags.Biomes.PATH_AVOID)) {
            return;
        }
        BlockPos mutable$blockPos;
        for (int x = -2; x < 2; x++) {
            for (int z = -3; z < 3; z++) {
                mutable$blockPos = origin.offset(x, 0, z);
                mutable$blockPos = findY(level, mutable$blockPos);
                this.setBlock(level, mutable$blockPos, random);
            }
        }
    }

    public void placeHorizontalConnection(ServerLevel level, RandomSource random, BlockPos origin) {
        if (level.getBiome(origin).is(TravelersTags.Biomes.PATH_AVOID)) {
            return;
        }
        BlockPos mutable$blockPos;
        for (int x = -3; x < 2; x++) {
            for (int z = -2; z < 2; z++) {
                mutable$blockPos = origin.offset(x, 0, z);
                mutable$blockPos = findY(level, mutable$blockPos);
                this.setBlock(level, mutable$blockPos, random);
            }
        }
    }

    public void placeDiagonalLR(ServerLevel level, RandomSource random, BlockPos origin) {
        if (level.getBiome(origin).is(TravelersTags.Biomes.PATH_AVOID)) {
            return;
        }
        int xStart = 0;
        BlockPos mutable$blockPos;
        for (int z = -3; z < 2 ; z++) {
            for (int x = xStart; x < xStart + 5; x++) {
                mutable$blockPos = origin.offset(x, 0, z);
                mutable$blockPos = findY(level, mutable$blockPos);
                this.setBlock(level, mutable$blockPos, random);
            }
            xStart --;
        }
    }

    public void placeDiagonalRL(ServerLevel level, RandomSource random, BlockPos origin) {
        if (level.getBiome(origin).is(TravelersTags.Biomes.PATH_AVOID)) {
            return;
        }
        int xStart = -5;
        BlockPos mutable$blockPos;
        for (int z = -3; z < 2 ; z++) {
            for (int x = xStart; x < xStart + 5; x++) {
                mutable$blockPos = origin.offset(x, 0, z);
                mutable$blockPos = findY(level, mutable$blockPos);
                this.setBlock(level, mutable$blockPos, random);
            }
            xStart ++;
        }
    }

    public void setBlock(Level level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, this.pathStyle.getPathBlock(level.getBlockState(pos), random), 2);
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

    public boolean isInProgress() {
        return !this.completed;
    }
}
