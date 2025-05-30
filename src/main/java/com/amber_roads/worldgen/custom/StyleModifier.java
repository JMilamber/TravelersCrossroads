package com.amber_roads.worldgen.custom;

import com.amber_roads.init.TravelersRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

public interface StyleModifier {
    /**
     * Codec for (de)serializing Style modifiers inline.
     * Mods can use this for data generation.
     */
    Codec<StyleModifier> DIRECT_CODEC = TravelersRegistries.STYLE_MODIFIER_SERIALIZERS.byNameCodec()
            .dispatch(StyleModifier::codec, Function.identity());

    /**
     * Codec for referring to Style  modifiers by id in other datapack registry files.
     * Can only be used with {@link RegistryOps}.
     */
    Codec<Holder<StyleModifier>> REFERENCE_CODEC = RegistryFileCodec.create(TravelersRegistries.Keys.STYLE_MODIFIERS, DIRECT_CODEC);

    /**
     * Codec for referring to Style  modifiers by id, list of id, or tags.
     * Can only be used with {@link RegistryOps}.
     */
    Codec<HolderSet<StyleModifier>> LIST_CODEC = RegistryCodecs.homogeneousList(TravelersRegistries.Keys.STYLE_MODIFIERS, DIRECT_CODEC);

    boolean checkBiome(Holder<Biome> checkBiome);

    BlockState getPathBlock(BlockState currentState, RandomSource randomSource);

    MapCodec<? extends StyleModifier> codec();
}

