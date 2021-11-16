package com.astler.gsmod.items

import com.astler.gsmod.ModCreativeTabs
import com.astler.gsmod.blocks.ModBlocks
import net.minecraft.item.BlockItem
import net.minecraft.item.Item

object ModItems {
    var STONE_STICK = Item(Item.Settings().group(ModCreativeTabs.GS_MOD_TAB))

    lateinit var SIEVE_TABLE_BLOCK: Item

    lateinit var STONE_PATH: Item
    lateinit var COBBLESTONE_PATH: Item

    lateinit var COBBLESTONE_BOOKSHELF: Item
    lateinit var STONE_BOOKSHELF: Item
    lateinit var STONE_BRICKS_BOOKSHELF: Item

    lateinit var STONE_CRAFTING_TABLE: Item
    lateinit var COBBLESTONE_CRAFTING_TABLE: Item
    lateinit var STONE_BRICKS_CRAFTING_TABLE: Item
}