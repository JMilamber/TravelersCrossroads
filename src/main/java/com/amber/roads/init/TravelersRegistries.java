package com.amber.roads.init;

import com.amber.roads.TravelersCrossroads;
import com.amber.roads.worldgen.custom.OffsetModifier;
import com.amber.roads.worldgen.custom.pathstyle.PathStyle;
import com.amber.roads.worldgen.custom.pathstyle.StyleModifierType;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = TravelersCrossroads.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class TravelersRegistries {

    public static final Registry<StyleModifierType<?>> PATH_STYLE_TYPE = new RegistryBuilder<>(Keys.PATH_STYLE_TYPE).create();

    public static final Registry<MapCodec<? extends OffsetModifier>> STRUCTURE_OFFSET_SERIALIZERS = new RegistryBuilder<>(Keys.STRUCTURE_OFFSET_SERIALIZERS).create();

    public static final class Keys {
        public static final ResourceKey<Registry<StyleModifierType<?>>> PATH_STYLE_TYPE = key("path_style_type");
        public static final ResourceKey<Registry<MapCodec<? extends OffsetModifier>>> STRUCTURE_OFFSET_SERIALIZERS = key("structure_offset_serializers");
        // Dynamic
        public static final ResourceKey<Registry<PathStyle>> PATH_STYLES = key("path_style");
        public static final ResourceKey<Registry<OffsetModifier>> STRUCTURE_OFFSETS = key("structure_offset");
    }

    private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(TravelersCrossroads.travelersLocation(name));
    }

    @SubscribeEvent
    static void registerRegistries(NewRegistryEvent event) {
        event.register(PATH_STYLE_TYPE);
        event.register(STRUCTURE_OFFSET_SERIALIZERS);
    }
}
