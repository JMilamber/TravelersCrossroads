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

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int distanceFromWorldCenter;


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        distanceFromWorldCenter = DISTANCE_FROM_WORLD_CENTER.get();
    }
}
