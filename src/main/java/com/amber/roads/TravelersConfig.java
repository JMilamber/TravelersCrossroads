package com.amber.roads;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = TravelersCrossroads.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class TravelersConfig
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue DISTANCE_FROM_WORLD_CENTER = BUILDER
            .comment("How many chunks away from world center a path must be to spawn")
            .defineInRange("distanceFromWorldCenter", 7, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue MAX_FIND_Y_STEPS = BUILDER
            .comment("Maximum vertical checks while finding valid path surface Y before giving up")
            .defineInRange("maxFindYSteps", 96, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue MAX_SECTIONS_PER_TICK = BUILDER
            .comment("Maximum number of path sections placed per build tick")
            .defineInRange("maxSectionsPerTick", 2, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue MAX_SECTION_PLACE_RETRIES = BUILDER
            .comment("Maximum retries for placing the same path section before aborting that path")
            .defineInRange("maxSectionPlaceRetries", 12, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue MAX_STRUCTURE_DISTANCE = BUILDER
            .comment("How many chunks away from a crossroad a structure can be and still get a path")
            .defineInRange("maxStructureDistance", 20, 1, 256);

    private static final ModConfigSpec.IntValue STRUCTURE_SEARCH_RADIUS = BUILDER
            .comment("How far to search for structures, in structure grid cells (about 32-40 chunks each for vanilla structures). High values can freeze the server when nothing is nearby")
            .defineInRange("structureSearchRadius", 3, 1, 100);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int distanceFromWorldCenter;
    public static int maxFindYSteps;
    public static int maxSectionsPerTick;
    public static int maxSectionPlaceRetries;
    public static int maxStructureDistance;
    public static int structureSearchRadius;


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        distanceFromWorldCenter = DISTANCE_FROM_WORLD_CENTER.get();
        maxFindYSteps = MAX_FIND_Y_STEPS.get();
        maxSectionsPerTick = MAX_SECTIONS_PER_TICK.get();
        maxSectionPlaceRetries = MAX_SECTION_PLACE_RETRIES.get();
        maxStructureDistance = MAX_STRUCTURE_DISTANCE.get();
        structureSearchRadius = STRUCTURE_SEARCH_RADIUS.get();
    }
}
