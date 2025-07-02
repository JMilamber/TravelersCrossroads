package com.amber.roads.util;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public enum PathSize implements StringRepresentable {
    TINY("TINY", 3),
    SMALL("SMALL", 4),
    MEDIUM("MEDIUM", 5),
    LARGE("LARGE", 6);

    private final int size;
    private final int distance;
    private final String name;

    PathSize(String name, int size) {
        this.size = size;
        this.name = name;
        this.distance = (size - 1) * 2;
    }

    public int getDistance() {
        return distance;
    }

    public int getSize() {
        return size;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }
}
