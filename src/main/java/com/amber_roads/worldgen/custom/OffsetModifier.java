package com.amber_roads.worldgen.custom;

import com.amber_roads.init.TravelersRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.function.Function;

public interface OffsetModifier {
    /**
     * Codec for (de)serializing pathOffsets modifiers inline.
     * Mods can use this for data generation.
     */
    Codec<OffsetModifier> DIRECT_CODEC = TravelersRegistries.OFFSET_MODIFIER_SERIALIZERS.byNameCodec()
            .dispatch(OffsetModifier::codec, Function.identity());

    /**
     * Codec for referring to biome modifiers by id in other datapack registry files.
     * Can only be used with {@link RegistryOps}.
     */
    Codec<Holder<OffsetModifier>> REFERENCE_CODEC = RegistryFileCodec.create(TravelersRegistries.Keys.OFFSET_MODIFIERS, DIRECT_CODEC);

    /**
     * Codec for referring to biome modifiers by id, list of id, or tags.
     * Can only be used with {@link RegistryOps}.
     */
    Codec<HolderSet<OffsetModifier>> LIST_CODEC = RegistryCodecs.homogeneousList(TravelersRegistries.Keys.OFFSET_MODIFIERS, DIRECT_CODEC);

    boolean checkStructure(Holder<Structure> checkStruct);

    int getOffset();

    MapCodec<? extends OffsetModifier> codec();
}
