package com.amber.roads.worldgen.custom;

import com.amber.roads.init.TravelersInit;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
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
            HolderSet<Biome> biomes, BlockStateProvider mainPathBlock, List<BlockState> textureBlocks
    ) implements StyleModifier {

        public boolean checkBiome(Holder<Biome> checkBiome) {
            Predicate<Holder<Biome>> predicate = biomes::contains;
            return predicate.test(checkBiome);
        }

        public BlockState getPathBlock(BlockState currentState, BlockPos pos, RandomSource randomSource) {
            int next = randomSource.nextInt(100);

            if (next >= 55) {
                return mainPathBlock.getState(randomSource, pos);
            } else if (next >= 35){
               return textureBlocks.get(randomSource.nextInt(textureBlocks.size()));
            } else {
                return currentState;
            }
        }

        @Override
        public MapCodec<? extends StyleModifier> codec() {
            return TravelersInit.PERCENT_STYLE_MODIFIER_TYPE.get();
        }
    }

    public record SparseStyleModifier (
            HolderSet<Biome> biomes, BlockStateProvider mainPathBlock, BlockStateProvider subPathBlock
    ) implements StyleModifier {

        public boolean checkBiome(Holder<Biome> checkBiome) {
            Predicate<Holder<Biome>> predicate = biomes::contains;
            return predicate.test(checkBiome);
        }

        public BlockState getPathBlock(BlockState currentState, BlockPos pos, RandomSource randomSource) {
            int next = randomSource.nextInt(100);

            if (next >= 70) {
                return mainPathBlock.getState(randomSource, pos);
            } else if (next >= 55) {
                return subPathBlock.getState(randomSource, pos);
            } else {
                return currentState;
            }
        }

        @Override
        public MapCodec<? extends StyleModifier> codec() {
            return TravelersInit.SPARSE_STYLE_MODIFIER_TYPE.get();
        }
    }
}
