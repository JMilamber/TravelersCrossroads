package com.amber.roads.worldgen.custom.pathstyle;

import com.amber.roads.TravelersCrossroads;
import com.amber.roads.init.TravelersRegistries;
import com.amber.roads.world.PathNode;
import com.amber.roads.util.PathSize;
import com.amber.roads.util.TravelersDirection;
import com.amber.roads.world.PathPos;
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

import static com.amber.roads.util.TravelersUtil.isEven;
import static com.amber.roads.util.TravelersUtil.offsetBlockPos;
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

    public boolean placeSection(ServerLevel level, PathNode pos1, PathNode pos2) {
        // Immediately return if one of the chunks is not loaded
        if (!level.hasChunk(pos1.getChunkX(), pos1.getChunkZ()) || !level.hasChunk(pos2.getChunkX(), pos2.getChunkZ())) {
            return false;
        }

        int distance = this.getDistance();
        TravelersDirection direction = TravelersDirection.directionFromPos(pos1, pos2);

        // TravelersCrossroads.LOGGER.debug("place section {} {}", pos1, direction);
        int width = this.pathSize.getWidth();
        int extraWidth = this.pathSize.getExtraWidth();
        List<BlockPos> pathBlocks = new ArrayList<>();
        List<BlockPos> extraBlocks = new ArrayList<>();
        PathPos currPos = pos1.asPathPos();

        if (isEven(width)) {
            // If working with an odd path size, ensure center is middle of the block
            currPos.offset(.5f, .5f);
        }

        // Find the pathPos on the line between pos1 and pos2
        List<PathPos> linePos = new ArrayList<>();
        for (int i = 0; i < distance; i++) {
            currPos = currPos.relative(direction);
            linePos.add(currPos);
        }
        // TravelersCrossroads.LOGGER.debug("Place Path Section Pos1: {}, Pos2 {}, Line Pos: {}", pos1, pos2, linePos);

        int xWidth = floor(abs((width / 2f) * direction.getZ()));
        int zWidth = floor(abs((width / 2f) * direction.getX()));
        int xExtraWidth = floor(abs((extraWidth / 2f) * direction.getZ()));
        int zExtraWidth = floor(abs((extraWidth / 2f) * direction.getX()));

        TravelersCrossroads.LOGGER.debug(
                "PathSize: {} | Width: {} | xWidth: {} | zWidth: {} | xExtraWidth: {} | zExtraWidth: {}",
                this.settings.pathSize(), width, xWidth, zWidth, xExtraWidth, zExtraWidth
        );

        BlockPos extraPos;

        // for each blockpos on the line,
        for (PathPos pos: linePos) {
            for (int x = -xWidth; x <= zWidth; x++) {
                for (int z = -zWidth; z <= zWidth; z++) {
                    pathBlocks.add(pos.asBlockPos().offset(x, 0, z));
                }
            }
            for (int x = -xExtraWidth; x < xExtraWidth; x++) {
                for (int z = -zExtraWidth; z < zExtraWidth; z++) {
                    extraPos = pos.asBlockPos().offset(x, 0, z);
                    if (!pathBlocks.contains(extraPos)) {
                        extraBlocks.add(extraPos);
                    }
                }
            }
        }

        BlockPos sectionCenter = direction.nextSectionCenter(pos1, distance);
        int generationY = level.getChunkSource().getGenerator().getFirstOccupiedHeight(
                sectionCenter.getX(), sectionCenter.getZ(), Heightmap.Types.WORLD_SURFACE,
                level, level.getChunkSource().randomState());


        Optional<BlockPos> newPlacePos;
        for (BlockPos position : pathBlocks.stream().distinct().toList()) {
            newPlacePos = findY(level, position.above(generationY));
            newPlacePos.ifPresent(blockPos -> this.setPathBlock(level, blockPos));
        }

        placeExtraBlocks(level, pos1, direction, extraBlocks.stream().distinct().toList());
        return true;
    }

    public abstract void placeExtraBlocks(ServerLevel level, PathNode pos1, TravelersDirection direction, List<BlockPos> extraBlockPositions);

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

