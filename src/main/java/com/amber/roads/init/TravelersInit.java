package com.amber.roads.init;

import com.amber.roads.TravelersCrossroads;
import com.amber.roads.block.CairnBlock;
import com.amber.roads.worldgen.custom.*;
import com.amber.roads.worldgen.TravelersBeginning;
import com.amber.roads.worldgen.custom.pathstyle.PathStyle;
import com.amber.roads.worldgen.custom.pathstyle.PercentStyle;
import com.amber.roads.worldgen.custom.pathstyle.SparseStyle;
import com.amber.roads.worldgen.custom.pathstyle.StyleModifierType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class TravelersInit {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(TravelersCrossroads.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, TravelersCrossroads.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TravelersCrossroads.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TravelersCrossroads.MOD_ID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, TravelersCrossroads.MOD_ID);
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENTS = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, TravelersCrossroads.MOD_ID);
    public static final DeferredRegister<StyleModifierType<?>> STYLE_MODIFIER_SERIALIZERS = DeferredRegister.create(TravelersRegistries.STYLE_MODIFIER_TYPE, TravelersCrossroads.MOD_ID);
    public static final DeferredRegister<MapCodec<? extends OffsetModifier>> OFFSET_MODIFIER_SERIALIZERS = DeferredRegister.create(TravelersRegistries.OFFSET_MODIFIER_SERIALIZERS, TravelersCrossroads.MOD_ID);

    public static final Supplier<Block> CAIRN = registerBlock(
            "cairn", () -> new CairnBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL).forceSolidOn())
    );

    public static final Supplier<Item> PEBBLE = ITEMS.register(
            "pebble", () -> new Item(new Item.Properties().stacksTo(24))
    );

    public static final Supplier<CreativeModeTab> TRAVELERS_TAB = CREATIVE_MODE_TABS.register(
            "travelers_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.travelerscrossroads.travelers_tab"))
                    .icon(() -> new ItemStack(CAIRN.get()))
                    .displayItems((pParameters, pOutput) -> pOutput.accept(CAIRN.get())
                    ).build()
    );

    public static final Supplier<Feature<NoneFeatureConfiguration>> TRAVELERS_BEGINNING = registerFeature(
            "travelers_beginning", () -> new TravelersBeginning(NoneFeatureConfiguration.CODEC)
    );

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<DistanceFilter>> DISTANCE_FILTER = PLACEMENTS.register(
            "distance_filter", () -> explicitPlacmentTypeTyping(DistanceFilter.CODEC)
    );

    public static final DeferredHolder<StyleModifierType<?>, StyleModifierType<PercentStyle>> PERCENT_STYLE = STYLE_MODIFIER_SERIALIZERS.register(
            "percent_style", () -> explicitPathStyleTypeTyping(PercentStyle.CODEC));

    public static final DeferredHolder<StyleModifierType<?>, StyleModifierType<SparseStyle>> SPARSE_STYLE = STYLE_MODIFIER_SERIALIZERS.register(
            "sparse_style", () -> explicitPathStyleTypeTyping(SparseStyle.CODEC));


    public static final DeferredHolder<MapCodec<? extends OffsetModifier>, MapCodec<PathModifiers.DistanceModifier>> DISTANCE_OFFSET_MODIFIER_TYPE = OFFSET_MODIFIER_SERIALIZERS.register("distance_offset_modifier", () -> RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            RegistryCodecs.homogeneousList(Registries.STRUCTURE).fieldOf("structures").forGetter(PathModifiers.DistanceModifier::structures),
                            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("offset").forGetter(PathModifiers.DistanceModifier::offset))
                    .apply(builder, PathModifiers.DistanceModifier::new))
    );

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = (DeferredBlock<T>) BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Feature<?>> Supplier<T> registerFeature(String name, Supplier<T> feature) {
        return FEATURES.register(name, feature);
    }

    /**
     * ** TelepathicGrunt Structure Tutorial 1.20.2 neoforge **
     * Originally, I had a double lambda ()->()-> for the RegistryObject line above, but it turns out that
     * some IDEs cannot resolve the typing correctly. This method explicitly states what the return type
     * is so that the IDE can put it into the DeferredRegistry properly.
     */
    private static <T extends PlacementModifier> PlacementModifierType<T> explicitPlacmentTypeTyping(MapCodec<T> placementTypeCodec) {
        return () -> placementTypeCodec;
    }

    private static <T extends PathStyle> StyleModifierType<T> explicitPathStyleTypeTyping(MapCodec<T> pathStyleTypeCodec) {
        return () -> pathStyleTypeCodec;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);
        FEATURES.register(eventBus);
        PLACEMENTS.register(eventBus);
        STYLE_MODIFIER_SERIALIZERS.register(eventBus);
        OFFSET_MODIFIER_SERIALIZERS.register(eventBus);
    }
}
