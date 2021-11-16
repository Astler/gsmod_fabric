package com.astler.gsmod.mixins

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.EnchantingTableBlock
import net.minecraft.tag.BlockTags
import net.minecraft.util.Identifier
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Redirect

@Mixin(EnchantingTableBlock::class)
class EnchantingParticlesMixin {
    @Redirect(
        method = ["randomDisplayTick"],
        at = At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z")
    )
    private fun isBookshelf(blockState: BlockState, block: Block): Boolean {
        val bookshelvesIdentifier = Identifier("bookshelves")
        val bookshelvesTag = BlockTags.getTagGroup().getTag(bookshelvesIdentifier)
        return blockState.isIn(bookshelvesTag)
    }
}