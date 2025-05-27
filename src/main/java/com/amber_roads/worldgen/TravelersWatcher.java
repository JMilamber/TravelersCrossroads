package com.amber_roads.worldgen;

import com.amber_roads.TravelersCrossroads;
import com.amber_roads.init.TravelersRegistries;
import com.amber_roads.util.TravelersTags;
import com.amber_roads.worldgen.custom.OffsetModifier;
import com.amber_roads.worldgen.custom.StyleModifier;
import com.amber_roads.worldgen.custom.PathModifiers;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import javax.annotation.Nullable;

import static com.amber_roads.util.TravelersUtil.chunkDistanceTo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = TravelersCrossroads.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class TravelersWatcher {

    public static ArrayList<ChunkPos> pathPositions = new ArrayList<>();
    public static MinecraftServer server;
    public static Registry<StyleModifier> pathStyles;
    public static Registry<OffsetModifier> pathOffsets;

    public TravelersWatcher() {}

    public static List<Pair<ChunkPos, Holder<Structure>>> getNearbyStructures(BlockPos center, List<BlockPos> blockPosList) {
        List<Pair<ChunkPos, Holder<Structure>>> structs = new ArrayList<>();

        Pair<BlockPos, Holder<Structure>> struct = findNearestMapStructure(TravelersTags.Structures.PATH_STRUCTURES, center);
            if (struct != null) {
                ChunkPos structChunkPos = new ChunkPos(struct.getFirst());
                int distance = chunkDistanceTo(new ChunkPos(center), structChunkPos);
                if (distance <= 20 && distance > 4) {
                    structs.add(Pair.of(structChunkPos, struct.getSecond()));
                }
            }

        for (BlockPos pos: blockPosList) {
            struct = findNearestMapStructure(TravelersTags.Structures.PATH_STRUCTURES, pos);
            if (struct != null) {
                ChunkPos structChunkPos = new ChunkPos(struct.getFirst());
                int distance = chunkDistanceTo(new ChunkPos(center), structChunkPos);
                if (distance <= 20 && distance > 1) {
                    structs.add(Pair.of(structChunkPos, struct.getSecond()));
                }
            }
        }

        return structs;
    }

    @Nullable
    public static Pair<BlockPos, Holder<Structure>> findNearestMapStructure(TagKey<Structure> structureTag, BlockPos pos) {
        if (!server.getWorldData().worldGenOptions().generateStructures()) {
            return null;
        } else {
            ServerLevel level = server.overworld();
            Optional<HolderSet.Named<Structure>> optional = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(structureTag);
            return optional.map(holders -> level.getChunkSource()
                    .getGenerator()
                    .findNearestMapStructure(level, holders, pos, 20, false)).orElse(null);
        }
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
        // The order of holders() is the order modifiers were loaded in.

        List<StyleModifier> styleModifiers = server.registryAccess().registryOrThrow(TravelersRegistries.Keys.STYLE_MODIFIERS)
                .holders()
                .map(Holder::value)
                .toList();

        pathOffsets = server.registryAccess().registryOrThrow(TravelersRegistries.Keys.OFFSET_MODIFIERS);
        pathStyles = server.registryAccess().registryOrThrow(TravelersRegistries.Keys.STYLE_MODIFIERS);
    }

}
