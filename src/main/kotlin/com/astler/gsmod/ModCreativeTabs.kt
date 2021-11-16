package com.astler.gsmod

import com.astler.gsmod.items.ModItems
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

object ModCreativeTabs {
    val GS_MOD_TAB: ItemGroup = FabricItemGroupBuilder.build(Identifier(GSMod.MOD_ID, "gs_tab")) {
        ItemStack(ModItems.STONE_STICK)
    }
}