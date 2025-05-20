package com.amber_roads.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.Structure;

public record TravelersConfiguration(int leastDistance, int leastStructDistance, HolderSet<Structure> avoidStructures) implements FeatureConfiguration {
    public static final Codec<TravelersConfiguration> CODEC = RecordCodecBuilder.create(
            group -> group.group(
                            Codec.INT.fieldOf("least_distance").orElse(10).forGetter(config -> config.leastDistance),
                            Codec.INT.fieldOf("least_struct_distance").orElse(5).forGetter(config -> config.leastStructDistance),
                            RegistryCodecs.homogeneousList(Registries.STRUCTURE).fieldOf("avoid_structures").forGetter(config -> config.avoidStructures)
                    ).apply(group, TravelersConfiguration::new)
    );

}
