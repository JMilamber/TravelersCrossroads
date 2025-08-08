package com.amber.roads.world;

import com.amber.roads.TravelersCrossroads;
import com.amber.roads.util.TravelersDirection;
import com.amber.roads.worldgen.TravelersFeatures;
import com.amber.roads.worldgen.TravelersWatcher;
import com.amber.roads.worldgen.custom.pathstyle.PathStyle;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Objects;

import static com.amber.roads.util.TravelersUtil.*;

public class TravelersPath {
    private PathStyle pathStyle;
    private boolean completed;
    private final ArrayList<PathNode> path;
    private int currentIndex;

    public TravelersPath(
            RandomSource randomSource,
            PathNode startNode, PathNode endNode,
            PathStyle pathStyle
    ) {
        this.currentIndex = 0;
        this.completed = false;
        this.path = new ArrayList<>();
        this.path.add(startNode);
        this.pathStyle = pathStyle;
        // TravelersCrossroads.LOGGER.debug("New path startNode: {} endNode {} || Path {}", startNode, endNode, pathDirections);

        int step;
        int styleDistance = pathStyle.getDistance();
        int doubleDistance = styleDistance * 2;
        PathNode currentNode = startNode;
        TravelersDirection nextDir;

        while (distanceTo2D(currentNode, endNode) > doubleDistance) {
            step = randomSource.nextInt(10);
            nextDir = TravelersDirection.directionFromPos(currentNode, endNode);
            if (step > 8) {
                nextDir = nextDir.getRandomNarrowForDirection(randomSource);
            } else if (step > 4) {
                nextDir = nextDir.getRandomForDirection(randomSource);
            }
            currentNode = nextDir.nextPos(currentNode, styleDistance);

            this.path.add(currentNode);
        }
        this.path.add(endNode);
    }

    public TravelersPath(CompoundTag tag, int index) {
        TravelersCrossroads.LOGGER.debug("Loading path");
        CompoundTag data = tag.getCompound("path" + index);
        this.completed = data.getBoolean("complete");
        this.path = new ArrayList<>();
        CompoundTag pathData = data.getCompound("path");
        for (int i = 0; i < data.getInt("length"); i++) {
            this.path.add(new PathNode(pathData.getCompound(String.valueOf(i))));
        }
        this.currentIndex = data.getInt("currentIndex");
        if (data.contains("style")) {
            PathStyle.REFERENCE_CODEC
                    .parse(new Dynamic<>(NbtOps.INSTANCE, data.get("style")))
                    .resultOrPartial(TravelersCrossroads.LOGGER::error)
                    .ifPresentOrElse(
                            tag1 -> this.pathStyle = tag1.value(),
                            () -> this.pathStyle = TravelersWatcher.pathStyleReg.getOrThrow(TravelersFeatures.DEFAULT_STYLE_KEY)
                    );
        } else {
            this.pathStyle = TravelersWatcher.pathStyleReg.getOrThrow(TravelersFeatures.DEFAULT_STYLE_KEY);
        }
    }

    public CompoundTag save(CompoundTag tag, int index) {
        CompoundTag data = new CompoundTag();
        CompoundTag pathData = new CompoundTag();

        for (int i = 0; i < this.path.size(); i++) {
            PathNode node = this.path.get(i);
            node.save(pathData, i);
        }
        data.putInt("currentIndex", this.currentIndex);
        data.putBoolean("complete", this.completed);
        data.put("path", pathData);
        data.putInt("length", this.path.size());
        PathStyle.REFERENCE_CODEC
                .encodeStart(NbtOps.INSTANCE, Holder.direct(this.pathStyle))
                .resultOrPartial(TravelersCrossroads.LOGGER::error)
                .ifPresent(tag1 -> data.put("style", tag1));
        tag.put("path" + index, data);

        return tag;
    }

    public PathNode getEnd() {
        return this.path.getLast();
    }

    public void placeNextSection(ServerLevel level) {
        // TravelersCrossroads.LOGGER.debug("Placing Section for Path: {} {}", path.getFirst(), path.getLast());

        if (this.currentIndex < this.path.size() - 1) {
            this.currentIndex += this.pathStyle.placeSection(level, this.path.get(this.currentIndex), this.path.get(this.currentIndex + 1)) ? 1 : 0;
            if (this.currentIndex % this.pathStyle.getNodeDistance() == 0 && this.path.size() - this.currentIndex > this.pathStyle.getNodeDistance()) {
                TravelersWatcher.crossroadsData.addPathNode(this.path.get(this.currentIndex+1));
            }
        } else {
            this.completed = true;
        }

    }

    public boolean completed() {
        return this.completed;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TravelersPath that = (TravelersPath) o;
        return Objects.equals(path.getFirst(), that.path.getFirst()) & Objects.equals(path.getLast(), that.path.getLast());
    }

    @Override
    public int hashCode() {
        return Objects.hash(path.getFirst(), path.getLast());
    }
}
