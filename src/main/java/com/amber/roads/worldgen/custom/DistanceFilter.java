package com.amber.roads.worldgen.custom;

import com.amber.roads.TravelersConfig;
import com.amber.roads.TravelersCrossroads;
import com.amber.roads.init.TravelersInit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Optional;

import static com.amber.roads.util.TravelersUtil.chunkDistanceTo;

public class DistanceFilter extends PlacementFilter {
    public static final MapCodec<DistanceFilter> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("distance").forGetter(distanceFilter -> distanceFilter.distance)
    ).apply(instance, instance.stable((DistanceFilter::new))));

    public final int distance;

    public DistanceFilter(int distance) {
        this.distance = distance;
    }

    public static DistanceFilter minimumEvery(int distance) {
        return new DistanceFilter(distance);
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        Optional<ChunkPos> closestPath = TravelersCrossroads.WATCHER.getClosest(pos, this.distance);
        if (closestPath.isPresent() || chunkDistanceTo(ChunkPos.ZERO, new ChunkPos(pos)) < TravelersConfig.distanceFromWorldCenter) {
            return false;
        }
        closestPath.ifPresent(pathPos -> TravelersCrossroads.LOGGER.debug("Acceptable cairn pos distance {}", chunkDistanceTo(new ChunkPos(pos), pathPos)));
        TravelersCrossroads.WATCHER.addDistanceFilterNode(new ChunkPos(pos));
        return true;
    }

    @Override
    public PlacementModifierType<?> type() {
        return TravelersInit.DISTANCE_FILTER.get();
    }
}
