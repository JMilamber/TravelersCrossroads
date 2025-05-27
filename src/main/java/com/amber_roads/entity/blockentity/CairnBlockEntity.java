package com.amber_roads.entity.blockentity;

import com.amber_roads.TravelersCrossroads;
import com.amber_roads.init.TravelersInit;
import com.amber_roads.util.TravelersDirection;
import com.amber_roads.util.TravelersPath;
import com.amber_roads.worldgen.TravelersFeatures;
import com.amber_roads.worldgen.TravelersWatcher;
import com.amber_roads.worldgen.custom.OffsetModifier;
import com.amber_roads.worldgen.custom.StyleModifier;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.amber_roads.util.TravelersUtil.*;

public class CairnBlockEntity extends BlockEntity {


    private List<TravelersPath> connections;
    private ChunkPos chunkPos;
    private List<TravelersPath> paths;
    private boolean connectionsFinished;
    private List<TravelersDirection> connectionDirections;
    private boolean pathsFinished;
    private int tickCount;
    private boolean connectionsCreated;
    private StyleModifier style;


    public CairnBlockEntity(BlockPos pos, BlockState blockState) {
        super(TravelersInit.CAIRN_BE.get(), pos, blockState);
        this.connections = new ArrayList<>();
        this.chunkPos = new ChunkPos(pos);
        this.paths = new ArrayList<>();
        this.connectionDirections = new ArrayList<>();
        this.connectionsFinished = false;
        this.pathsFinished = false;
        this.tickCount = 0;
        this.connectionsCreated = false;
    }

    public void setStyle(StyleModifier style) {
        this.style = style;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        int i = 0;
        for (TravelersPath path: this.paths) {
            path.save(tag, i);
            i++;
        }
        tag.putInt("PathsLen", this.paths.size());
        tag.putBoolean("PathsFinished", this.pathsFinished);

        i = 50;
        for (TravelersPath conn: this.connections) {
            conn.save(tag, i);
            i++;
        }

        tag.putInt("ConnSLen", this.connections.size());
        tag.putBoolean("ConnsFinished", this.connectionsFinished);

        tag.putBoolean("ConnsCreated", this.connectionsCreated);

        Optional<ResourceKey<StyleModifier>> styleKey = TravelersWatcher.pathStyles.getResourceKey(this.style);

        if (styleKey.isPresent()) {
            tag.putString("Style", styleKey.get().location().getPath());
        } else {
            tag.putString("Style", "");
        }

    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        String styleString = tag.getString("Style");
        if (!styleString.isEmpty()) {
           this.style = TravelersWatcher.pathStyles.get(ResourceLocation.parse(styleString));
        } else {
            this.style = TravelersWatcher.pathStyles.get(TravelersFeatures.DEFAULT_STYLE_KEY);
        }

        for (int i = 0; i < tag.getInt("PathsLen"); i++) {
            this.paths.add(new TravelersPath(tag, i, this.style));
        }
        this.pathsFinished = tag.getBoolean("PathsFinished");

        for (int i = 50; i < tag.getInt("ConnsLen") + 50; i++) {
            this.connections.add(new TravelersPath(tag, i, this.style));
        }
        this.connectionsFinished = tag.getBoolean("ConnsFinished");

        this.connectionsCreated = tag.getBoolean("ConnsCreated");
    }


    public void addConnections(ServerLevel level, RandomSource random) {
        // Add 3 connections if first load.
        TravelersCrossroads.LOGGER.debug("Adding connections");
        TravelersDirection startDirection;
        TravelersDirection nextDirection;
        ChunkPos nextPos = this.chunkPos;
        // add 1-3 connections
        for (int i = 0; i < random.nextInt(2) + 1; i++) {
            do {
                startDirection = TravelersDirection.getRandom(random);
            } while (this.connectionDirections.contains(startDirection));

            nextPos = new ChunkPos(nextPos.x + startDirection.getX(), nextPos.z + startDirection.getZ());
            // Move the end point of the new connection up to 3-6 chunks away
            for (int j = 0; j < random.nextInt(3 + 3); j++) {
                nextDirection = TravelersDirection.getRandomNarrowForDirection(random, startDirection);
                nextPos = new ChunkPos(nextPos.x + nextDirection.getX(), nextPos.z + nextDirection.getZ());
            }
            this.connections.add(new TravelersPath(this.chunkPos, nextPos, random, startDirection, this.style));
            this.connectionDirections.addAll(startDirection.getNeighbors());
            // TravelersCrossroads.LOGGER.debug("Connections {}", this.connectionDirections);
        }
    }

