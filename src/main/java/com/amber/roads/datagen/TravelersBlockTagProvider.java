package com.amber.roads.datagen;

import com.amber.roads.init.TravelersInit;
import com.amber.roads.util.TravelersTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class TravelersBlockTagProvider extends BlockTagsProvider {

    public TravelersBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(TravelersTags.Blocks.PATH_ABOVE)
                .add(TravelersInit.CAIRN.get())
                .addTag(BlockTags.REPLACEABLE)
                .addTag(BlockTags.REPLACEABLE_BY_TREES)
                .addTag(BlockTags.LEAVES)
                .addTag(BlockTags.LOGS)
                .addTag(BlockTags.FLOWERS)
                .addTag(BlockTags.SNOW);

        this.tag(TravelersTags.Blocks.PATH_BELOW)
                .add(Blocks.GRAVEL)
                .addTag(BlockTags.DIRT)
                .addTag(BlockTags.STONE_ORE_REPLACEABLES)
                .addTag(BlockTags.SAND)
                .add(
                        Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW, TravelersInit.CAIRN.get()
                )
                .addTag(BlockTags.BADLANDS_TERRACOTTA);

    }
}
