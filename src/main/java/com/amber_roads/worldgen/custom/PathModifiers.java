package com.amber_roads.worldgen.custom;

import com.amber_roads.init.TravelersInit;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;
import java.util.function.Predicate;

public class PathModifiers {

    private PathModifiers() {}

    public record DistanceModifier(HolderSet<Structure> structures, int offset) implements OffsetModifier {

        @Override
        public boolean checkStructure(Holder<Structure> checkStruct) {
            Predicate<Holder<Structure>> predicate = structures::contains;
            return predicate.test(checkStruct);
        }

        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public MapCodec<? extends OffsetModifier> codec() {
            return TravelersInit.DISTANCE_OFFSET_MODIFIER_TYPE.get();
        }
    }

    public record PercentStyleModifier (
            HolderSet<Biome> biomes,
            List<BlockState> mainPathBlocks, List<BlockState> subPathBlocks,
            List<BlockState> textureBlocks) implements StyleModifier {

        public boolean checkBiome(Holder<Biome> checkBiome) {
            Predicate<Holder<Biome>> predicate = biomes::contains;
            return predicate.test(checkBiome);
        }

        public BlockState getPathBlock(BlockState currentState, RandomSource randomSource) {
            int next = randomSource.nextInt(100);
            if (next >= 95) {
                return currentState;
            } else if (next >= 40) {
                return mainPathBlocks.get(randomSource.nextInt(textureBlocks.size()));
            } else if (next >= 15) {
                return subPathBlocks.get(randomSource.nextInt(textureBlocks.size()));
            } else {
               return textureBlocks.get(randomSource.nextInt(textureBlocks.size()));
            }
        }

        @Override
        public MapCodec<? extends StyleModifier> codec() {
            return TravelersInit.PERCENT_STYLE_MODIFIER_TYPE.get();
        }
    }
}
