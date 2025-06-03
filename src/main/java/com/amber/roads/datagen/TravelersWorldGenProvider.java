package com.amber.roads.datagen;

import com.amber.roads.TravelersCrossroads;
import com.amber.roads.init.TravelersRegistries;
import com.amber.roads.worldgen.TravelersFeatures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TravelersWorldGenProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, TravelersFeatures::configuredBootstrap)
            .add(Registries.PLACED_FEATURE, TravelersFeatures::placedBootstrap)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, TravelersFeatures::biomeBootstrap)
            .add(TravelersRegistries.Keys.OFFSET_MODIFIERS, TravelersFeatures::pathOffsetBootstrap)
            .add(TravelersRegistries.Keys.STYLE_MODIFIERS, TravelersFeatures::pathBiomeStylesBootstrap);

    public TravelersWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of("minecraft", TravelersCrossroads.MOD_ID));
    }
}
