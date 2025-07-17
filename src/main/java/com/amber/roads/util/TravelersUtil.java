package com.amber.roads.util;

import com.amber.roads.world.PathNode;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

import static net.minecraft.util.Mth.floor;

public class TravelersUtil {
    static final Logger LOGGER = LogUtils.getLogger();

    // From SinfulCynic on ForgeForums : https://forums.minecraftforge.net/topic/74979-1144-rotate-voxel-shapes/
    public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, Shapes.empty()};

        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1], Shapes.create(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }

        return buffer[0];
    }

    public static int chunkDistanceTo(ChunkPos start, ChunkPos end) {
        return Mth.floor(distanceTo2D(end.x - start.x, end.z - start.z));
    }

    public static boolean chunkMatch(ChunkPos pos1, ChunkPos pos2) {
        return pos1.x == pos2.x && pos1.z == pos2.z;
    }

    public static double distanceTo2D(double side, double side2) {
        return  Math.sqrt(side * side + side2 * side2);
    }

    public static double distanceTo2D(BlockPos origin, BlockPos end) {
        double dX = Math.abs(origin.getX() - end.getX());
        double dZ = Math.abs(origin.getZ() - end.getZ());
        return Math.sqrt(dX * dX + dZ * dZ);
    }

    public static double distanceTo2D(PathNode origin, PathNode end) {
        double dX = Math.abs(end.getX() - origin.getX());
        double dZ = Math.abs(end.getZ() - origin.getZ());
        return Math.sqrt(dX * dX + dZ * dZ);
    }

    public static ChunkPos offsetChunk(ChunkPos pos, int xOffset, int zOffset) {
        return new ChunkPos(pos.x + xOffset, pos.z + zOffset);
    }

    public static BlockPos offsetBlockPos(BlockPos pos, float x, float z) {
        return new BlockPos(floor(pos.getX() + x), 0, floor(pos.getZ() + z));
    }

    public static float roundToHalf(float d) {
        return Math.round(d * 2) / 2.0f;
    }

    public static boolean isEven(int i) {
        return (i & 1) == 0;
    }
}
