package com.amber.roads.worldgen;

import com.amber.roads.TravelersCrossroads;
import com.amber.roads.init.TravelersInit;
import com.amber.roads.init.TravelersRegistries;
import com.amber.roads.util.PathSize;
import com.amber.roads.util.TravelersTags;
import com.amber.roads.worldgen.custom.*;
import com.amber.roads.worldgen.custom.pathstyle.PathStyle;
import com.amber.roads.worldgen.custom.pathstyle.PercentStyle;
import com.amber.roads.worldgen.custom.pathstyle.SparseStyle;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
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

    public static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED_PATH_START = registerConfiguredKey("configured_path_start");
    public static final ResourceKey<PlacedFeature> PLACED_PATH_START = registerPlacedKey("placed_path_start");
    public static final ResourceKey<BiomeModifier> PATH_START_BIOMES = registerBiomeKey("path_start_biomes");
    public static final ResourceKey<OffsetModifier> ZERO_OFFSET_KEY = registerPathOffsetKey("zero_offset");
    public static final ResourceKey<OffsetModifier> DEFAULT_OFFSET_KEY = registerPathOffsetKey("default_offset");
    public static final ResourceKey<OffsetModifier> VILLAGE_OFFSET_KEY = registerPathOffsetKey("village_offset");
    public static final ResourceKey<OffsetModifier> MANSION_OFFSET_KEY = registerPathOffsetKey("mansion_offset");

    public static final ResourceKey<PathStyle> DEFAULT_STYLE_KEY = registerPathStyleKey("default_style");
    public static final ResourceKey<PathStyle> STONE_BRICKS_STYLE_KEY = registerPathStyleKey("stone_bricks_style");
    public static final ResourceKey<PathStyle> DESERT_STYLE_KEY = registerPathStyleKey("desert_style");
    public static final ResourceKey<PathStyle> SPARSE_GRAVEL_STYLE_KEY = registerPathStyleKey("sparse_gravel_style");
    public static final ResourceKey<PathStyle> RUSTIC_STYLE_KEY = registerPathStyleKey("rustic_style");

    public static void configuredBootstrap(BootstrapContext<ConfiguredFeature<?, ?>> configuredContext) {
        configuredRegister(
                configuredContext, CONFIGURED_PATH_START, TravelersInit.TRAVELERS_BEGINNING.get(),
                FeatureConfiguration.NONE
        );
    }

    public static void placedBootstrap(BootstrapContext<PlacedFeature> placedContext) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = placedContext.lookup(Registries.CONFIGURED_FEATURE);

        placedRegister(
                placedContext, PLACED_PATH_START, configuredFeatures.getOrThrow(CONFIGURED_PATH_START),
                List.of(
                        RarityFilter.onAverageOnceEvery(20),
                        DistanceFilter.minimumEvery(16),
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP,
                        BiomeFilter.biome()
                )
        );
    }

    public static void biomeBootstrap(BootstrapContext<BiomeModifier> biomeContext) {
        var placedFeatures = biomeContext.lookup(Registries.PLACED_FEATURE);
        var biomes = biomeContext.lookup(Registries.BIOME);

        biomeContext.register(PATH_START_BIOMES, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(PLACED_PATH_START)),
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION
        ));

    }

    public static void pathOffsetBootstrap(BootstrapContext<OffsetModifier> pathOffsetContext) {
        var structures = pathOffsetContext.lookup(Registries.STRUCTURE);

        pathOffsetContext.register(
                ZERO_OFFSET_KEY,
                new PathModifiers.DistanceModifier(structures.getOrThrow(TravelersTags.Structures.ZERO_OFFSET_STRUCTURES), 0)
        );
        pathOffsetContext.register(
                DEFAULT_OFFSET_KEY,
                new PathModifiers.DistanceModifier(structures.getOrThrow(TravelersTags.Structures.DEFAULT_OFFSET_STRUCTURES), 10)
        );
        pathOffsetContext.register(
                VILLAGE_OFFSET_KEY,
                new PathModifiers.DistanceModifier(structures.getOrThrow(TravelersTags.Structures.VILLAGE_OFFSET_STRUCTURES), 20)
        );
        pathOffsetContext.register(
                MANSION_OFFSET_KEY,
                new PathModifiers.DistanceModifier(structures.getOrThrow(TravelersTags.Structures.MANSION_OFFSET_STRUCTURES), 20)
        );
    }

    public static void pathBiomeStylesBootstrap(BootstrapContext<PathStyle> pathStylesContext) {
        var biomes = pathStylesContext.lookup(Registries.BIOME);

        pathStylesContext.register(
                DEFAULT_STYLE_KEY,
                new PercentStyle(
                        new PathStyle.PathSettings(
                                biomes.getOrThrow(BiomeTags.IS_OVERWORLD), BlockStateProvider.simple(Blocks.DIRT_PATH),
                                PathSize.SMALL.getSerializedName()
                        ),
                        List.of(Blocks.GRAVEL.defaultBlockState(), Blocks.COARSE_DIRT.defaultBlockState()),
                        60, 30, 10
                )
        );

        pathStylesContext.register(
                STONE_BRICKS_STYLE_KEY,
                new PercentStyle(
                        new PathStyle.PathSettings(
                                biomes.getOrThrow(BiomeTags.IS_OVERWORLD), BlockStateProvider.simple(Blocks.STONE_BRICKS),
                                PathSize.MEDIUM.getSerializedName()
                        ),
                        List.of(
                                Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
                                Blocks.STONE_BRICK_SLAB.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState()
                        ),
                        60, 30, 10
                )
        );

        pathStylesContext.register(
                RUSTIC_STYLE_KEY,
                new PercentStyle(
                        new PathStyle.PathSettings(
                                biomes.getOrThrow(BiomeTags.IS_FOREST), BlockStateProvider.simple(Blocks.DIRT_PATH),
                                PathSize.MINI.getSerializedName()
                        ),
                        List.of(Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_STAIRS.defaultBlockState(), Blocks.COARSE_DIRT.defaultBlockState()),
                        60, 30, 10
                )
        );

        pathStylesContext.register(
                DESERT_STYLE_KEY,
                new PercentStyle(
                        new PathStyle.PathSettings(
                                biomes.getOrThrow(Tags.Biomes.IS_DESERT), BlockStateProvider.simple(Blocks.SANDSTONE),
                                PathSize.SMALL.getSerializedName()
                        ),
                        List.of(Blocks.GRAVEL.defaultBlockState(), Blocks.SANDSTONE_SLAB.defaultBlockState()),
                        60, 30, 10
                )
        );

        pathStylesContext.register(
                SPARSE_GRAVEL_STYLE_KEY,
                new SparseStyle(
                        new PathStyle.PathSettings(
                                biomes.getOrThrow(Tags.Biomes.IS_TEMPERATE_OVERWORLD),
                                BlockStateProvider.simple(Blocks.GRAVEL),
                                PathSize.MINI.getSerializedName()
                        ),
                        List.of(Blocks.COARSE_DIRT.defaultBlockState())
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
        return ResourceKey.create(TravelersRegistries.Keys.STRUCTURE_OFFSETS, TravelersCrossroads.travelersLocation(name));
    }

    private static ResourceKey<PathStyle> registerPathStyleKey(String name) {
        return ResourceKey.create(TravelersRegistries.Keys.PATH_STYLES, TravelersCrossroads.travelersLocation(name));
    }
}


