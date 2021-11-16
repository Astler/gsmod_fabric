package com.astler.gsmod

import com.astler.gsmod.blocks.ModBlocks
import com.astler.gsmod.blocks.SieveTableBlock
import com.astler.gsmod.items.ModItems
import com.astler.gsmod.utils.RandomCollection
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry
import net.fabricmc.fabric.api.registry.FuelRegistry
import net.minecraft.block.Block
import net.minecraft.block.EnchantingTableBlock
import net.minecraft.client.render.RenderLayer
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.registry.Registry

fun String.registerBlockWithItem(pBlock: Block): Item {
    Registry.register(Registry.BLOCK, this.getModIdentifier(), pBlock)

    val nItem = BlockItem(pBlock, Item.Settings().group(ModCreativeTabs.GS_MOD_TAB))

    Registry.register(Registry.ITEM, this.getModIdentifier(), nItem)

    return nItem
}

fun String.registerWoodenBlockWithItem(
    pBlock: Block,
    pFuelTime: Int = 300,
    burnTime: Int = 30,
    spreadTime: Int = 20
): Item {
    Registry.register(Registry.BLOCK, this.getModIdentifier(), pBlock)

    val nItem = BlockItem(pBlock, Item.Settings().group(ModCreativeTabs.GS_MOD_TAB))

    Registry.register(Registry.ITEM, this.getModIdentifier(), nItem)

    FuelRegistry.INSTANCE.add(pBlock, pFuelTime)
    FlammableBlockRegistry.getDefaultInstance().add(pBlock, burnTime, spreadTime)

    return nItem
}

fun String.getModIdentifier(): Identifier {
    return Identifier(GSMod.MOD_ID, this)
}

class ModRegister {

    companion object {
        fun registerItems() {
            Registry.register(Registry.ITEM, Identifier(GSMod.MOD_ID, "stone_stick"), ModItems.STONE_STICK)
        }

        fun registerBlocks() {
            ModItems.SIEVE_TABLE_BLOCK = "sieve_table_block".registerWoodenBlockWithItem(ModBlocks.SIEVE_TABLE_BLOCK)

            ModItems.STONE_PATH = "stone_path".registerBlockWithItem(ModBlocks.STONE_PATH)
            ModItems.COBBLESTONE_PATH = "cobblestone_path".registerBlockWithItem(ModBlocks.COBBLESTONE_PATH)

            ModItems.COBBLESTONE_BOOKSHELF =
                "cobblestone_bookshelf".registerBlockWithItem(ModBlocks.COBBLESTONE_BOOKSHELF)
            ModItems.STONE_BOOKSHELF = "stone_bookshelf".registerBlockWithItem(ModBlocks.STONE_BOOKSHELF)
            ModItems.STONE_BRICKS_BOOKSHELF =
                "stone_bricks_bookshelf".registerBlockWithItem(ModBlocks.STONE_BRICKS_BOOKSHELF)

            ModItems.STONE_CRAFTING_TABLE =
                "stone_crafting_table".registerBlockWithItem(ModBlocks.STONE_CRAFTING_TABLE)
            ModItems.COBBLESTONE_CRAFTING_TABLE =
                "cobblestone_crafting_table".registerBlockWithItem(ModBlocks.COBBLESTONE_CRAFTING_TABLE)
            ModItems.STONE_BRICKS_CRAFTING_TABLE =
                "stone_bricks_crafting_table".registerBlockWithItem(ModBlocks.STONE_BRICKS_CRAFTING_TABLE)
        }

        fun registerOther() {

            println("ASTLER!")

            val res = ModRegister::class.java.getResourceAsStream("/data/gsmod/sieve/sieve_map.json")

            if (res != null) {
                val nJsonData = JsonHelper.deserialize(res.bufferedReader())
                val mapType = object : TypeToken<Map<String, JsonArray>>() {}.type
                val son: Map<String, JsonArray> = Gson().fromJson(nJsonData, mapType)

                val nKeys = son.keys

                val sieveDrops: HashMap<String, RandomCollection<String>> = HashMap()

                nKeys.forEach {
                    val sieveDropForItem = RandomCollection<String>()

                    val nArray = son[it] as JsonArray

                    nArray.forEach { pJsonElement ->
                        val chance = pJsonElement.asJsonObject.get("chance").asDouble
                        val item = pJsonElement.asJsonObject.get("item").asString
                        sieveDropForItem.add(chance, item)
                    }

                    if (it == "default") {
                        SieveTableBlock.sieveDefaultDrop = sieveDropForItem
                    } else {
                        sieveDrops[it] = sieveDropForItem
                    }

                    println(it)
                }

                SieveTableBlock.sieveRecipes = sieveDrops
            } else {
                SieveTableBlock.generateRecipes()
            }
        }

        fun registerLayers() {
            BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SIEVE_TABLE_BLOCK, RenderLayer.getCutoutMipped())
        }
    }

}