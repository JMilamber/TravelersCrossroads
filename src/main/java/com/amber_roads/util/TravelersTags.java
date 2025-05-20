package com.amber_roads.util;

import com.amber_roads.TravelersCrossroads;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;

public class TravelersTags {
    public static class Blocks {
        public static final  TagKey<Block> PATH_ABOVE = createTag("path_above");
        public static final  TagKey<Block> PATH_BELOW = createTag("path_below");

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(TravelersCrossroads.MODID, name));
        }
    }

    public static class Items {
        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(TravelersCrossroads.MODID, name));
        }
    }

    public static class Structures {
        public static final TagKey<Structure> PATH_AVOID = createTag("path_avoid_structure");

        private static TagKey<Structure> createTag(String name) {
            return create(ResourceLocation.fromNamespaceAndPath(TravelersCrossroads.MODID, name));
        }
        public static TagKey<Structure> create(final ResourceLocation name) {
            return TagKey.create(Registries.STRUCTURE, name);
        }
    }

    public static class Biomes {
        public static final TagKey<Biome> PATH_AVOID = createTag("path_avoid_biome");

        private static TagKey<Biome> createTag(String name) {
            return create(ResourceLocation.fromNamespaceAndPath(TravelersCrossroads.MODID, name));
        }
        public static TagKey<Biome> create(final ResourceLocation name) {
            return TagKey.create(Registries.BIOME, name);
        }
    }
}
