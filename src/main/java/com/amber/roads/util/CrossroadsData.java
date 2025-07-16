package com.amber.roads.util;

import com.amber.roads.world.PathNode;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.ArrayList;

public class CrossroadsData extends SavedData {

    private ArrayList<TravelersPath> unfinishedPaths = new ArrayList<>();
    private ArrayList<PathNode> pathNodes = new ArrayList<>();
    // Create new instance of saved data
    public static CrossroadsData create() {
        return new CrossroadsData();
    }

    public static CrossroadsData instance(DimensionDataStorage storage) {
        return storage.computeIfAbsent(new Factory<>(CrossroadsData::create, CrossroadsData::load), "path_data");
    }

    // Load existing instance of saved data
    public static CrossroadsData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        CrossroadsData data = CrossroadsData.create();
        int length = tag.getInt("len");
        for (int i = 0; i < length; i++) {
            data.unfinishedPaths.add(new TravelersPath(tag, i));
        }
        int nodes_len = tag.getInt("nodes_len");
        for (int i = 0; i < nodes_len; i++) {
            data.pathNodes.add(new PathNode(NbtUtils.readBlockPos(tag, "nodes" + i).orElseThrow()));
        }
        // Load saved data
        return data;
    }
    
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {

        tag.putInt("len", this.unfinishedPaths.size());
        for (int i = 0; i < this.unfinishedPaths.size(); i++) {
            TravelersPath path = this.unfinishedPaths.get(i);
            path.save(tag, i);
        }

        tag.putInt("nodes_len", this.pathNodes.size());
        for (int i = 0; i < this.pathNodes.size(); i++) {
            tag.put("nodes" + i, NbtUtils.writeBlockPos(this.pathNodes.get(i).asBlockPos()));
        }
        return tag;
    }

    public ArrayList<TravelersPath> getUnfinishedPaths() {
        return unfinishedPaths;
    }


    public ArrayList<PathNode> getPathNodes() {
        return pathNodes;
    }

    public boolean addPath(TravelersPath newPath) {
        if (this.unfinishedPaths.contains(newPath)) {
            return false;
        }
        // Change data in saved data
        this.unfinishedPaths.add(newPath);
        // Call set dirty if data changes
        this.setDirty();
        return true;
    }

    public void addPathNode(PathNode node) {
        if (this.pathNodes.contains(node)) {
            return;
        }

        this.pathNodes.add(node);
        // Call set dirty if data changes
        this.setDirty();
    }

    public void removePath(TravelersPath path) {
        this.unfinishedPaths.remove(path);
        // Call set dirty if data changes
        this.setDirty();
    }
}
