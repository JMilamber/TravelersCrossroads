package com.amber.roads.util;

import com.amber.roads.TravelersCrossroads;
import com.amber.roads.world.PathPos;
import com.amber.roads.worldgen.TravelersFeatures;
import com.amber.roads.worldgen.custom.pathstyle.PathStyle;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import static com.amber.roads.util.TravelersUtil.*;

public class TravelersPath {
    private PathStyle pathStyle;
    private final PathPos start;
    private PathPos end;
    private boolean completed;
    private ArrayList<TravelersDirection> pathDirections;
    private int directionId;
    private PathPos currentPos;

    public TravelersPath(
            PathPos start, PathPos end, RandomSource randomSource, TravelersDirection initialDirection,
            PathStyle pathStyle
    ) {
        this.start = start;
        this.currentPos = start;
        this.end = end;
        this.completed = false;
        this.pathDirections = new ArrayList<>();
        this.pathDirections.add(initialDirection);
        this.directionId = 0;
        this.pathStyle = pathStyle;
        this.createPath(randomSource);
        // TravelersCrossroads.LOGGER.debug("New path start: {} end {} || Path {}", start, end, pathDirections);
    }

    public TravelersPath(CompoundTag tag, int index) {

        CompoundTag data = tag.getCompound("path" + index);
        this.completed = data.getBoolean("complete");
        this.directionId = data.getInt("index");
        CompoundTag startData = data.getCompound("start");
        this.start = new PathPos(startData.getInt("x"), startData.getInt("z"));
        CompoundTag endData = data.getCompound("end");
        this.end = new PathPos(endData.getInt("x"), endData.getInt("z"));
        this.currentPos = this.start;
        this.pathDirections = new ArrayList<>();
        CompoundTag pathData = data.getCompound("pathDirections");
        for (int i = 0; i < data.getInt("length"); i++) {
            TravelersDirection nextDirection = TravelersDirection.valueOf(pathData.getString(String.valueOf(i)));
            this.pathDirections.add(nextDirection);
            if (i < this.directionId) {
                this.currentPos = nextDirection.nextPos(this.currentPos, this.pathStyle.getDistance());
            }
        }
        if (data.contains("style")) {
            PathStyle.DIRECT_CODEC
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

        for (int i = 0; i < this.pathDirections.size(); i++) {
            TravelersDirection direction = this.pathDirections.get(i);
            pathData.putString(String.valueOf(i), direction.getSerializedName());
            i++;
        }

        data.putBoolean("complete", this.completed);
        data.put("start", startData);
        data.put("end", endData);
        data.put("pathDirections", pathData);
        data.putInt("length", this.pathDirections.size());
        data.putInt("index", this.directionId);
        PathStyle.DIRECT_CODEC
                .encodeStart(NbtOps.INSTANCE, this.pathStyle)
                .resultOrPartial(TravelersCrossroads.LOGGER::error)
                .ifPresent(tag1 -> data.put("style", tag1));
        tag.put("path" + index, data);

        return tag;
    }

    public PathPos getEnd() {
        return this.end;
    }

    public void createPath(RandomSource randomSource) {
        int distance = this.pathStyle.getDistance();
        TravelersDirection initialDirection = pathDirections.getFirst();
        PathPos pathPos = initialDirection.nextPos(this.start, distance);
        TravelersDirection direction;
        TravelersDirection newDirection;
        int pathState = 0;

        while (distanceTo2D(pathPos, this.end) >= distance) {
            // TravelersCrossroads.LOGGER.debug("pos {} End {} Distance{}", pathPos, this.end, distanceTo2D(pathPos, this.end));
            direction = TravelersDirection.directionFromPos(pathPos, this.end, distance);
            // TravelersCrossroads.LOGGER.debug("Direction {}", direction);

            if (distanceTo2D(pathPos, this.end) < distance * 3) {
                this.pathDirections.add(direction);
                pathPos = direction.nextPos(pathPos, distance);
            } else {
                switch (pathState) {
                    case 1, 3 -> newDirection = TravelersDirection.getRandomForDirection(randomSource, direction);
                    case 2, 4 -> newDirection = TravelersDirection.getRandomNarrowForDirection(randomSource, direction);
                    case 6 -> {
                        pathState = 0;
                        newDirection = null;
                    }
                    default -> newDirection = direction;
                }
                if (newDirection != null) {
                    // TravelersCrossroads.LOGGER.debug("Direction 2 {}", newDirection);
                    this.pathDirections.add(newDirection);
                    pathPos = newDirection.nextPos(pathPos, distance);
                }
                pathState++;
            }
        }
        this.end = pathPos;
    }

    public void placeNextSection(ServerLevel level) {
        TravelersDirection nextPosDirection = this.pathDirections.get(this.directionId);
        // TravelersCrossroads.LOGGER.debug("Placing Chunk: {}" , placePos);

        if (this.pathStyle.placeSection(level, this.currentPos, nextPosDirection)) {
            this.directionId++;
            this.currentPos = nextPosDirection.nextPos(this.currentPos, pathStyle.getDistance());
        }

        if (this.directionId == this.pathDirections.size() - 1) {
            this.completed = true;
        }
    }

    public boolean isInProgress() {
        return !this.completed;
    }
}
