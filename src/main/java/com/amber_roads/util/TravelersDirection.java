package com.amber_roads.util;

import com.amber_roads.TravelersCrossroads;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public enum TravelersDirection implements StringRepresentable {
    NORTH(0, 0, -1, "north"),
    NORTHEAST(1, 1, -1, "northeast"),
    EAST(2, 1, 0, "east"),
    SOUTHEAST(3, 1, 1, "southeast"),
    SOUTH(4, 0, 1, "south"),
    SOUTHWEST(5, -1, 1, "southwest"),
    WEST(6, -1, 0, "west"),
    NORTHWEST(7, -1, -1, "northwest");

    private final int index;
    private final int x;
    private final int z;
    private final String name;
    private static final int[][] indexByXZ = {{7, 6, 5}, {0, -1, 4}, {1, 2, 3}};
    private static final TravelersDirection[] VALUES = values();

    private TravelersDirection(
        int index, int x, int z, String name
    ) {
        this.index = index;
        this.x = x;
        this.z = z;
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public int getOppositeIndex() {
        return Math.abs((index + 4) - 8);
    }

    public int getModifiedIndex(int modifier) {
        int newIndex = index + modifier;

        if (newIndex > 7) {
            newIndex = newIndex - 8;
        } else if (newIndex < 0) {
            newIndex = newIndex + 8;
        }
        // TravelersCrossroads.LOGGER.debug("Initial index {}  new index {}", index, newIndex);
        return newIndex;
    }

    public Collection<? extends TravelersDirection> getNeighbors() {
        return List.of(this, VALUES[getModifiedIndex(1)], VALUES[getModifiedIndex(-1)]);
    }

    public TravelersDirection getOpposite() {
        return VALUES[getOppositeIndex()];
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public static TravelersDirection directionFromChunks(ChunkPos pos1, ChunkPos pos2) {
        int x = Mth.clamp(pos2.x - pos1.x, -1, 1);
        int z = Mth.clamp(pos2.z - pos1.z, -1, 1);
        return VALUES[indexByXZ[x + 1][z + 1]];
    }

    public static ArrayList<TravelersDirection> weightedList(TravelersDirection direction) {
        ArrayList<TravelersDirection> list = new ArrayList<>();
        int above = direction.getModifiedIndex(1);
        int above2 = direction.getModifiedIndex(2);
        int below = direction.getModifiedIndex(-1);
        int below2 = direction.getModifiedIndex(-2);

        list.add(direction);
        list.add(direction);
        list.add(direction);

        list.add(VALUES[above]);
        list.add(VALUES[above]);
        list.add(VALUES[above2]);
        list.add(VALUES[above2]);
        list.add(VALUES[below]);
        list.add(VALUES[below]);
        list.add(VALUES[below2]);
        list.add(VALUES[below2]);

        return list;
    }

    public static ArrayList<TravelersDirection> weightedNarrowList(TravelersDirection direction) {
        ArrayList<TravelersDirection> list = new ArrayList<>();

        int above = direction.getModifiedIndex(1);
        int below = direction.getModifiedIndex(-1);
        list.add(direction);
        list.add(direction);
        list.add(direction);
        list.add(VALUES[above]);
        list.add(VALUES[above]);
        list.add(VALUES[below]);
        list.add(VALUES[below]);

        return list;
    }

    public static TravelersDirection getRandom(RandomSource random) {
        return values()[random.nextInt(8)];
    }

    public static TravelersDirection getRandomList(RandomSource random, ArrayList<TravelersDirection> weightedList) {
        return weightedList.get(random.nextInt(weightedList.size()));
    }

    public static TravelersDirection getRandomForDirection(RandomSource random, TravelersDirection direction) {
        return getRandomList(random, weightedList(direction));
    }

    public static TravelersDirection getRandomNarrowList(RandomSource random, ArrayList<TravelersDirection> weightedNarrowList) {
        return weightedNarrowList.get(random.nextInt(weightedNarrowList.size()));
    }

    public static TravelersDirection getRandomNarrowForDirection(RandomSource random, TravelersDirection direction) {
        return getRandomNarrowList(random, weightedNarrowList(direction));
    }



    @Override
    public String getSerializedName() {
        return this.name;
    }
}
