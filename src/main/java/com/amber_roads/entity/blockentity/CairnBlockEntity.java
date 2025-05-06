package com.amber_roads.entity.blockentity;

import com.amber_roads.init.TravelersInit;
import com.amber_roads.util.TravelersDirection;
import com.amber_roads.util.TravelersPath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

import static com.amber_roads.util.TravelersUtil.chunkMatch;

public class CairnBlockEntity extends BlockEntity {

    public ChunkPos[] connections;
    private ChunkPos chunkPos;
    private List<TravelersPath> paths;

    public CairnBlockEntity(BlockPos pos, BlockState blockState) {
        super(TravelersInit.CAIRN_BE.get(), pos, blockState);
        this.connections = new ChunkPos[8];
        this.chunkPos = new ChunkPos(pos);
        this.paths = new ArrayList<>();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        long[] conns = new long[8];

        for (int i = 0; i < this.connections.length; i++) {
            ChunkPos connection = this.connections[i];
            conns[i] = connection.toLong();
        }
        tag.putLongArray("Connections", conns);

        int i = 0;
        for (TravelersPath path: this.paths) {
            path.save(tag, i);
            i++;
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }


    public void addConnections(RandomSource random) {
        // Add 3 connections if first load.
        for (int i = 0; i < random.nextInt(3) + 1; i++) {
            TravelersDirection.getRandom(random);
        }
    }

    public void addStructure(ChunkPos structPos, RandomSource randomSource) {
        for (TravelersPath path :this.paths) {
            if (chunkMatch(path.getEnd(), structPos)) {
                return;
            }
        }
        this.paths.add(new TravelersPath(this.chunkPos, structPos, randomSource));
    }



    @Override
    public void onLoad() {
        super.onLoad();
    }
}
