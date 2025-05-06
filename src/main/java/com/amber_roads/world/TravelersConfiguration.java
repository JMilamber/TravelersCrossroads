package com.amber_roads.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class TravelersConfiguration implements FeatureConfiguration {
    public static final Codec<TravelersConfiguration> CODEC = RecordCodecBuilder.create(
        p_68139_ -> p_68139_.group(
                    Codec.INT.fieldOf("least_distance").orElse(10).forGetter(p_161205_ -> p_161205_.leastDistance),
                    Codec.INT.fieldOf("least_struct_distance").orElse(5).forGetter(p_161203_ -> p_161203_.leastStructDistance),
                    RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("valid_biomes").forGetter(p_204854_ -> p_204854_.validBiomes)
                )
                .apply(p_68139_, TravelersConfiguration::new)
    );

    public final int leastDistance;
    public final int leastStructDistance;
    public final HolderSet<Biome> validBiomes;

    public TravelersConfiguration(int leastDistance, int leastStructDistance, HolderSet<Biome> validBiomes) {
        this.leastDistance = leastDistance;
        this.leastStructDistance = leastStructDistance;
        this.validBiomes = validBiomes;
    }

}
