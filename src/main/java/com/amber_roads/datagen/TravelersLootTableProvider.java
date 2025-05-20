package com.amber_roads.datagen;

import com.amber_roads.init.TravelersInit;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.Set;

public class TravelersLootTableProvider extends BlockLootSubProvider {
    protected TravelersLootTableProvider(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        this.add(TravelersInit.CAIRN.get(), this.createSingleItemTable(TravelersInit.PEBBLE.get(), UniformGenerator.between(4, 6)));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return TravelersInit.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
