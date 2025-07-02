package com.amber.roads.worldgen.custom.pathstyle;

import com.amber.roads.TravelersCrossroads;
import com.amber.roads.init.TravelersRegistries;
import com.amber.roads.world.PathPos;
import com.amber.roads.util.PathSize;
import com.amber.roads.util.TravelersDirection;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.amber.roads.util.TravelersTags.Blocks.PATH_ABOVE;
import static com.amber.roads.util.TravelersTags.Blocks.PATH_BELOW;

import static net.minecraft.util.Mth.*;

public abstract class PathStyle {
    /**
     * Codec for (de)serializing Style modifiers inline.
     * Mods can use this for data generation.
     */
    public static final Codec<PathStyle> DIRECT_CODEC = TravelersRegistries.STYLE_MODIFIER_TYPE.byNameCodec().dispatch(PathStyle::type, StyleModifierType::codec);

    /**
     * Codec for referring to Style  modifiers by id in other datapack registry files.
     * Can only be used with {@link RegistryOps}.
     */
    public static final Codec<Holder<PathStyle>> CODEC = RegistryFileCodec.create(TravelersRegistries.Keys.STYLE_MODIFIERS, DIRECT_CODEC);


    protected final PathStyle.PathSettings settings;
    protected final PathSize pathSize;


    public static <S extends PathStyle> RecordCodecBuilder<S, PathSettings> settingsCodec(RecordCodecBuilder.Instance<S> instance) {
        return PathSettings.CODEC.forGetter(style -> style.settings); // FORGE: Patch codec to ignore field redirect coremods.
    }
    protected PathStyle(PathSettings settings) {
        this.settings = settings;
        this.pathSize = PathSize.valueOf(settings.pathSize);
    }


    public boolean checkBiome(Holder<Biome> checkBiome) {
        Predicate<Holder<Biome>> predicate = settings.biomes::contains;
        return predicate.test(checkBiome);
    }

    /**
     * implementations should use level.setBlock() to place the decided upon path block at the location.
     * @param level ServerLevel, used for setting the block picked.
     * @param originPos the center of the current path section path position
     */
    public abstract void setPathBlock(ServerLevel level, BlockPos originPos);

    public boolean placeSection(ServerLevel level, PathPos pos1, TravelersDirection direction) {
        int distance = this.getDistance();
        PathPos pos2 = direction.nextPos(pos1, distance);
        if (!level.hasChunk(pos1.getChunkX(), pos1.getChunkZ()) || !level.hasChunk(pos2.getChunkX(), pos2.getChunkZ())) {
            return false;
        }
        // TravelersCrossroads.LOGGER.debug("place section {} {}", pos1, direction);
        int size = this.pathSize.getSize();
        int innerSize = size - 2;

        List<Pair<Integer, Integer>> pathBlocks = new ArrayList<>();
        List<Pair<Integer, Integer>> extraBlocks = new ArrayList<>();

        int lowerX;
        int lowerZ;
        int upperX;
        int upperZ;

        if (direction.isN()) {
            lowerZ = pos2.z;
            upperZ = pos1.z;
        } else {
            lowerZ = pos1.z;
            upperZ = pos2.z;
        }

        if (direction.isW()) {
            lowerX = pos2.x;
            upperX = pos1.x;
        } else {
            lowerX = pos1.x;
           upperX = pos2.x;
        }

        int startX = lowerX - innerSize;
        int startZ = lowerZ - innerSize;
        int endX = upperX + innerSize - 1;
        int endZ = upperZ + innerSize - 1;

        int xdistance;
        int zdistance;
        boolean sameX = pos1.x == pos2.x;
        boolean sameZ = pos1.z == pos2.z;

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                xdistance = abs(x-lowerX) + innerSize;
                zdistance = abs(z-lowerZ) + innerSize;

                if (sameX && 0 < xdistance && xdistance < innerSize) {
                    pathBlocks.add(Pair.of(x, z));
                } else if (sameZ && 0 < zdistance && zdistance < innerSize) {
                    pathBlocks.add(Pair.of(x, z));
                } else if (findDistance(pos1.x, pos1.z, pos2.x, pos2.z, x, z) < innerSize) {
                    pathBlocks.add(Pair.of(x, z));
                } else {
                    extraBlocks.add(Pair.of(x, z));
                }
            }
        }

        BlockPos sectionCenter = direction.nextSectionCenter(pos1, distance);
        int generationY = level.getChunkSource().getGenerator().getFirstOccupiedHeight(
                sectionCenter.getX(), sectionCenter.getZ(), Heightmap.Types.WORLD_SURFACE,
                level, level.getChunkSource().randomState());


        BlockPos placePos;
        Optional<BlockPos> newPlacePos;
        for (Pair<Integer, Integer> position : pathBlocks.stream().distinct().toList()) {
            placePos = new BlockPos(position.getFirst(), generationY, position.getSecond());
            newPlacePos = findY(level, placePos);
            newPlacePos.ifPresent(blockPos -> this.setPathBlock(level, blockPos));
        }

        placeExtraBlocks(level, pos1, direction, extraBlocks);
        return true;
    }

    public float findDistance(float x1, float y1, float x2, float y2, float x0, float y0) {
        float top = abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2*y1 - y2*x1);
        float bottom = sqrt((float) (Math.pow(y2-y1, 2) + Math.pow(x2-x1, 2)));

        return top/bottom;
    }

    public abstract void placeExtraBlocks(ServerLevel level, PathPos pos1, TravelersDirection direction, List<Pair<Integer, Integer>> extraBlockPositions);

    public static Optional<BlockPos> findY(ServerLevel level, BlockPos origin) {

        while (!level.getBlockState(origin.above()).is(PATH_ABOVE) || !level.getBlockState(origin).is(PATH_BELOW)) {
            /**TravelersCrossroads.LOGGER.debug(
                    "Blockstates: {} {}",
                    level.getBlockState(origin.above()),
                    level.getBlockState(origin)
            );*/
            if (level.getBlockState(origin.above()).getFluidState().isSource() || level.getBlockState(origin).getFluidState().isSource()){
                return Optional.empty();
            } else if (level.getBlockState(origin).is(PATH_ABOVE)) {
                origin = origin.below();
            } else if (level.getBlockState(origin.above()).is(PATH_BELOW)) {
                origin = origin.above();
            } else {
                break;
            }
        }
        return Optional.of(origin);
    }

    public int getDistance() {
        return pathSize.getDistance();
    }

    abstract  MapCodec<? extends PathStyle> codec();
    public abstract StyleModifierType<?> type();

    public record PathSettings(
           HolderSet<Biome> biomes, BlockStateProvider mainPathBlock, String pathSize
    ) {
        public static final MapCodec<PathSettings> CODEC = RecordCodecBuilder.mapCodec(
            p_351995_ -> p_351995_.group(
                        RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(PathSettings::biomes),
                            BlockStateProvider.CODEC.fieldOf("main_path_block").forGetter(PathSettings::mainPathBlock),
                            Codec.STRING.fieldOf("pathSize").forGetter(PathSettings::pathSize)
                    )
                    .apply(p_351995_, PathStyle.PathSettings::new)
        );
    }
}

