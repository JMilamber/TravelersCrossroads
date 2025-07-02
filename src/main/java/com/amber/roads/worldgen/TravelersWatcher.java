package com.amber.roads.worldgen;

import com.amber.roads.TravelersCrossroads;
import com.amber.roads.init.TravelersRegistries;
import com.amber.roads.util.CrossroadsData;
import com.amber.roads.util.TravelersDirection;
import com.amber.roads.util.TravelersPath;
import com.amber.roads.util.TravelersTags;
import com.amber.roads.world.PathPos;
import com.amber.roads.world.TravelersCrossroad;
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
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import javax.annotation.Nullable;

import static com.amber.roads.util.TravelersUtil.chunkDistanceTo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = TravelersCrossroads.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class TravelersWatcher {

    public MinecraftServer server;
    public Registry<PathStyle> pathStyles;
    public Registry<OffsetModifier> pathOffsets;
    public int tickCount = 0;
    public CrossroadsData crossroadsData;
    public ArrayList<BlockPos> newCrossroadPositions = new ArrayList<>();
    private boolean pathsFinished = false;
    private ArrayList<ChunkPos> beginnings = new ArrayList<>();

    public TravelersWatcher() {}

    public void addCrossroadToCreate(BlockPos center) {
        this.newCrossroadPositions.add(center);
    }

    public void addCrossroad(BlockPos center) {
        RandomSource random = server.overworld().getRandom();
        Holder<Biome> biome = this.server.overworld().getBiome(center);
        PathPos centerPos = new PathPos(center);

        List<PathStyle> possiblePathStyles = TravelersCrossroads.WATCHER.pathStyles.holders().map(Holder::value)
                .filter(pathBiomeStyleHolder -> pathBiomeStyleHolder.checkBiome(biome))
                .toList();
        // TravelersCrossroads.LOGGER.debug("Biome {} Possible Styles {}", biome.getRegisteredName(), possibleStyles);
        PathStyle pathStyle = possiblePathStyles.get(random.nextInt(possiblePathStyles.size()));

        TravelersCrossroad crossroad = new TravelersCrossroad(centerPos, random);
        List<TravelersPath> connectionPaths = crossroad.addConnections(pathStyle);
        this.crossroadsData.addCrossroad(crossroad);
        for (TravelersPath path : connectionPaths) {
            this.addPath(path);
        }

        List<Pair<ChunkPos, Holder<Structure>>> structures = this.getNearbyStructures(center, crossroad.getConnections());
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

            PathPos structPathPos = new PathPos(struct.getFirst());

            TravelersDirection direction = TravelersDirection.directionFromPos(centerPos, structPathPos, pathStyle.getDistance()).getOpposite();
            Optional<TravelersPath> travelersPath = crossroad.addStructure(
                    direction.nextPos(structPathPos, offset.getOffset()),
                    pathStyle
            );
            travelersPath.ifPresent(this::addPath);
        }
    }

    public void addPath(TravelersPath path) {
        this.crossroadsData.addPath(path);
        this.pathsFinished = false;
    }

    public List<Pair<ChunkPos, Holder<Structure>>> getNearbyStructures(BlockPos center, List<BlockPos> blockPosList) {
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

        for (BlockPos pos: blockPosList) {
            struct = findNearestMapStructure(TravelersTags.Structures.PATH_STRUCTURES, pos);
            if (struct != null) {
                ChunkPos structChunkPos = new ChunkPos(struct.getFirst());
                int distance = chunkDistanceTo(centerChunk, structChunkPos);
                if (distance <= 20 && distance > 1) {
                    structs.add(Pair.of(structChunkPos, struct.getSecond()));
                    TravelersCrossroads.LOGGER.debug("Structure found {} {}", struct.getFirst(), struct.getSecond());
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
                    .findNearestMapStructure(level, holders, pos, 20, false)).orElse(null);
        }
    }

    public int getClosest(BlockPos checkPos) {
        int distance = 1000;
        ChunkPos chunkPos = new ChunkPos(checkPos);
        for (ChunkPos pathPos : this.beginnings) {
            distance = Math.min(distance, chunkDistanceTo(chunkPos, pathPos));
        }
        return this.beginnings.isEmpty() ? 100 : distance;
    }

    public void addDistanceFilterPath(ChunkPos pos) {
        this.beginnings.add(pos);
    }

    @SubscribeEvent
    public static void tick(LevelTickEvent.Post event) {
        if (!event.hasTime() || event.getLevel().isClientSide()) {
          return;
        }
        TravelersCrossroads.WATCHER.buildPaths();
    }

    public void buildPaths () {
        this.tickCount++;
        if (tickCount % 10 != 0) {
            return;
        }

        if (!this.newCrossroadPositions.isEmpty()) {
            this.addCrossroad(this.newCrossroadPositions.removeFirst());
        }
        if (!this.pathsFinished) {
            // TravelersCrossroads.LOGGER.debug("Finishing Connections");
            int count = 0;
            List<TravelersPath> paths = crossroadsData.getPaths();
            for (TravelersPath path: paths) {
                if (path.isInProgress()) {
                    try {
                        path.placeNextSection(this.server.overworld());
                    } catch (Exception e) {
                        System.out.println("path placement failed: ");
                        e.printStackTrace();
                    }
                } else {
                    count++;
                }
            }
            pathsFinished = count == paths.size();
        }
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
        this.pathOffsets = server.registryAccess().registryOrThrow(TravelersRegistries.Keys.OFFSET_MODIFIERS);
        this.pathStyles = server.registryAccess().registryOrThrow(TravelersRegistries.Keys.STYLE_MODIFIERS);
    }

    public void setPathData() {
        this.crossroadsData = CrossroadsData.instance(server.overworld().getDataStorage());
        this.beginnings.addAll(this.crossroadsData.getBeginnings());
    }

    public void saveBeginningsData() {
        for (ChunkPos pos : this.beginnings) {
            this.crossroadsData.addBeginning(pos);
        }
    }
}
