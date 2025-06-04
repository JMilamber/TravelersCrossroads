package com.amber.roads.util;

import com.amber.roads.TravelersCrossroads;
import com.amber.roads.worldgen.TravelersFeatures;
import com.amber.roads.worldgen.custom.StyleModifier;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
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
import java.util.Optional;

import static com.amber.roads.util.TravelersTags.Blocks.PATH_ABOVE;
import static com.amber.roads.util.TravelersTags.Blocks.PATH_BELOW;
import static com.amber.roads.util.TravelersUtil.LOGGER;
import static com.amber.roads.util.TravelersUtil.chunkMatch;
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

    private StyleModifier pathStyle;
    
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
        if (data.contains("style")) {
            StyleModifier.DIRECT_CODEC
                    .parse(new Dynamic<>(NbtOps.INSTANCE, data.get("style")))
                    .resultOrPartial(LOGGER::error)
                    .ifPresentOrElse(
                            tag1 -> this.pathStyle = tag1,
                            () -> this.pathStyle = TravelersCrossroads.WATCHER.pathStyles.getOrThrow(TravelersFeatures.DEFAULT_STYLE_KEY)
                            );
        } else {
            this.pathStyle = TravelersCrossroads.WATCHER.pathStyles.getOrThrow(TravelersFeatures.DEFAULT_STYLE_KEY);
        }
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
        StyleModifier.DIRECT_CODEC
                .encodeStart(NbtOps.INSTANCE, this.pathStyle)
                .resultOrPartial(TravelersCrossroads.LOGGER::error)
                .ifPresent(tag1 -> data.put("style", tag1));
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

        placeRoadPiece(level, center, previousChunk, nextChunk);

        if (this.pathIndex == this.path.size() - 1) {
            this.completed = true;
        }

        this.pathIndex++;
    }
    
    public void placeRoadPiece(ServerLevel level, BlockPos origin, @Nullable TravelersDirection previous, @Nullable TravelersDirection next) {
        placeCenter(level, origin);
        if (previous != null ) {
            placeCenterConnection(level, origin, previous);
        }
        if (next != null) {
            placeCenterConnection(level, origin, next);
        }
    }

    public void placeCenter(ServerLevel level, BlockPos origin) {
        Optional<BlockPos> mutable$blockPos;
        // TravelersCrossroads.LOGGER.debug("In center place");

        for (int x = -2; x < 2; x++) {
            // TravelersCrossroads.LOGGER.debug("X: {} ", x);
            for (int z = - 2; z < 2; z++) {
                // TravelersCrossroads.LOGGER.debug("Z: {} ", z);
                mutable$blockPos = findY(level, origin.offset(x, 0, z));
                // TravelersCrossroads.LOGGER.debug("Above: {} | At: {}| Y {}", level.getBlockState(mutable$blockPos.above()), level.getBlockState(mutable$blockPos), mutable$blockPos.getY());
                int finalX = x;
                int finalZ = z;
                mutable$blockPos.ifPresent(blockPos ->  this.pathStyle.setPathBlock(level, origin, finalX, finalZ));
            }
        }
    }

    public void placeCenterConnection(ServerLevel level, BlockPos origin, TravelersDirection direction) {
        Optional<BlockPos> mutable$blockPos;
        for (Pair<Integer, Integer> pair : CONNECTION_POS.get(direction)) {
            mutable$blockPos = findY(level, origin.offset(pair.getFirst(), 0 , pair.getSecond()));
            mutable$blockPos.ifPresent(blockPos ->  this.pathStyle.setPathBlock(level, origin, pair.getFirst(),  pair.getSecond()));
        }
        BlockPos placementCenter;
        int startX;
        int endX;
        int startZ;
        int endZ;

        switch (direction) {
            case NORTHEAST, SOUTHWEST -> {
                placementCenter = origin.offset(direction.getX() * 5, 0, direction.getZ() * 5);
                startX = 0;
                endX = 5;
                startZ = -3;
                endZ = 3;
            }
            case EAST, WEST -> {
                placementCenter = origin.offset(direction.getX() * 5, 0, 0);
                startX = -3;
                endX = 3;
                startZ = -2;
                endZ = 2;
            }
            case SOUTHEAST, NORTHWEST -> {
                placementCenter = origin.offset(direction.getX() * 5, 0, direction.getZ() * 5);
                startX = -5;
                endX = 0;
                startZ = -3;
                endZ = 3;
            }
            default -> { // NORTH, SOUTH
                placementCenter = origin.offset(0, 0, direction.getZ() * 5);
                startX = -2;
                endX = 2;
                startZ = -3;
                endZ = 3;
            }
        }


        if (level.getBiome(placementCenter).is(TravelersTags.Biomes.PATH_AVOID)) {
            return;
        }

        for (int x = startX; x < endX; x++) {
            for (int z = startZ; z < endZ; z++) {
                mutable$blockPos = findY(level, placementCenter.offset(x, 0, z));
                int finalX = x;
                int finalZ = z;
                mutable$blockPos.ifPresent(blockPos ->  this.pathStyle.setPathBlock(level, origin, finalX, finalZ));
            }
        }

    }

    public static Optional<BlockPos> findY(ServerLevel level, BlockPos origin) {

        while (!level.getBlockState(origin.above()).is(PATH_ABOVE) || !level.getBlockState(origin).is(PATH_BELOW)) {
            //TravelersCrossroads.LOGGER.info(
            //        "Blockstates: {} {} {} {}",
            //        level.getBlockState(origin.above()), level.getBlockState(origin.above()).is(PATH_ABOVE),
            //        level.getBlockState(origin), level.getBlockState(origin).is(PATH_BELOW)
            //);
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

    public boolean isInProgress() {
        return !this.completed;
    }
}
