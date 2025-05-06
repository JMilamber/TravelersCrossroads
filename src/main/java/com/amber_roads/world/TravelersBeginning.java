package com.amber_roads.world;

import com.mojang.serialization.Codec;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class TravelersBeginning extends Feature<TravelersConfiguration> {

    public TravelersBeginning(Codec<TravelersConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext context) {
        return false;
    }
}
