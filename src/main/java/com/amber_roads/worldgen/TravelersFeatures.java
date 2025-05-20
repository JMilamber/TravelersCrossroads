package com.amber_roads.worldgen;

import com.amber_roads.TravelersCrossroads;
import com.amber_roads.init.TravelersInit;
import com.amber_roads.util.TravelersTags;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public class TravelersFeatures {



    public static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED_BEGINNING_KEY = registerConfiguredKey("road");
    public static final ResourceKey<PlacedFeature> PLACED_BEGINNING_KEY = registerPlacedKey("road");
    public static final ResourceKey<BiomeModifier> BIOME_BEGINNING_KEY = registerBiomeKey("road");


    public static void configuredBootstrap(BootstrapContext<ConfiguredFeature<?, ?>> configuredContext) {
        HolderGetter<Structure> structureHolderGetter = configuredContext.lookup(Registries.STRUCTURE);
        configuredRegister(
                configuredContext, CONFIGURED_BEGINNING_KEY, TravelersInit.TRAVELERS_BEGINNING.get(),
                new TravelersConfiguration(18, 4, structureHolderGetter.getOrThrow(TravelersTags.Structures.PATH_AVOID))
        );
    }

    public static void placedBootstrap(BootstrapContext<PlacedFeature> placedContext) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = placedContext.lookup(Registries.CONFIGURED_FEATURE);

        placedRegister(
                placedContext, PLACED_BEGINNING_KEY,configuredFeatures.getOrThrow(CONFIGURED_BEGINNING_KEY),
                List.of(
                        RarityFilter.onAverageOnceEvery(74),
                        DistanceFilter.minimumEvery(35),
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP,
                        EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.noFluid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 12),
                        EnvironmentScanPlacement.scanningFor(Direction.EAST, BlockPredicate.noFluid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 12),
                        EnvironmentScanPlacement.scanningFor(Direction.WEST, BlockPredicate.noFluid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 12),
                        EnvironmentScanPlacement.scanningFor(Direction.NORTH, BlockPredicate.noFluid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 12),
                        EnvironmentScanPlacement.scanningFor(Direction.SOUTH, BlockPredicate.noFluid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 12),
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

    public static ResourceKey<ConfiguredFeature<?, ?>> registerConfiguredKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(TravelersCrossroads.MODID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void configuredRegister(BootstrapContext<ConfiguredFeature<?, ?>> context,
                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }

    private static ResourceKey<PlacedFeature> registerPlacedKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(TravelersCrossroads.MODID, name));
    }

    private static void placedRegister(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }

    private static ResourceKey<BiomeModifier> registerBiomeKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(TravelersCrossroads.MODID, name));
    }

}


