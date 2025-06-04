package com.amber.roads.worldgen.custom;

import com.amber.roads.init.TravelersInit;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
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

        public void setPathBlock(ServerLevel level, BlockPos originPos, int xOffset, int zOffset) {
            RandomSource randomSource = level.getRandom();
            int next = randomSource.nextInt(100);
            BlockPos currentPos = originPos.offset(xOffset, 0, zOffset);
            BlockState currentState = level.getBlockState(currentPos);
            BlockState setState;

            if (next >= 55) {
                setState = mainPathBlock.getState(randomSource, currentPos);
            } else if (next >= 35){
                setState = textureBlocks.get(randomSource.nextInt(textureBlocks.size()));
            } else {
                setState = currentState;
            }

            level.setBlock(currentPos, setState, 2);
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

        public void setPathBlock(ServerLevel level, BlockPos originPos, int xOffset, int zOffset) {
            RandomSource randomSource = level.getRandom();
            int next = randomSource.nextInt(100);
            BlockPos currentPos = originPos.offset(xOffset, 0, zOffset);
            BlockState currentState = level.getBlockState(currentPos);
            BlockState setState;
            if (next >= 75) {
                setState =  mainPathBlock.getState(randomSource, currentPos);
            } else if (next >= 60) {
                setState =  subPathBlock.getState(randomSource, currentPos);
            } else {
                setState =  currentState;
            }

            level.setBlock(currentPos, setState, 2);
        }

        @Override
        public MapCodec<? extends StyleModifier> codec() {
            return TravelersInit.SPARSE_STYLE_MODIFIER_TYPE.get();
        }
    }
}
