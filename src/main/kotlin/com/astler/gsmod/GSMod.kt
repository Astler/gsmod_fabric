package com.astler.gsmod

import com.astler.gsmod.screen.ModCraftingScreenHandler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.minecraft.screen.ScreenHandlerType

@Suppress("UNUSED")
object GSMod : ModInitializer {
    const val MOD_ID = "gsmod"

    override fun onInitialize() {
        ModRegister.registerBlocks()
        ModRegister.registerItems()
        ModRegister.registerLayers()
        ModRegister.registerOther()
    }
}