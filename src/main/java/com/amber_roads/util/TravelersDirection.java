package com.amber_roads.util;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;

public enum TravelersDirection implements StringRepresentable {
    NORTH(0, 0, 1, "north"),
    NORTHEAST(1, 1, 1, "northeast"),
    EAST(2, 1, 0, "east"),
    SOUTHEAST(3, 1, -1, "southeast"),
    SOUTH(4, 0, -1, "south"),
    SOUTHWEST(5, -1, -1, "southwest"),
    WEST(6, -1, 0, "west"),
    NORTHWEST(7, -1, 1, "northwest");

    private final int index;
    private final int x;
    private final int z;
    private final String name;
    private static final int[][] indexByZX = {{3, 4, 5}, {6, -1, 2}, {1, 0, 7}};
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

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public static TravelersDirection directionFromChunks(ChunkPos pos1, ChunkPos pos2) {
        int x = Mth.clamp(pos2.x - pos1.x, -1, 1);
        int z = Mth.clamp(pos2.z - pos1.z, -1, 1);
        return VALUES[indexByZX[z + 1][x + 1]];
    }

    public static ArrayList<TravelersDirection> weightedList(TravelersDirection direction) {
        ArrayList<TravelersDirection> list = new ArrayList<>();
        list.add(direction);
        list.add(direction);
        list.add(direction);
        list.add(VALUES[Mth.abs(direction.index + 1 - 8)]);
        list.add(VALUES[Mth.abs(direction.index + 1 - 8)]);
        list.add(VALUES[Mth.abs(direction.index + 2 - 8)]);
        list.add(VALUES[Mth.abs(direction.index + 2 - 8)]);
        list.add(VALUES[Mth.abs(direction.index + 7 - 8)]);
        list.add(VALUES[Mth.abs(direction.index + 7 - 8)]);
        list.add(VALUES[Mth.abs(direction.index + 6 - 8)]);
        list.add(VALUES[Mth.abs(direction.index + 6 - 8)]);

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

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
