package com.amber.roads.util;

import com.amber.roads.world.TravelersCrossroad;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.ArrayList;

public class CrossroadsData extends SavedData {

    private ArrayList<TravelersPath> paths = new ArrayList<>();
    private ArrayList<TravelersCrossroad> crossroads = new ArrayList<>();
    private ArrayList<ChunkPos> beginnings = new ArrayList<>();
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
            data.paths.add(new TravelersPath(tag, i));
        }
        int cross_length = tag.getInt("cross_len");
        for (int i = 0; i < cross_length; i++) {
            data.crossroads.add(new TravelersCrossroad(tag, i));
        }
        int beginning_length = tag.getInt("begin_len");
        for (int i = 0; i < beginning_length; i++) {
            data.beginnings.add(new ChunkPos(NbtUtils.readBlockPos(tag, "begin" + i).orElseThrow()));
        }
        // Load saved data
        return data;
    }
    
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {

        tag.putInt("len", this.paths.size());
        for (int i = 0; i < this.paths.size(); i++) {
            TravelersPath path = this.paths.get(i);
            path.save(tag, i);
        }

        tag.putInt("cross_len", this.crossroads.size());
        for (int i = 0; i < this.crossroads.size(); i++) {
            TravelersCrossroad crossroad = this.crossroads.get(i);
            crossroad.save(tag, i);
        }

        tag.putInt("begin_len", this.beginnings.size());
        for (int i = 0; i < this.beginnings.size(); i++) {
            tag.put("begin" + i, NbtUtils.writeBlockPos(this.beginnings.get(i).getWorldPosition()));
        }
        return tag;
    }

    public ArrayList<TravelersPath> getPaths() {
        return paths;
    }

    public ArrayList<TravelersCrossroad> getCrossroads() {
        return crossroads;
    }

    public ArrayList<ChunkPos> getBeginnings() {
        return beginnings;
    }

    public boolean addPath(TravelersPath newPath) {
        // Change data in saved data
        for (TravelersPath path : this.paths) {
            if (path.getEnd().x == newPath.getEnd().x && path.getEnd().z == newPath.getEnd().z) {
                return false;
            }
        }
        this.paths.add(newPath);
        // Call set dirty if data changes
        this.setDirty();
        return true;
    }

    public void addCrossroad(TravelersCrossroad crossroad) {
        this.crossroads.add(crossroad);
        this.setDirty();
    }

    public void addBeginning(ChunkPos pos) {
        this.beginnings.add(pos);
        this.setDirty();
    }
}
