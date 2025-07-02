package com.amber.roads.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

public class PathPos {
    public static final PathPos ZERO = new PathPos(0, 0);
    public final int x;
    public final int z;

    public PathPos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public PathPos(BlockPos pos) {
        this.x = pos.getX();
        this.z = pos.getZ();
    }

    public PathPos(ChunkPos pos) {
        this.x = pos.getMiddleBlockX();
        this.z = pos.getMiddleBlockZ();
    }

     public PathPos(CompoundTag tag) {
        this.x = tag.getInt("x");
        this.z = tag.getInt("z");

    }

    public CompoundTag save(CompoundTag tag, int index) {
        CompoundTag data = new CompoundTag();
        data.putInt("x", this.x);
        data.putInt("z", this.z);

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

    public BlockPos asBlockPos() {
        return new BlockPos(this.x, 0, this.z);
    }

    public BlockPos asBlockPos(int y) {
        return new BlockPos(this.x, y, this.z);
    }

    public ChunkPos asChunkPos() {
        return new ChunkPos(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
    }

    @Override
    public String toString() {
        return "X: " + x + "| Z: "+ z;
    }
}
