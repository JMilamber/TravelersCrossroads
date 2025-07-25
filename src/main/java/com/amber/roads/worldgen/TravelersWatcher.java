package com.amber.roads.worldgen;

import com.amber.roads.TravelersCrossroads;
import com.amber.roads.init.TravelersRegistries;
import com.amber.roads.util.CrossroadsData;
import com.amber.roads.util.TravelersDirection;
import com.amber.roads.world.TravelersPath;
import com.amber.roads.util.TravelersTags;
import com.amber.roads.world.PathNode;
import com.amber.roads.worldgen.custom.OffsetModifier;
import com.amber.roads.worldgen.custom.pathstyle.PathStyle;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nullable;

import static com.amber.roads.util.TravelersUtil.chunkDistanceTo;
import static com.amber.roads.util.TravelersUtil.distanceTo2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = TravelersCrossroads.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class TravelersWatcher {

    // Changed on Server start
    public MinecraftServer server;
    public Registry<PathStyle> pathStyleReg;
    public Registry<OffsetModifier> pathOffsets;
    public List<PathStyle> pathStyles;
    public RandomSource randomSource;

    // Changed while playing
    public int tickCount = 0;
    public CrossroadsData crossroadsData = null;
    public ArrayList<BlockPos> newCrossroadPositions = new ArrayList<>();
    public ArrayList<ChunkPos> beforeServerLoadNodes = new ArrayList<>();

    public TravelersWatcher() {}

    public void addCrossroadToCreate(BlockPos center) {
        this.newCrossroadPositions.add(center);
    }

    public void placeCrossroad(BlockPos center) {
        Holder<Biome> biome = this.server.overworld().getBiome(center);
        PathNode centerPos = new PathNode(center);

        // Get list of possible PathStyles
        List<PathStyle> possiblePathStyles = this.pathStyles.stream()
                .filter(pathBiomeStyleHolder -> pathBiomeStyleHolder.checkBiome(biome))
                .toList();
        // TravelersCrossroads.LOGGER.debug("Biome {} Possible Styles {}", biome.getRegisteredName(), possibleStyles);
        PathStyle pathStyle = possiblePathStyles.get(this.randomSource.nextInt(possiblePathStyles.size()));

        List<TravelersPath> connectionPaths = this.placeConnectionPaths(pathStyle, centerPos);
        List<PathNode> connectionNodes = new ArrayList<>();
        connectionNodes.add(centerPos);

        for (TravelersPath path : connectionPaths) {
            this.addPath(path);
            connectionNodes.add(path.getEnd());
            this.crossroadsData.addPathNode(path.getEnd());
        }

        // Find nearby structures and add paths to tower.
        List<Pair<ChunkPos, Holder<Structure>>> structures = this.getNearbyStructures(center, connectionNodes);
        for (Pair<ChunkPos, Holder<Structure>> struct : structures) {
            OffsetModifier offset = null;
            for (OffsetModifier offsetCheck: pathOffsets.holders().map(Holder::value).toList()) {
                if (offsetCheck.checkStructure(struct.getSecond())) {
                    offset = offsetCheck;
                }
            }
            if (offset == null) {
                offset = this.pathOffsets.getOrThrow(TravelersFeatures.DEFAULT_OFFSET_KEY);
            }

            PathNode structPathPos = new PathNode(struct.getFirst());
            TravelersDirection direction = TravelersDirection.directionFromPos(structPathPos, centerPos);

            structPathPos = direction.nextPos(structPathPos, offset.getOffset());

            PathNode closest = centerPos;
            double distance = 0;
            for (PathNode connection: connectionNodes) {
                distance = distanceTo2D(structPathPos, connection);
                closest = distance < distanceTo2D(closest, structPathPos) ? connection: closest;
            }
            if (distance <= pathStyle.getDistance()) {
                break;
            }
            this.addPath(new TravelersPath(this.randomSource, closest, structPathPos, pathStyle));
            TravelersCrossroads.LOGGER.debug("Structure Path - Start:  {} | End: {} | Structure {}", closest, structPathPos, struct.getSecond().getRegisteredName());
        }

        // If no nearby structure check for nearby pathNode to connect to.
        if (structures.isEmpty()) {
            Optional<ChunkPos> crossRoadPos = getClosestAvoidCurrent(center, 30, connectionNodes);
            crossRoadPos.ifPresent(pos -> this.addPath(new TravelersPath(this.randomSource, centerPos, new PathNode(pos), pathStyle)));
        }
    }

    public List<TravelersPath> placeConnectionPaths(PathStyle pathStyle, PathNode centerPos) {
        // Add 3 connections if first load.
        // TravelersCrossroads.LOGGER.debug("Adding connections");
        List<TravelersDirection> connectionDirections = new ArrayList<>();
        List<TravelersPath> paths = new ArrayList<>();
        TravelersDirection startDirection;
        TravelersDirection nextDirection;
        int distance = pathStyle.getDistance();
        PathNode nextPos = centerPos;
        // add 1-3 connections
        for (int i = 0; i < this.randomSource.nextInt(2) + 1; i++) {
            do {
                startDirection = TravelersDirection.getRandom(this.randomSource);
                // TravelersCrossroads.LOGGER.debug("New Direction: {} | Current Connections : {}", startDirection, this.connectionDirections);
            } while (connectionDirections.contains(startDirection));

            nextPos = startDirection.nextPos(nextPos, distance);

            // Move the end point of the new connection up to 7-13 positions away (less when bigger paths)
            for (int j = 0; j < this.randomSource.nextInt(7 - pathStyle.getWidth()) + 7; j++) {
                nextDirection = startDirection.getRandomForDirection(this.randomSource);
                nextPos = nextDirection.nextPos(nextPos, distance);
            }
            paths.add(new TravelersPath(this.randomSource, centerPos, nextPos, pathStyle));
            connectionDirections.addAll(startDirection.getNeighbors());
            // TravelersCrossroads.LOGGER.debug("Connections {}", connectionDirections);
        }
        return paths;
    }

    public void addPath(TravelersPath path) {
        this.crossroadsData.addPath(path);
        this.crossroadsData.addPathNode(path.getEnd());
    }

    public List<Pair<ChunkPos, Holder<Structure>>> getNearbyStructures(BlockPos center, List<PathNode> pathNodeList) {
        List<Pair<ChunkPos, Holder<Structure>>> structs = new ArrayList<>();
        ChunkPos centerChunk = new ChunkPos(center);

        Pair<BlockPos, Holder<Structure>> struct = findNearestMapStructure(TravelersTags.Structures.PATH_STRUCTURES, center);
            if (struct != null) {
                ChunkPos structChunkPos = new ChunkPos(struct.getFirst());
                int distance = chunkDistanceTo(centerChunk, structChunkPos);
                if (distance <= 20 && distance > 4) {
                    structs.add(Pair.of(structChunkPos, struct.getSecond()));
                }
            }

        for (PathNode pos: pathNodeList) {
            struct = findNearestMapStructure(TravelersTags.Structures.PATH_STRUCTURES, pos.asBlockPos());
            if (struct != null) {
                ChunkPos structChunkPos = new ChunkPos(struct.getFirst());
                int distance = chunkDistanceTo(centerChunk, structChunkPos);
                if (distance <= 20 && distance > 1) {
                    structs.add(Pair.of(structChunkPos, struct.getSecond()));
                    // TravelersCrossroads.LOGGER.debug("Structure found {} {}", struct.getFirst(), struct.getSecond());
                }
            }
        }

        return structs.stream().distinct().toList();
    }

    @Nullable
    public Pair<BlockPos, Holder<Structure>> findNearestMapStructure(TagKey<Structure> structureTag, BlockPos pos) {
        if (!server.getWorldData().worldGenOptions().generateStructures()) {
            return null;
        } else {
            ServerLevel level = server.overworld();
            Optional<HolderSet.Named<Structure>> optional = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(structureTag);
            return optional.map(holders -> level.getChunkSource()
                    .getGenerator()
                    .findNearestMapStructure(level, holders, pos, 30, false)).orElse(null);
        }
    }

    public Optional<ChunkPos> getClosest(BlockPos checkPos, int distance) {
        int newDist;
        Optional<ChunkPos> foundPos = Optional.empty();
        ChunkPos chunkPos = new ChunkPos(checkPos);

        if (this.crossroadsData == null) {
            for (ChunkPos pos : this.beforeServerLoadNodes) {
                newDist = chunkDistanceTo(chunkPos, pos);
                if (newDist < distance) {
                    distance = newDist;
                    foundPos = Optional.of(pos);
                }
            }
            return foundPos;
        }

        for (PathNode pathPos : this.crossroadsData.getPathNodes()) {
            newDist = chunkDistanceTo(chunkPos, pathPos.asChunkPos());
            if (newDist < distance) {
                distance = newDist;
                foundPos = Optional.of(pathPos.asChunkPos());
            }
        }
        return foundPos;
    }

    public Optional<ChunkPos> getClosestAvoidCurrent(BlockPos checkPos, int distance, List<PathNode> currentNodes) {
        int newDist;
        Optional<ChunkPos> foundPos = Optional.empty();
        ChunkPos chunkPos = new ChunkPos(checkPos);

        int farthestCurrent = 0;
        for (PathNode pathPos : currentNodes) {
            newDist = chunkDistanceTo(chunkPos, pathPos.asChunkPos());
            if (newDist > farthestCurrent) {
                farthestCurrent = newDist;
            }
        }

        if (this.crossroadsData == null) {
            for (ChunkPos pos : this.beforeServerLoadNodes) {
                newDist = chunkDistanceTo(chunkPos, pos);
                if (newDist < distance) {
                    distance = newDist;
                    foundPos = Optional.of(pos);
                }
            }
            return foundPos;
        }

        for (PathNode pathPos : this.crossroadsData.getPathNodes()) {
            newDist = chunkDistanceTo(chunkPos, pathPos.asChunkPos());
            if (newDist > farthestCurrent && newDist < distance) {
                distance = newDist;
                foundPos = Optional.of(pathPos.asChunkPos());
            }
        }
        return foundPos;
    }

    public void addDistanceFilterNode(ChunkPos pos) {
        if (this.crossroadsData == null) {
            this.beforeServerLoadNodes.add(pos);
        }
        else {
            this.crossroadsData.addPathNode(new PathNode(pos));
        }
    }

    public void removeDistanceFilterNode(ChunkPos pos) {
        if (this.crossroadsData == null) {
            this.beforeServerLoadNodes.remove(pos);
        }
        else {
            this.crossroadsData.removePathNode(new PathNode(pos));
        }
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        if (!event.hasTime()) {
          return;
        }
        TravelersCrossroads.WATCHER.buildPaths();
    }

    public void buildPaths () {
        this.tickCount++;
        if (tickCount % 3 != 0) {
            return;
        }

        if (!this.newCrossroadPositions.isEmpty()) {
            this.placeCrossroad(this.newCrossroadPositions.removeFirst());
        }

        List<TravelersPath> paths = this.crossroadsData.getUnfinishedPaths();
        for (TravelersPath path: paths) {
            if (!path.completed()) {
                try {
                    path.placeNextSection(this.server.overworld());
                } catch (Exception e) {
                    System.out.println("path placement failed: ");
                    e.printStackTrace();
                }
            }
        }

        this.crossroadsData.checkPaths();
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
        this.pathOffsets = server.registryAccess().registryOrThrow(TravelersRegistries.Keys.STRUCTURE_OFFSETS);
        this.pathStyleReg = server.registryAccess().registryOrThrow(TravelersRegistries.Keys.PATH_STYLES);
        // Get list of possible PathStyles
        this.pathStyles = TravelersCrossroads.WATCHER.pathStyleReg.holders().map(Holder::value).toList();
    }

    public void setPathData() {
        this.crossroadsData = CrossroadsData.instance(server.overworld().getDataStorage());
        TravelersCrossroads.LOGGER.debug("Server Load adding beforeServerLoadNodes {}", this.beforeServerLoadNodes.size());
        this.beforeServerLoadNodes.forEach(pos -> this.crossroadsData.addPathNode(new PathNode(pos)));
        this.randomSource = RandomSource.create(this.server.overworld().getSeed());
    }
}
