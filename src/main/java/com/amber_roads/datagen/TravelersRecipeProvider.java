package com.amber_roads.datagen;

import com.amber_roads.init.TravelersInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class TravelersRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public TravelersRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        threeByThreePacker(recipeOutput, RecipeCategory.MISC, TravelersInit.CAIRN.get(), TravelersInit.PEBBLE.get());
    }
}
