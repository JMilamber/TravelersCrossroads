package com.amber_roads.util;

import com.amber_roads.TravelersCrossroads;
import com.amber_roads.worldgen.TravelersBeginning;
import com.amber_roads.worldgen.TravelersFeatures;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import java.util.ArrayList;

import static com.amber_roads.util.TravelersUtil.chunkMatch;
import static java.lang.Math.abs;

public class TravelersPath {

    private final ChunkPos start;
    private final ChunkPos end;
    private boolean completed;
    private ArrayList<ChunkPos> path;
    private int pathIndex;
    private TravelersDirection initialDirection;

    public TravelersPath(ChunkPos start, ChunkPos end, RandomSource randomSource, TravelersDirection initialDirection) {
        this.start = start;
        this.end = end;
        this.completed = false;
        this.path = new ArrayList<>();
        this.pathIndex = 1;
        this.initialDirection = initialDirection;
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
        int i = 0;
        for (ChunkPos pos: this.path) {
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
        data.putInt("length", i);
        data.putInt("index", this.pathIndex);
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

        if (this.pathIndex == 1) {
            TravelersBeginning.placeCenterConnection(level, randomSource, this.path.getFirst().getMiddleBlockPosition(generationY), TravelersDirection.directionFromChunks(this.path.getFirst(), placeChunk));
        }
        if (!level.getBiome(center).is(TravelersTags.Biomes.PATH_AVOID)) {
            TravelersBeginning.placeRoadPiece(level, randomSource, center, previousChunk, nextChunk);
        }

        if (this.pathIndex == this.path.size() - 1) {
            this.completed = true;
        }

        this.pathIndex++;
    }

    public boolean isInProgress() {
        return !this.completed;
    }
}