    public void addStructure(ChunkPos structPos, RandomSource randomSource) {
        for (TravelersPath path : this.paths) {
            if (chunkMatch(path.getEnd(), structPos)) {
                return;
            }
        }
        TravelersCrossroads.LOGGER.debug("Adding structure {} to {} distance {}", structPos, this.chunkPos, chunkDistanceTo(structPos, this.chunkPos));
        ChunkPos closest = this.chunkPos;
        int distance = 0;
        for (TravelersPath path: this.connections) {
            distance = chunkDistanceTo(structPos, path.getEnd());
            closest = distance < chunkDistanceTo(closest, structPos) ? path.getEnd(): closest;

        }
        if (distance <= 1) {
            return;
        }
        this.paths.add(
                new TravelersPath(closest, structPos, randomSource,
                        TravelersDirection.directionFromChunks(closest, structPos), this.style
                )
        );
        this.pathsFinished = false;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        this.tickCount++;
        if (level.isClientSide()) {
            return;
        }

        if (this.tickCount > 5 && !this.connectionsCreated) {
            BlockPos centerPos = this.chunkPos.getMiddleBlockPosition(this.getBlockPos().getY());
            Holder<Biome> biome = level.getBiome(centerPos);

            List<StyleModifier> possibleStyles = TravelersWatcher.pathStyles.holders().map(Holder::value)
                    .filter(pathBiomeStyleHolder -> pathBiomeStyleHolder.checkBiome(biome))
                    .toList();
            // TravelersCrossroads.LOGGER.debug("Biome {} Possible Styles {}", biome.getRegisteredName(), possibleStyles);

            this.style = possibleStyles.get(level.random.nextInt(possibleStyles.size()));
            // TravelersCrossroads.LOGGER.debug("Style {} ", this.style);
            addConnections((ServerLevel) level, level.random);
            this.addStructures(pos);
            this.connectionsCreated = true;
        }

        if (this.tickCount % 10 == 0) {
            if (!this.connectionsFinished) {
                // TravelersCrossroads.LOGGER.debug("Finishing Connections");
                int count = 0;
                for (TravelersPath path: this.connections) {
                    if (path.isInProgress()) {
                        try {
                            path.placeNextChunk((ServerLevel) level);
                        } catch (Exception e) {
                            System.out.println("connection placement failed: " + e.getMessage());
                        }

                    } else {
                        count++;
                    }
                }
                this.connectionsFinished = count == this.connections.size();
            } else if (!this.pathsFinished) {
                // TravelersCrossroads.LOGGER.debug("Finishing paths");
                int count = 0;
                for (TravelersPath path: this.paths) {
                    if (path.isInProgress()) {
                        try {
                            path.placeNextChunk((ServerLevel) level);
                        } catch (Exception e) {
                            System.out.println("path placement failed: " + e.getMessage());
                        }
                    } else {
                        count ++;
                    }
                }
                this.pathsFinished = count == this.paths.size();
            }
        }
        if (this.tickCount % 1200 == 0) {
            this.addStructures(pos);
        }
    }


    public void addStructures(BlockPos pos) {
        List<OffsetModifier> pathOffsets = TravelersWatcher.pathOffsets.holders().map(Holder::value).toList();
        List<BlockPos> positions = new ArrayList<>();
        for (TravelersPath path: this.connections) {
            positions.add(path.getEnd().getMiddleBlockPosition(pos.getY()));
        }
        List<Pair<ChunkPos, Holder<Structure>>> structures = TravelersWatcher.getNearbyStructures(pos, positions);
        for (Pair<ChunkPos, Holder<Structure>> struct: structures) {
            OffsetModifier offset = null;
            for (OffsetModifier offsetCheck: pathOffsets) {
                if (offsetCheck.checkStructure(struct.getSecond())) {
                    offset = offsetCheck;
                }
            }
            if (offset == null) {
                offset = TravelersWatcher.pathOffsets.get(TravelersFeatures.DEFAULT_OFFSET_KEY);
            }

            TravelersCrossroads.LOGGER.debug("Found structure {} at {} with offset {}", struct.getSecond().getRegisteredName(), struct.getFirst(), offset.getOffset());

            TravelersDirection direction = TravelersDirection.directionFromChunks(this.chunkPos, struct.getFirst()).getOpposite();
            this.addStructure(offsetChunk(struct.getFirst(), offset.getOffset() * direction.getX(), offset.getOffset() * direction.getZ()), level.getRandom());
        }
    }
}
