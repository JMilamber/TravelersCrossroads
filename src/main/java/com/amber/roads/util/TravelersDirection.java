package com.amber.roads.util;

import com.amber.roads.world.PathNode;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.amber.roads.util.TravelersUtil.roundToHalf;
import static java.lang.Math.abs;
import static net.minecraft.util.Mth.floor;

public enum TravelersDirection implements StringRepresentable {
    N(0, 0f, -1f, "N"),
    NNE(1, 0.5f, -1f, "NNE"),
    NE(2, 1f, -1f, "NE"),
    ENE(3, 1f, -0.5f, "ENE"),
    E(4, 1f, 0f, "E"),
    ESE(5, 1f, 0.5f, "ESE"),
    SE(6, 1f, 1f, "SE"),
    SSE(7, 0.5f, 1f, "SSE"),
    S(8, 0f, 1f, "S"),
    SSW(9, -0.5f, 1f, "SSW"),
    SW(10, -1f, 1f, "SW"),
    WSW(11, -1f, 0.5f, "WSW"),
    W(12, -1f, 0f, "W"),
    WNW(13, -1f, -0.5f, "WNW"),
    NW(14, -1f, -1f, "NW"),
    NNW(15, -0.5f, -1f, "NNW");

    private final int index;
    private final float x;
    private final float z;
    private final String name;
    private static final int[][] indexByXZ = {{14, 13, 12, 11, 10}, {15, -1, -1, -1, 9}, {0, -1, -1, -1, 8}, {1, -1, -1, -1, 7}, {2, 3, 4, 5, 6}};
    private static final TravelersDirection[] VALUES = values();
        /**
     * Normalized vector that points in the direction of this Direction
     */
    private final Vec2 normal;

    TravelersDirection(
            int index, float x, float z, String name
    ) {
        this.index = index;
        this.x = x;
        this.z = z;
        this.name = name;
        this.normal = new Vec2(x, z);
    }

    public int getIndex() {
        return index;
    }

    public int getOppositeIndex() {
        return index > 7 ? index - 8 : index + 8;
    }

    public int getModifiedIndex(int modifier) {
        int newIndex = index + modifier;

        if (newIndex > 15) {
            newIndex = newIndex - 16;
        } else if (newIndex < 0) {
            newIndex = newIndex + 16;
        }
        // TravelersCrossroads.LOGGER.debug("Initial index {}  new index {}", index, newIndex);
        return newIndex;
    }


    public Collection<? extends TravelersDirection> getNeighbors() {
        return List.of(this,
                VALUES[getModifiedIndex(1)], VALUES[getModifiedIndex(-1)],
                VALUES[getModifiedIndex(2)], VALUES[getModifiedIndex(-2)]
        );
    }

    public float getStepX() {
        return this.normal.x;
    }


    public float getStepZ() {
        return this.normal.y;
    }


    public TravelersDirection getOpposite() {
        return VALUES[getOppositeIndex()];
    }

    public float getX() {
        return x;
    }

    public int nextPosX(int distance) {
        return floor(getStepX() * distance);
    }

    public int nextSectionCenterX(int distance) {
        return nextPosX(distance) / 2;
    }

    public float getZ() {
        return z;
    }

    public int nextPosZ(int distance) {
        return floor(getStepZ()* distance);
    }

    public int nextSectionCenterZ(int distance) {
        return nextPosZ(distance) / 2;
    }

    public PathNode nextPos(PathNode pos, int distance) {
        return new PathNode(pos.x + nextPosX(distance), pos.z + nextPosZ(distance), false);
    }

    public BlockPos nextSectionCenter(PathNode pos, int distance) {
        return new BlockPos(pos.x + nextSectionCenterX(distance), 0, pos.z + nextSectionCenterZ(distance));
    }


    public static TravelersDirection directionFromPos(PathNode pos1, PathNode pos2) {
        float max = Math.max(abs(pos2.x - pos1.x), abs(pos2.z - pos1.z));
        double x = Mth.clamp(roundToHalf((pos2.x - pos1.x) / max), -1f, 1f);
        double z = Mth.clamp(roundToHalf((pos2.z - pos1.z) / max), -1f, 1f);
        // TravelersCrossroads.LOGGER.debug("Direction x {} z {} indexX {} indexZ {}", x, z, (x + 1) * 2, (z + 1) * 2);
        return VALUES[indexByXZ[(int) ((x + 1) * 2)][(int) ((z + 1) * 2)]];
    }

    public ArrayList<TravelersDirection> randomList() {
        ArrayList<TravelersDirection> list = this.narrowRandomList();
        int above2 = this.getModifiedIndex(2);
        int below2 = this.getModifiedIndex(-2);

        list.add(VALUES[above2]);
        list.add(VALUES[below2]);

        return list;
    }

    public ArrayList<TravelersDirection> narrowRandomList() {
        ArrayList<TravelersDirection> list = new ArrayList<>();

        int above = this.getModifiedIndex(1);
        int below = this.getModifiedIndex(-1);

        list.add(this);
        list.add(VALUES[above]);
        list.add(VALUES[below]);

        return list;
    }

    public static TravelersDirection getRandom(RandomSource random) {
        return values()[random.nextInt(8)];
    }

    public static TravelersDirection getRandomList(RandomSource random, ArrayList<TravelersDirection> weightedList) {
        return weightedList.get(random.nextInt(weightedList.size()));
    }

    public TravelersDirection getRandomForDirection(RandomSource random) {
        return getRandomList(random, this.randomList());
    }

    public static TravelersDirection getRandomNarrowList(RandomSource random, ArrayList<TravelersDirection> weightedNarrowList) {
        return weightedNarrowList.get(random.nextInt(weightedNarrowList.size()));
    }

    public TravelersDirection getRandomNarrowForDirection(RandomSource random) {
        return getRandomNarrowList(random, this.narrowRandomList());
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
