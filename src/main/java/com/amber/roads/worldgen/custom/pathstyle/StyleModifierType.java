package com.amber.roads.worldgen.custom.pathstyle;

import com.amber.roads.init.TravelersRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;

public interface StyleModifierType<S extends PathStyle> {

    MapCodec<S> codec();

    private static <S extends PathStyle> StyleModifierType<S> register(String name, MapCodec<S> codec) {
        return Registry.register(TravelersRegistries.STYLE_MODIFIER_TYPE, name, () -> codec);
    }
}
