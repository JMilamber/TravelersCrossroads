package com.amber_roads.init;

import com.amber_roads.TravelersCrossroads;
import com.amber_roads.worldgen.custom.OffsetModifier;
import com.amber_roads.worldgen.custom.StyleModifier;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = TravelersCrossroads.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class TravelersRegistries {

    public static final Registry<MapCodec<? extends StyleModifier>> STYLE_MODIFIER_SERIALIZERS = new RegistryBuilder<>(Keys.STYLE_MODIFIER_SERIALIZERS).create();
    public static final Registry<MapCodec<? extends OffsetModifier>> OFFSET_MODIFIER_SERIALIZERS = new RegistryBuilder<>(Keys.OFFSET_MODIFIER_SERIALIZERS).create();

    public static final class Keys {
        public static final ResourceKey<Registry<MapCodec<? extends StyleModifier>>> STYLE_MODIFIER_SERIALIZERS = key("style_modifier_serializers");
        public static final ResourceKey<Registry<MapCodec<? extends OffsetModifier>>> OFFSET_MODIFIER_SERIALIZERS = key("offset_modifier_serializers");
        // Dynamic
        public static final ResourceKey<Registry<StyleModifier>> STYLE_MODIFIERS = key("style_modifier");
        public static final ResourceKey<Registry<OffsetModifier>> OFFSET_MODIFIERS = key("offset_modifier");
    }

    private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(TravelersCrossroads.travelersLocation(name));
    }

    @SubscribeEvent
    static void registerRegistries(NewRegistryEvent event) {
        event.register(STYLE_MODIFIER_SERIALIZERS);
        event.register(OFFSET_MODIFIER_SERIALIZERS);
    }
}
