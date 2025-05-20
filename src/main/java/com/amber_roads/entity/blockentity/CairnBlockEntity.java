package com.amber_roads.entity.blockentity;

import com.amber_roads.TravelersCrossroads;
import com.amber_roads.init.TravelersInit;
import com.amber_roads.util.TravelersDirection;
import com.amber_roads.util.TravelersPath;
import com.amber_roads.worldgen.TravelersBeginning;
import com.amber_roads.worldgen.TravelersWatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

import static com.amber_roads.util.TravelersUtil.chunkDistanceTo;
import static com.amber_roads.util.TravelersUtil.chunkMatch;

public class CairnBlockEntity extends BlockEntity {

    private List<TravelersPath> connections;
    private ChunkPos chunkPos;
    private List<TravelersPath> paths;
    private boolean connectionsFinished;
    private List<TravelersDirection> connectionDirections;
    private boolean pathsFinished;
    private int tickCount;
    private boolean connectionsCreated;


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
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        for (int i = 0; i < tag.getInt("PathsLen"); i++) {
            this.paths.add(new TravelersPath(tag, i));
        }
        this.pathsFinished = tag.getBoolean("PathsFinished");

        for (int i = 50; i < tag.getInt("ConnsLen") + 50; i++) {
            this.connections.add(new TravelersPath(tag, i));
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
        BlockPos centerPos = this.chunkPos.getMiddleBlockPosition(this.getBlockPos().getY());
        TravelersBeginning.placeCenter(level, random, centerPos);
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
            this.connections.add(new TravelersPath(this.chunkPos, nextPos, random, startDirection));
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
        this.paths.add(new TravelersPath(closest, structPos, randomSource, TravelersDirection.directionFromChunks(closest, structPos)));
        this.pathsFinished = false;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        this.tickCount++;
        if (level.isClientSide()) {
            return;
        }
        if (tickCount > 5 && !this.connectionsCreated) {
            addConnections((ServerLevel) level, level.random);
            List<BlockPos> positions = new ArrayList<>();
            for (TravelersPath path: this.connections) {
                positions.add(path.getEnd().getMiddleBlockPosition(pos.getY()));
            }
            List<ChunkPos> structures = TravelersWatcher.getNearbyStructures(pos, positions);
            for (ChunkPos structPos: structures) {
                this.addStructure(structPos, level.getRandom());
            }
            this.connectionsCreated = true;
        }
        if (tickCount % 10 == 0) {
            if (!this.connectionsFinished) {
                // TravelersCrossroads.LOGGER.debug("Finishing Connections");
                int count = 0;
                for (TravelersPath path: this.connections) {
                    if (path.isInProgress()) {
                        try {
                            path.placeNextChunk((ServerLevel) level);
                        } catch (Exception e) {
                            System.out.println("connection placement failed: " + e);
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
                            System.out.println("path placement failed: " + e);
                        }
                    } else {
                        count ++;
                    }
                }
                this.pathsFinished = count == this.paths.size();
            }
        }
        if (tickCount > 1200) {
            List<BlockPos> positions = new ArrayList<>();
            for (TravelersPath path: this.connections) {
                positions.add(path.getEnd().getMiddleBlockPosition(pos.getY()));
            }
            List<ChunkPos> structures = TravelersWatcher.getNearbyStructures(pos, positions);
            for (ChunkPos structPos: structures) {
                this.addStructure(structPos, level.getRandom());
            }
        }
    }
}
