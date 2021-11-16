package com.astler.gsmod.blocks.state.properties

import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Property

object ModBlockStateProperties {
    val SIEVE_LOOT_MODEL: IntProperty = IntProperty.of("loot_model", 0, 3)
    val SIEVE_PROGRESS: IntProperty = IntProperty.of("progress", 0, 1)

    /**
     * 0 - dirt_type
     * 1 - sand_type
     * 2 - red_sand_type
     * 3 - gravel_type
     */

    val SIEVE_BLOCK_TYPE: IntProperty = IntProperty.of("block_type", 0, 4)
    val SIEVE_HAS_LOOT: BooleanProperty = BooleanProperty.of("has_loot")
}