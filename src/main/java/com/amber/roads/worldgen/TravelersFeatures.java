package com.amber.roads.worldgen;

import com.amber.roads.TravelersCrossroads;
import com.amber.roads.init.TravelersInit;
import com.amber.roads.init.TravelersRegistries;
import com.amber.roads.util.TravelersTags;
import com.amber.roads.worldgen.custom.OffsetModifier;
import com.amber.roads.worldgen.custom.PathModifiers;
import com.amber.roads.worldgen.custom.StyleModifier;
import com.amber.roads.worldgen.custom.DistanceFilter;
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
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.*;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public class TravelersFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED_BEGINNING_KEY = registerConfiguredKey("configured_road");
    public static final ResourceKey<PlacedFeature> PLACED_BEGINNING_KEY = registerPlacedKey("placed_road");
    public static final ResourceKey<BiomeModifier> BIOME_BEGINNING_KEY = registerBiomeKey("road_biomes");
    public static final ResourceKey<OffsetModifier> ZERO_OFFSET_KEY = registerPathOffsetKey("zero_offset");
    public static final ResourceKey<OffsetModifier> DEFAULT_OFFSET_KEY = registerPathOffsetKey("default_offset");
    public static final ResourceKey<OffsetModifier> VILLAGE_OFFSET_KEY = registerPathOffsetKey("village_offset");
    public static final ResourceKey<OffsetModifier> MANSION_OFFSET_KEY = registerPathOffsetKey("mansion_offset");
    public static final ResourceKey<StyleModifier> DEFAULT_STYLE_KEY = registerPathStyleKey("default_style");
    public static final ResourceKey<StyleModifier> STONE_BRICKS_STYLE_KEY = registerPathStyleKey("stone_bricks_style");
    public static final ResourceKey<StyleModifier> DESERT_STYLE_KEY = registerPathStyleKey("desert_style");
    public static final ResourceKey<StyleModifier> SPARSE_GRAVEL_STYLE_KEY = registerPathStyleKey("sparse_gravel_style");
    public static final ResourceKey<StyleModifier> RUSTIC_STYLE_KEY = registerPathStyleKey("rustic_style");

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
                        RarityFilter.onAverageOnceEvery(47),
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
                ZERO_OFFSET_KEY,
                new PathModifiers.DistanceModifier(structures.getOrThrow(TravelersTags.Structures.ZERO_OFFSET_STRUCTURES), 1)
        );
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

    public static void pathBiomeStylesBootstrap(BootstrapContext<StyleModifier> pathStylesContext) {
        var biomes = pathStylesContext.lookup(Registries.BIOME);

        pathStylesContext.register(
                DEFAULT_STYLE_KEY,
                new PathModifiers.PercentStyleModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD), BlockStateProvider.simple(Blocks.DIRT_PATH),
                        List.of(Blocks.GRAVEL.defaultBlockState(), Blocks.COARSE_DIRT.defaultBlockState())
                )
        );

        pathStylesContext.register(
                STONE_BRICKS_STYLE_KEY,
                new PathModifiers.PercentStyleModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD), BlockStateProvider.simple(Blocks.STONE_BRICKS),
                        List.of(
                                Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
                                Blocks.STONE_BRICK_SLAB.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState()
                        )
                )
        );

        pathStylesContext.register(
                RUSTIC_STYLE_KEY,
                new PathModifiers.PercentStyleModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD), BlockStateProvider.simple(Blocks.DIRT_PATH),
                        List.of(Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_STAIRS.defaultBlockState(), Blocks.COARSE_DIRT.defaultBlockState())
                )
        );

        pathStylesContext.register(
                DESERT_STYLE_KEY,
                new PathModifiers.PercentStyleModifier(
                        biomes.getOrThrow(Tags.Biomes.IS_DESERT), BlockStateProvider.simple(Blocks.SANDSTONE),
                        List.of(Blocks.GRAVEL.defaultBlockState(), Blocks.SANDSTONE_SLAB.defaultBlockState())
                )
        );

        pathStylesContext.register(
                SPARSE_GRAVEL_STYLE_KEY,
                new PathModifiers.SparseStyleModifier(
                        biomes.getOrThrow(Tags.Biomes.IS_TEMPERATE), BlockStateProvider.simple(Blocks.GRAVEL),
                        BlockStateProvider.simple(Blocks.COARSE_DIRT)
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


