package com.amber.roads.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

import java.util.Objects;

public class PathNode {
    public static final PathNode ZERO = new PathNode(0, 0, false);
    public final int x;
    public final int z;
    public final boolean start;

    public PathNode(int x, int z, boolean start) {
        this.x = x;
        this.z = z;
        this.start = start;
    }

    public PathNode(BlockPos pos) {
        this(pos.getX(), pos.getZ(), false);
    }

    public PathNode(ChunkPos pos) {
        this(pos.getMiddleBlockX(), pos.getMiddleBlockZ(), false);
    }

     public PathNode(CompoundTag tag) {
        this.x = tag.getInt("x");
        this.z = tag.getInt("z");
        this.start = tag.getBoolean("start");
    }

    public CompoundTag save(CompoundTag tag, int index) {
        CompoundTag data = new CompoundTag();
        data.putInt("x", this.x);
        data.putInt("z", this.z);
        data.putBoolean("z", this.start);

        tag.put(String.valueOf(index), data);
        return tag;
    }

    public int getZ() {
        return z;
    }

    public int getX() {
        return x;
    }

    public int getChunkZ() {
        return SectionPos.blockToSectionCoord(z);
    }

    public int getChunkX() {
        return SectionPos.blockToSectionCoord(x);
    }

    public PathPos asPathPos() {
        return new PathPos(this.x,  this.z);
    }

    public BlockPos asBlockPos() {
        return new BlockPos(this.x, 0, this.z);
    }

    public BlockPos asBlockPos(int y) {
        return new BlockPos(this.x, y, this.z);
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
        PathNode pathNode = (PathNode) o;
        return getX() == pathNode.getX() && getZ() == pathNode.getZ();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getZ());
    }
}
