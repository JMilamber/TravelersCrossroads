package com.amber.roads.world;

import com.amber.roads.TravelersCrossroads;
import com.amber.roads.util.TravelersDirection;
import com.amber.roads.util.TravelersPath;
import com.amber.roads.worldgen.custom.pathstyle.PathStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.amber.roads.util.TravelersUtil.distanceTo2D;

public class TravelersCrossroad {
    
    private final PathPos pathPos;
    private List<PathPos> connections;
    private List<TravelersDirection> connectionDirections;
    private final RandomSource randomSource;


    public TravelersCrossroad(PathPos pos, RandomSource source) {
        this.pathPos = pos;
        this.connections = new ArrayList<>();
        this.connectionDirections = new ArrayList<>();
        this.randomSource = source;
    }

    public TravelersCrossroad(CompoundTag tag, int index) {
        CompoundTag data = tag.getCompound("crossroad" + index);
        this.randomSource = RandomSource.create(data.getLong("Seed"));
        this.pathPos = new PathPos(NbtUtils.readBlockPos(data, "ChunkPos").orElseThrow());
        this.connections = new ArrayList<>();
        CompoundTag connectionData = data.getCompound("crossroad");
        for (int i = 0; i < data.getInt("length"); i++) {
            this.connections.add(new PathPos(NbtUtils.readBlockPos(connectionData, String.valueOf(i)).orElseThrow()));
        }
        this.connectionDirections = new ArrayList<>();
    }

    public CompoundTag save(CompoundTag tag, int index) {
        CompoundTag data = new CompoundTag();
        data.put("ChunkPos", NbtUtils.writeBlockPos(this.pathPos.asBlockPos()));
        CompoundTag connectionData = new CompoundTag();
        for (int i = 0; i < this.connections.size(); i++) {
            PathPos position = this.connections.get(i);
            connectionData.put(String.valueOf(i), NbtUtils.writeBlockPos(position.asBlockPos()));
            i++;
        }
        data.put("crossroad", connectionData);
        data.putInt("length", this.connections.size());

        tag.put("crossroad" + index, data);
        return tag;
    }
    
    public List<TravelersPath> addConnections(PathStyle pathStyle) {
        // Add 3 connections if first load.
        // TravelersCrossroads.LOGGER.debug("Adding connections");
        TravelersDirection startDirection;
        TravelersDirection nextDirection;
        int distance = pathStyle.getDistance();
        PathPos nextPos = this.pathPos;
        ArrayList<TravelersPath> paths = new ArrayList<>();
        // add 1-3 connections
        for (int i = 0; i < this.randomSource.nextInt(2) + 1; i++) {
            do {
                startDirection = TravelersDirection.getRandom(this.randomSource);
                // TravelersCrossroads.LOGGER.debug("New Direction: {} | Current Connections : {}", startDirection, this.connectionDirections);
            } while (this.connectionDirections.contains(startDirection));

            nextPos = startDirection.nextPos(nextPos, distance);

            // Move the end point of the new connection up to 3-6 positions away
            for (int j = 0; j < this.randomSource.nextInt(3) + 3; j++) {
                nextDirection = TravelersDirection.getRandomNarrowForDirection(this.randomSource, startDirection);
                nextPos = nextDirection.nextPos(nextPos, distance);
            }
            paths.add(new TravelersPath(this.pathPos, nextPos, this.randomSource, startDirection, pathStyle));
            this.connectionDirections.addAll(startDirection.getNeighbors());
            this.connections.add(paths.getLast().getEnd());
            // TravelersCrossroads.LOGGER.debug("Connections {}", this.connectionDirections);
        }

        return paths;
    }

    public Optional<TravelersPath> addStructure(PathPos structPos, PathStyle pathStyle) {
        TravelersCrossroads.LOGGER.debug("Adding structure {} to path {} distance {}", structPos, this.pathPos, distanceTo2D(structPos, this.pathPos));
        PathPos closest = this.pathPos;
        double distance = 0;
        for (PathPos connection: this.connections) {
            distance = distanceTo2D(structPos, connection);
            closest = distance < distanceTo2D(closest, structPos) ? connection: closest;

        }
        if (distance <= pathStyle.getDistance()) {
            return Optional.empty();
        }
        return Optional.of(
                new TravelersPath(closest,
                        structPos, randomSource,
                        TravelersDirection.directionFromPos(closest, structPos, pathStyle.getDistance()),
                        pathStyle
                )
        );
    }

    public List<BlockPos> getConnections() {
        return connections.stream().map(PathPos::asBlockPos).toList();
    }
}
