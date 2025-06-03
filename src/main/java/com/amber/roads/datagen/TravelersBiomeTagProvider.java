package com.amber.roads.datagen;

import com.amber.roads.util.TravelersTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class TravelersBiomeTagProvider extends BiomeTagsProvider {

    public TravelersBiomeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(TravelersTags.Biomes.PATH_AVOID)
                .addTag(Tags.Biomes.IS_AQUATIC)
                .addTag(Tags.Biomes.IS_AQUATIC_ICY);
    }
}
