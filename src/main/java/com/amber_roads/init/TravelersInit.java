package com.amber_roads.init;

import com.amber_roads.TravelersCrossroads;
import com.amber_roads.block.CairnBlock;
import com.amber_roads.entity.blockentity.CairnBlockEntity;
import com.amber_roads.world.TravelersBeginning;
import com.amber_roads.world.TravelersConfiguration;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;


public class TravelersInit {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(TravelersCrossroads.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, TravelersCrossroads.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TravelersCrossroads.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TravelersCrossroads.MODID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, TravelersCrossroads.MODID);


    public static final Supplier<Block> CAIRN = registerBlock("cairn", () -> new CairnBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE)));

    public static final Supplier<BlockEntityType<CairnBlockEntity>> CAIRN_BE =
            BLOCK_ENTITIES.register("cairn_be", () -> BlockEntityType.Builder.of(
                    CairnBlockEntity::new, CAIRN.get()).build(null)
            );

    public static final Supplier<CreativeModeTab> TRAVELERS_TAB =
            CREATIVE_MODE_TABS.register("travelers_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.travelerscrossroads.travelers_tab"))
                    .icon(() -> new ItemStack(CAIRN.get()))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(CAIRN.get());
                    }).build()
            );

    public static final Supplier<Feature<TravelersConfiguration>> TRAVELERS_BEGINNING = registerFeature("travelers_beginning", () -> new TravelersBeginning(TravelersConfiguration.CODEC));

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

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);
        FEATURES.register(eventBus);
    }
}
