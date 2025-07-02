package com.amber.roads.worldgen.custom;

import com.amber.roads.init.TravelersInit;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.levelgen.structure.Structure;

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
}
