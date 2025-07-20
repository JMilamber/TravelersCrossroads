package com.amber.roads.worldgen.custom.pathstyle;

import com.mojang.serialization.MapCodec;

public interface StyleModifierType<S extends PathStyle> {

    MapCodec<S> codec();
}
