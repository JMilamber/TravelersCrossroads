package com.amber_roads.world;

import com.amber_roads.entity.blockentity.CairnBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import static com.amber_roads.util.TravelersUtil.chunkDistanceTo;

import java.util.ArrayList;
import java.util.List;

public class TravelersWatcher {

    public static ArrayList<ChunkPos> structures = new ArrayList<>();
    public static ArrayList<ChunkPos> removeList = new ArrayList<>();
    public static ArrayList<BlockPos> cairns = new ArrayList<>();
    public static Level level;

    public TravelersWatcher() {}

    private static void addStructure(ChunkPos pos) {
        if (!structures.contains(pos)) {
            structures.add(pos);
            for (BlockPos cairnPos : cairns) {
                if (chunkDistanceTo(new ChunkPos(cairnPos), pos) < 20) {
                    CairnBlockEntity cairn = (CairnBlockEntity) level.getBlockEntity(cairnPos);
                    cairn.addStructure(pos, level.random);
                }
            }
        }
    }

    private static void removeStructure(ChunkPos pos) {
        removeList.add(pos);
    }

    public static List<ChunkPos> getNearbyStructures(ChunkPos pos) {
        return structures.stream().filter(chunkPos -> chunkDistanceTo(chunkPos, pos) < 20).toList();
    }

    public void addCairn(BlockPos pos) {
        cairns.add(pos);
    }

    public void removeCairn(BlockPos pos) {
        cairns.removeIf(listPos -> pos.getX() == listPos.getX() && pos.getZ() == listPos.getZ());
    }


    @SubscribeEvent
    public static void chunkLoaded(ChunkEvent.Load event) {
        if (!event.getLevel().isClientSide()) {
            if (event.getChunk().hasAnyStructureReferences()) {
                addStructure(event.getChunk().getPos());
            }
        }
    }


    @SubscribeEvent
    public static void chunkUnloaded(ChunkEvent.Unload event) {

        if (!event.getLevel().isClientSide()) {
            ChunkPos eventPos = event.getChunk().getPos();
            if (event.getChunk().hasAnyStructureReferences()) {
                removeStructure(eventPos);
            }
            // If chunk is slated for removal and more than 6 chunks away from the unloaded edge, remove from reference list
            for (ChunkPos pos : removeList) {
                if (chunkDistanceTo(pos, eventPos) > 6) {
                    structures.remove(pos);
                    removeList.remove(pos);
                }
            }
        }
    }


    @SubscribeEvent
    public static void levelLoad(LevelEvent.Load event) {

    }

    @SubscribeEvent
    public static void levelLoad(LevelEvent.Unload event) {

    }

}
