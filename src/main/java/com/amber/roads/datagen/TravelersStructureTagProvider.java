package com.amber.roads.datagen;

import com.amber.roads.util.TravelersTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class TravelersStructureTagProvider extends StructureTagsProvider {

    public TravelersStructureTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.tag(TravelersTags.Structures.PATH_STRUCTURES)
                .add(BuiltinStructures.PILLAGER_OUTPOST)
                .add(BuiltinStructures.WOODLAND_MANSION)
                .add(BuiltinStructures.JUNGLE_TEMPLE)
                .add(BuiltinStructures.DESERT_PYRAMID)
                .add(BuiltinStructures.IGLOO)
                .add(BuiltinStructures.SWAMP_HUT)
                .add(BuiltinStructures.VILLAGE_PLAINS)
                .add(BuiltinStructures.VILLAGE_DESERT)
                .add(BuiltinStructures.VILLAGE_SAVANNA)
                .add(BuiltinStructures.VILLAGE_SNOWY)
                .add(BuiltinStructures.VILLAGE_TAIGA)
                .add(BuiltinStructures.RUINED_PORTAL_STANDARD)
                .add(BuiltinStructures.RUINED_PORTAL_DESERT)
                .add(BuiltinStructures.RUINED_PORTAL_JUNGLE)
                .add(BuiltinStructures.RUINED_PORTAL_SWAMP)
                .add(BuiltinStructures.RUINED_PORTAL_MOUNTAIN)
                .add(BuiltinStructures.TRAIL_RUINS);


        this.tag(TravelersTags.Structures.ZERO_OFFSET_STRUCTURES)
                .add(BuiltinStructures.RUINED_PORTAL_STANDARD);

        this.tag(TravelersTags.Structures.DEFAULT_OFFSET_STRUCTURES)
                .add(BuiltinStructures.PILLAGER_OUTPOST)
                .add(BuiltinStructures.IGLOO)
                .add(BuiltinStructures.SWAMP_HUT)
                .add(BuiltinStructures.RUINED_PORTAL_DESERT)
                .add(BuiltinStructures.RUINED_PORTAL_JUNGLE)
                .add(BuiltinStructures.RUINED_PORTAL_SWAMP)
                .add(BuiltinStructures.RUINED_PORTAL_MOUNTAIN)
                .add(BuiltinStructures.JUNGLE_TEMPLE);

        this.tag(TravelersTags.Structures.VILLAGE_OFFSET_STRUCTURES)
                .add(BuiltinStructures.VILLAGE_PLAINS)
                .add(BuiltinStructures.VILLAGE_DESERT)
                .add(BuiltinStructures.VILLAGE_SAVANNA)
                .add(BuiltinStructures.VILLAGE_SNOWY)
                .add(BuiltinStructures.VILLAGE_TAIGA)
                .add(BuiltinStructures.DESERT_PYRAMID)
                .add(BuiltinStructures.TRAIL_RUINS);

        this.tag(TravelersTags.Structures.MANSION_OFFSET_STRUCTURES)
                .add(BuiltinStructures.WOODLAND_MANSION);
    }
}
