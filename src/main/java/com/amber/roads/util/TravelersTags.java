package com.amber.roads.util;

import com.amber.roads.TravelersCrossroads;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;

import static com.amber.roads.TravelersCrossroads.travelersLocation;

public class TravelersTags {
    public static class Blocks {
        public static final  TagKey<Block> PATH_ABOVE = createTag("path_above");
        public static final  TagKey<Block> PATH_BELOW = createTag("path_below");

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(TravelersCrossroads.MOD_ID, name));
        }
    }

    public static class Items {
        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(TravelersCrossroads.MOD_ID, name));
        }
    }

    public static class Structures {
        public static final TagKey<Structure> PATH_STRUCTURES = createTag("path_structures");
        public static final TagKey<Structure> ZERO_OFFSET_STRUCTURES = createTag("zero_offset_structures");
        public static final TagKey<Structure> DEFAULT_OFFSET_STRUCTURES = createTag("default_offset_structures");
        public static final TagKey<Structure> VILLAGE_OFFSET_STRUCTURES = createTag("village_offset_structures");
        public static final TagKey<Structure> MANSION_OFFSET_STRUCTURES = createTag("mansion_offset_structures");

        private static TagKey<Structure> createTag(String name) {
            return create(travelersLocation(name));
        }
        public static TagKey<Structure> create(final ResourceLocation name) {
            return TagKey.create(Registries.STRUCTURE, name);
        }
    }

    public static class Biomes {
        public static final TagKey<Biome> PATH_AVOID = createTag("path_avoid_biome");

        private static TagKey<Biome> createTag(String name) {
            return create(travelersLocation(name));
        }
        public static TagKey<Biome> create(final ResourceLocation name) {
            return TagKey.create(Registries.BIOME, name);
        }
    }

}
