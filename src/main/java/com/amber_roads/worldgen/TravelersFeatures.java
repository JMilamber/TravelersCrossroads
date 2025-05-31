package com.amber_roads.worldgen;

import com.amber_roads.TravelersCrossroads;
import com.amber_roads.init.TravelersInit;
import com.amber_roads.init.TravelersRegistries;
import com.amber_roads.util.TravelersTags;
import com.amber_roads.worldgen.custom.OffsetModifier;
import com.amber_roads.worldgen.custom.PathModifiers;
import com.amber_roads.worldgen.custom.StyleModifier;
import com.amber_roads.worldgen.custom.DistanceFilter;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public class TravelersFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED_BEGINNING_KEY = registerConfiguredKey("configured_road");
    public static final ResourceKey<PlacedFeature> PLACED_BEGINNING_KEY = registerPlacedKey("placed_road");
    public static final ResourceKey<BiomeModifier> BIOME_BEGINNING_KEY = registerBiomeKey("road_biomes");
    public static final ResourceKey<OffsetModifier> DEFAULT_OFFSET_KEY = registerPathOffsetKey("default_offset");
    public static final ResourceKey<OffsetModifier> VILLAGE_OFFSET_KEY = registerPathOffsetKey("village_offset");
    public static final ResourceKey<OffsetModifier> MANSION_OFFSET_KEY = registerPathOffsetKey("mansion_offset");
    public static final ResourceKey<StyleModifier> DEFAULT_STYLE_KEY = registerPathStyleKey("default_style");

    public static void configuredBootstrap(BootstrapContext<ConfiguredFeature<?, ?>> configuredContext) {
        configuredRegister(
                configuredContext, CONFIGURED_BEGINNING_KEY, TravelersInit.TRAVELERS_BEGINNING.get(),
                FeatureConfiguration.NONE
        );
    }

    public static void placedBootstrap(BootstrapContext<PlacedFeature> placedContext) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = placedContext.lookup(Registries.CONFIGURED_FEATURE);

        placedRegister(
                placedContext, PLACED_BEGINNING_KEY, configuredFeatures.getOrThrow(CONFIGURED_BEGINNING_KEY),
                List.of(
                        RarityFilter.onAverageOnceEvery(67),
                        DistanceFilter.minimumEvery(25),
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP,
                        EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.noFluid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 5),
                        EnvironmentScanPlacement.scanningFor(Direction.EAST, BlockPredicate.noFluid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 5),
                        EnvironmentScanPlacement.scanningFor(Direction.WEST, BlockPredicate.noFluid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 5),
                        EnvironmentScanPlacement.scanningFor(Direction.NORTH, BlockPredicate.noFluid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 5),
                        EnvironmentScanPlacement.scanningFor(Direction.SOUTH, BlockPredicate.noFluid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 5),
                        BiomeFilter.biome()
                )
        );
    }

    public static void biomeBootstrap(BootstrapContext<BiomeModifier> biomeContext) {
        var placedFeatures = biomeContext.lookup(Registries.PLACED_FEATURE);
        var biomes = biomeContext.lookup(Registries.BIOME);

        biomeContext.register(BIOME_BEGINNING_KEY, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(PLACED_BEGINNING_KEY)),
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION
        ));

    }

    public static void pathOffsetBootstrap(BootstrapContext<OffsetModifier> pathOffsetContext) {
        var structures = pathOffsetContext.lookup(Registries.STRUCTURE);

        pathOffsetContext.register(
                DEFAULT_OFFSET_KEY,
                new PathModifiers.DistanceModifier(structures.getOrThrow(TravelersTags.Structures.DEFAULT_OFFSET_STRUCTURES), 1)
        );
        pathOffsetContext.register(
                VILLAGE_OFFSET_KEY,
                new PathModifiers.DistanceModifier(structures.getOrThrow(TravelersTags.Structures.VILLAGE_OFFSET_STRUCTURES), 2)
        );
        pathOffsetContext.register(
                MANSION_OFFSET_KEY,
                new PathModifiers.DistanceModifier(structures.getOrThrow(TravelersTags.Structures.MANSION_OFFSET_STRUCTURES), 2)
        );
    }

    public static void pathBiomeStylesBootstrap(BootstrapContext<StyleModifier> pathBiomeBlocksContext) {
        var biomes = pathBiomeBlocksContext.lookup(Registries.BIOME);

        pathBiomeBlocksContext.register(
                DEFAULT_STYLE_KEY,
                new PathModifiers.PercentStyleModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD), List.of(Blocks.GRAVEL.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState()),
                        List.of(Blocks.DIRT_PATH.defaultBlockState()), List.of(Blocks.COARSE_DIRT.defaultBlockState())
                )
        );

    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerConfiguredKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, TravelersCrossroads.travelersLocation(name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void configuredRegister(BootstrapContext<ConfiguredFeature<?, ?>> context,
                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }

    private static ResourceKey<PlacedFeature> registerPlacedKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, TravelersCrossroads.travelersLocation(name));
    }

    private static void placedRegister(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }

    private static ResourceKey<BiomeModifier> registerBiomeKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, TravelersCrossroads.travelersLocation(name));
    }

    private static ResourceKey<OffsetModifier> registerPathOffsetKey(String name) {
        return ResourceKey.create(TravelersRegistries.Keys.OFFSET_MODIFIERS, TravelersCrossroads.travelersLocation(name));
    }

    private static ResourceKey<StyleModifier> registerPathStyleKey(String name) {
        return ResourceKey.create(TravelersRegistries.Keys.STYLE_MODIFIERS, TravelersCrossroads.travelersLocation(name));
    }
}


