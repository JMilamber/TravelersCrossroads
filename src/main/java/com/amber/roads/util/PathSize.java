package com.amber.roads.util;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum PathSize implements StringRepresentable {
    MINI("MINI", 1),
    SMALL("SMALL", 2),
    MEDIUM("MEDIUM", 3),
    LARGE("LARGE", 4),
    EXTRA_LARGE("EXTRA_LARGE", 5);


    private final int width;
    private final int extraWidth;
    private final int distance;
    private final String name;

    PathSize(String name, int width) {
        this.width = width;
        this.extraWidth = width + 2;
        this.name = name;
        this.distance = (width + 1) * 2;
    }

    public int getDistance() {
        return distance;
    }

    public int getWidth() {
        return width;
    }

    public int getExtraWidth() {
        return extraWidth;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }
}
