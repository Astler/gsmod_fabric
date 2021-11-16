package com.astler.gsmod.blocks

import net.minecraft.block.*
import net.minecraft.sound.BlockSoundGroup


object ModBlocks {
    val SIEVE_TABLE_BLOCK =
        SieveTableBlock(AbstractBlock.Settings.of(Material.WOOD).strength(0.6F).sounds(BlockSoundGroup.WOOD))

    val STONE_PATH = PathBlock(
        AbstractBlock.Settings.of(Material.STONE, MapColor.STONE_GRAY).requiresTool().strength(1.5f, 6.0f),
        Blocks.STONE
    )

    val COBBLESTONE_PATH = PathBlock(
        AbstractBlock.Settings.of(Material.STONE, MapColor.STONE_GRAY).requiresTool().strength(1.5f, 6.0f),
        Blocks.COBBLESTONE
    )

    val COBBLESTONE_BOOKSHELF = simpleStoneBlock()
    val STONE_BOOKSHELF = simpleStoneBlock()
    val STONE_BRICKS_BOOKSHELF = simpleStoneBlock()

    val STONE_CRAFTING_TABLE = simpleCraftingTable()
    val COBBLESTONE_CRAFTING_TABLE = simpleCraftingTable()
    val STONE_BRICKS_CRAFTING_TABLE = simpleCraftingTable()
}

fun simpleStoneBlock(pMaterial: AbstractBlock.Settings = stoneBlock()): Block {
    return Block(pMaterial)
}

fun simpleCraftingTable(pMaterial: AbstractBlock.Settings = stoneBlock()): Block {
    return SimpleCraftingTableBlock(pMaterial)
}

fun stoneBlock(): AbstractBlock.Settings {
    return AbstractBlock.Settings.of(Material.STONE, MapColor.STONE_GRAY).requiresTool().strength(1.5f, 6.0f)
}