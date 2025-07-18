package com.amber.roads.world;

import com.amber.roads.util.TravelersDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

import java.util.Objects;

import static net.minecraft.util.Mth.floor;

public class PathPos {
    public static final PathPos ZERO = new PathPos(0, 0);
    public final float x;
    public final float z;

    public PathPos(float x, float z) {
        this.x = x;
        this.z = z;
    }

    public PathPos(BlockPos pos) {
        this(pos.getX(), pos.getZ());
    }

    public PathPos(ChunkPos pos) {
        this(pos.getMiddleBlockX(), pos.getMiddleBlockZ());
    }

    public PathPos offset(float dx, float dz) {
        return dx == 0 && dz == 0
            ? this
            : new PathPos(this.getX() + dx, this.getZ() + dz);
    }

    public PathPos offset(int dx, int dz) {
        return dx == 0 && dz == 0
            ? this
            : new PathPos(this.getX() + dx, this.getZ() + dz);
    }

    public PathPos relative(TravelersDirection direction) {
        return new PathPos(this.getX() + direction.getStepX(), this.getZ() + direction.getStepZ());
    }

    public float getZ() {
        return z;
    }

    public float getX() {
        return x;
    }

    public float getChunkZ() {
        return SectionPos.blockToSectionCoord(z);
    }

    public float getChunkX() {
        return SectionPos.blockToSectionCoord(x);
    }

    public BlockPos asBlockPos() {
        return new BlockPos(floor(this.x), 0, floor(this.z));
    }

    public BlockPos asBlockPos(int y) {
        return new BlockPos(floor(this.x), y, floor(this.z));
    }

    public ChunkPos asChunkPos() {
        return new ChunkPos(asBlockPos());
    }

    @Override
    public String toString() {
        return "X: " + x + "| Z: "+ z;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PathPos pathPos = (PathPos) o;
        return getX() == pathPos.getX() && getZ() == pathPos.getZ();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getZ());
    }
}
