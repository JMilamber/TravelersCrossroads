package com.amber_roads.datagen;

import com.amber_roads.TravelersCrossroads;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = TravelersCrossroads.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class TravelersDataGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new TravelersRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(),
                        List.of(new LootTableProvider.SubProviderEntry(TravelersLootTableProvider::new, LootContextParamSets.BLOCK)), lookupProvider));
        generator.addProvider(event.includeServer(), new TravelersBlockTagProvider(packOutput, lookupProvider, TravelersCrossroads.MOD_ID, existingFileHelper));
        generator.addProvider(event.includeServer(), new TravelersStructureTagProvider(packOutput, lookupProvider, TravelersCrossroads.MOD_ID, existingFileHelper));

        generator.addProvider(event.includeServer(), new TravelersBiomeTagProvider(packOutput, lookupProvider, TravelersCrossroads.MOD_ID, existingFileHelper));

        /* SUPER IMPORTANT -- Must use lookup provider from WorldGenProvider (which extends DatapackBuiltinEntriesProvider) for datapackRegistry tags
          otherwise it can't find the registries. */
        DatapackBuiltinEntriesProvider datapackprovider = new TravelersWorldGenProvider(packOutput, lookupProvider);
        generator.addProvider(event.includeServer(), datapackprovider);
    }
}
