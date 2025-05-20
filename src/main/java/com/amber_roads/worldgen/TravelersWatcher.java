package com.amber_roads.worldgen;

import com.amber_roads.TravelersCrossroads;
import com.amber_roads.util.TravelersTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import static com.amber_roads.util.TravelersUtil.chunkDistanceTo;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = TravelersCrossroads.MODID, bus = EventBusSubscriber.Bus.GAME)
public class TravelersWatcher {

    public static ArrayList<ChunkPos> pathPositions = new ArrayList<>();
    public static MinecraftServer server;

    public TravelersWatcher() {}

    public static List<ChunkPos> getNearbyStructures(BlockPos center, List<BlockPos> blockPosList) {
        List<ChunkPos> structs = new ArrayList<>();
        BlockPos struct = server.overworld().findNearestMapStructure(TravelersTags.Structures.PATH_AVOID, center, 20, false);
            if (struct != null) {
                int distance = chunkDistanceTo(new ChunkPos(center), new ChunkPos(struct));
                if (distance <= 20 && distance > 4) {
                    structs.add(new ChunkPos(struct));
                }
            }

        for (BlockPos pos: blockPosList) {
            struct = server.overworld().findNearestMapStructure(TravelersTags.Structures.PATH_AVOID, pos, 20, false);
            if (struct != null) {
                int distance = chunkDistanceTo(new ChunkPos(pos), new ChunkPos(struct));
                if (distance <= 20 && distance > 1) {
                    structs.add(new ChunkPos(struct));
                }
            }
        }
        return structs;
    }

    public static MinecraftServer getServer() {
        return server;
    }


    public static int getClosest(BlockPos checkPos) {
        int distance = 1000;
        ChunkPos chunkPos = new ChunkPos(checkPos);
        for (ChunkPos pathPos : pathPositions) {
            distance = Math.min(distance, chunkDistanceTo(chunkPos, pathPos));
        }
        return pathPositions.isEmpty() ? 100 : distance;
    }

    public static void addDistanceFilterPath(ChunkPos pos) {
        pathPositions.add(pos);
    }


    @SubscribeEvent
    public static void serverSetup(ServerAboutToStartEvent event) {
        server = event.getServer();
    }

}
