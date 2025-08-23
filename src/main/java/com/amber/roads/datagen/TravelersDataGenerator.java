package com.amber.roads.datagen;

import com.amber.roads.TravelersCrossroads;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = TravelersCrossroads.MOD_ID)
public class TravelersDataGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        event.createProvider(TravelersRecipeProvider.Runner::new);
        event.createProvider(TravelersLootTableprovider::new);
        event.createProvider(TravelersBlockTagProvider::new);
        event.createProvider(TravelersStructureTagProvider::new);
        event.createProvider(TravelersBiomeTagProvider::new);

        /* SUPER IMPORTANT -- Must use lookup provider from WorldGenProvider (which extends DatapackBuiltinEntriesProvider) for datapackRegistry tags
          otherwise it can't find the registries. */
        event.createDatapackRegistryObjects(TravelersWorldGenProvider.BUILDER);
    }
}
