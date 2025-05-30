package com.amber_roads.worldgen;

import com.amber_roads.TravelersCrossroads;
import com.amber_roads.util.TravelersDirection;
import com.amber_roads.util.TravelersPath;
import com.amber_roads.worldgen.custom.StyleModifier;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.amber_roads.util.TravelersUtil.chunkDistanceTo;
import static com.amber_roads.util.TravelersUtil.chunkMatch;

public class TravelersCrossroad {
    
    private final ChunkPos chunkPos;
    private List<ChunkPos> connections;
    private List<TravelersDirection> connectionDirections;
    private final RandomSource randomSource;


    public TravelersCrossroad(ChunkPos pos, RandomSource source) {
        this.chunkPos = pos;
        this.connections = new ArrayList<>();
        this.connectionDirections = new ArrayList<>();
        this.randomSource = source;
    }

    public TravelersCrossroad(CompoundTag tag, int index) {
        CompoundTag data = tag.getCompound("crossroad" + index);
        this.randomSource = RandomSource.create(data.getLong("Seed"));
        this.chunkPos = new ChunkPos(NbtUtils.readBlockPos(data, "ChunkPos").orElseThrow());
        this.connections = new ArrayList<>();
        CompoundTag connectionData = data.getCompound("crossroad");
        for (int i = 0; i < data.getInt("length"); i++) {
            this.connections.add(new ChunkPos(NbtUtils.readBlockPos(connectionData, String.valueOf(i)).orElseThrow()));
        }
        this.connectionDirections = new ArrayList<>();
    }

    public CompoundTag save(CompoundTag tag, int index) {
        CompoundTag data = new CompoundTag();
        data.put("ChunkPos", NbtUtils.writeBlockPos(this.chunkPos.getWorldPosition()));
        CompoundTag connectionData = new CompoundTag();
        for (int i = 0; i < this.connections.size(); i++) {
            ChunkPos position = this.connections.get(i);
            connectionData.put(String.valueOf(i), NbtUtils.writeBlockPos(position.getWorldPosition()));
            i++;
        }
        data.put("crossroad", connectionData);
        data.putInt("length", this.connections.size());

        tag.put("crossroad" + index, data);
        return tag;
    }
    
    public List<TravelersPath> addConnections(StyleModifier style) {
        // Add 3 connections if first load.
        TravelersCrossroads.LOGGER.debug("Adding connections");
        TravelersDirection startDirection;
        TravelersDirection nextDirection;
        ChunkPos nextPos = this.chunkPos;
        ArrayList<TravelersPath> paths = new ArrayList<>();
        // add 1-3 connections
        for (int i = 0; i < this.randomSource.nextInt(2) + 1; i++) {
            do {
                startDirection = TravelersDirection.getRandom(this.randomSource);
            } while (this.connectionDirections.contains(startDirection));

            nextPos = new ChunkPos(nextPos.x + startDirection.getX(), nextPos.z + startDirection.getZ());
            // Move the end point of the new connection up to 3-6 chunks away
            for (int j = 0; j < this.randomSource.nextInt(3 + 3); j++) {
                nextDirection = TravelersDirection.getRandomNarrowForDirection(this.randomSource, startDirection);
                nextPos = new ChunkPos(nextPos.x + nextDirection.getX(), nextPos.z + nextDirection.getZ());
            }
            paths.add(new TravelersPath(this.chunkPos, nextPos, this.randomSource, startDirection, style));
            this.connectionDirections.addAll(startDirection.getNeighbors());
            this.connections.add(nextPos);
            // TravelersCrossroads.LOGGER.debug("Connections {}", this.connectionDirections);
        }

        return paths;
    }

    public Optional<TravelersPath> addStructure(ChunkPos structPos, StyleModifier style) {
        TravelersCrossroads.LOGGER.debug("Adding structure {} to {} distance {}", structPos, this.chunkPos, chunkDistanceTo(structPos, this.chunkPos));
        ChunkPos closest = this.chunkPos;
        int distance = 0;
        for (ChunkPos connection: this.connections) {
            distance = chunkDistanceTo(structPos, connection);
            closest = distance < chunkDistanceTo(closest, structPos) ? connection: closest;

        }
        if (distance <= 1) {
            return Optional.empty();
        }
        return Optional.of(
                new TravelersPath(closest,
                        structPos, randomSource,
                        TravelersDirection.directionFromChunks(closest, structPos),
                        style
                )
        );
    }

    public List<BlockPos> getConnections() {
        return connections.stream().map(chunkPos -> chunkPos.getMiddleBlockPosition(0)).toList();
    }
}
