package com.amber.roads.datagen;

import com.amber.roads.init.TravelersInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;

import java.util.concurrent.CompletableFuture;

public class TravelersRecipeProvider extends RecipeProvider {

    public TravelersRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        threeByThreePacker(RecipeCategory.MISC, TravelersInit.CAIRN_ITEM.get(), TravelersInit.PEBBLE.get());
    }

    // The data provider class
    public static class Runner extends RecipeProvider.Runner {

        protected Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new TravelersRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "TravelersRecipeProvider";
        }
    }
}
