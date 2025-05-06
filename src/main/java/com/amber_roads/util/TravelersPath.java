package com.amber_roads.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;

import static com.amber_roads.util.TravelersUtil.chunkDistanceTo;
import static com.amber_roads.util.TravelersUtil.chunkMatch;
import static java.lang.Math.abs;

public class TravelersPath {

    private final ChunkPos start;
    private final ChunkPos end;
    private boolean completed;
    private ArrayList<ChunkPos> path;
    private int pathIndex;

    public TravelersPath(ChunkPos start, ChunkPos end, RandomSource randomSource) {
        this.start = start;
        this.end = end;
        this.completed = false;
        this.pathIndex = 0;
        this.createPath(randomSource);
    }

    public TravelersPath(CompoundTag tag, int index) {
        CompoundTag data = tag.getCompound("path" + index);
        this.completed = data.getBoolean("complete");
        this.pathIndex = data.getInt("index");
        CompoundTag startData = data.getCompound("start");
        this.start = new ChunkPos(startData.getInt("x"), startData.getInt("z"));
        CompoundTag endData = data.getCompound("end");
        this.end = new ChunkPos(endData.getInt("x"), endData.getInt("z"));
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

    public void createPath(RandomSource randomSource) {
        ChunkPos pathPos = this.start;
        TravelersDirection direction = TravelersDirection.directionFromChunks(pathPos, this.end);
        TravelersDirection randDirection = TravelersDirection.getRandomForDirection(randomSource, direction);
        ChunkPos nextPathPos = new ChunkPos(pathPos.x + randDirection.getX(), pathPos.z + randDirection.getIndex());

        while (!chunkMatch(nextPathPos, this.end)) {
            this.path.add(nextPathPos);
            pathPos = nextPathPos;
            direction = TravelersDirection.directionFromChunks(pathPos, this.end);
            if (abs(pathPos.x - this.end.x) <= 1 && abs(pathPos.z - this.end.z) <= 1) {
                nextPathPos = new ChunkPos(pathPos.x + direction.getX(), pathPos.z + direction.getZ());
            } else {
                randDirection = TravelersDirection.getRandomForDirection(randomSource, direction);
                nextPathPos = new ChunkPos(pathPos.x + randDirection.getX(), pathPos.z + randDirection.getZ());
            }
        }
    }

    public ChunkPos getNextPathChunk() {
        this.pathIndex ++;
        if (pathIndex == this.path.size() - 1) {
            this.completed = true;
        }
        return this.path.get(this.pathIndex);
    }

}
