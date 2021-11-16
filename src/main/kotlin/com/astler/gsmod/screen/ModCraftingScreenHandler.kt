package com.astler.gsmod.screen

import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.CraftingInventory
import net.minecraft.inventory.CraftingResultInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeMatcher
import net.minecraft.recipe.RecipeType
import net.minecraft.recipe.book.RecipeBookCategory
import net.minecraft.screen.AbstractRecipeScreenHandler
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.CraftingResultSlot
import net.minecraft.screen.slot.Slot
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.tag.BlockTags
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ModCraftingScreenHandler @JvmOverloads constructor(
    syncId: Int,
    playerInventory: PlayerInventory,
    context: ScreenHandlerContext = ScreenHandlerContext.EMPTY
) :
    AbstractRecipeScreenHandler<CraftingInventory?>(ScreenHandlerType.CRAFTING, syncId) {

    private val input: CraftingInventory = CraftingInventory(this, 3, 3)
    private val result: CraftingResultInventory = CraftingResultInventory()
    private val context: ScreenHandlerContext
    private val player: PlayerEntity

    override fun onContentChanged(inventory: Inventory) {
        context.run { world: World, pos: BlockPos? ->
            updateResult(
                this,
                world,
                player,
                input,
                result
            )
        }
    }

    override fun populateRecipeFinder(finder: RecipeMatcher) {
        input.provideRecipeInputs(finder)
    }

    override fun clearCraftingSlots() {
        input.clear()
        result.clear()
    }

    override fun matches(recipe: Recipe<in CraftingInventory?>): Boolean {
        return recipe.matches(input, player.world)
    }

    override fun close(player: PlayerEntity) {
        super.close(player)
        context.run { _: World?, _: BlockPos? ->
            dropInventory(
                player,
                input
            )
        }
    }

    override fun canUse(player: PlayerEntity): Boolean {
        return (context.get({ world: World, pos: BlockPos ->
            val identifier = Identifier("crafting_tables")
            val bookshelvesTag = BlockTags.getTagGroup().getTag(identifier)
            val nCorrectBlock = world.getBlockState(pos).isIn(bookshelvesTag)


            if (!nCorrectBlock) false
            else player.squaredDistanceTo(
                pos.x.toDouble() + 0.5,
                pos.y.toDouble() + 0.5,
                pos.z.toDouble() + 0.5
            ) <= 64.0
        }, true) as Boolean)
    }

    override fun transferSlot(player: PlayerEntity, index: Int): ItemStack {
        var itemStack = ItemStack.EMPTY
        val slot = slots[index]
        if (slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            if (index == 0) {
                context.run { world: World?, pos: BlockPos? ->
                    itemStack2.item.onCraft(itemStack2, world, player)
                }
                if (!insertItem(itemStack2, 10, 46, true)) {
                    return ItemStack.EMPTY
                }
                slot.onQuickTransfer(itemStack2, itemStack)
            } else if (index in 10..45) {
                if (!insertItem(itemStack2, 1, 10, false)) {
                    if (index < 37) {
                        if (!insertItem(itemStack2, 37, 46, false)) {
                            return ItemStack.EMPTY
                        }
                    } else if (!insertItem(itemStack2, 10, 37, false)) {
                        return ItemStack.EMPTY
                    }
                }
            } else if (!insertItem(itemStack2, 10, 46, false)) {
                return ItemStack.EMPTY
            }
            if (itemStack2.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
            if (itemStack2.count == itemStack.count) {
                return ItemStack.EMPTY
            }
            slot.onTakeItem(player, itemStack2)
            if (index == 0) {
                player.dropItem(itemStack2, false)
            }
        }
        return itemStack
    }

    override fun canInsertIntoSlot(stack: ItemStack, slot: Slot): Boolean {
        return slot.inventory !== result && super.canInsertIntoSlot(stack, slot)
    }

    override fun getCraftingResultSlotIndex(): Int {
        return 0
    }

    override fun getCraftingWidth(): Int {
        return input.width
    }

    override fun getCraftingHeight(): Int {
        return input.height
    }

    override fun getCraftingSlotCount(): Int {
        return 10
    }

    override fun getCategory(): RecipeBookCategory {
        return RecipeBookCategory.CRAFTING
    }

    override fun canInsertIntoSlot(index: Int): Boolean {
        return index != this.craftingResultSlotIndex
    }

    companion object {
        const val field_30781 = 0
        private const val field_30782 = 1
        private const val field_30783 = 10
        private const val field_30784 = 10
        private const val field_30785 = 37
        private const val field_30786 = 37
        private const val field_30787 = 46
        protected fun updateResult(
            handler: ScreenHandler,
            world: World,
            player: PlayerEntity,
            craftingInventory: CraftingInventory,
            resultInventory: CraftingResultInventory
        ) {
            if (!world.isClient) {
                val serverPlayerEntity = player as ServerPlayerEntity
                var itemStack = ItemStack.EMPTY
                val optional = world.server!!.recipeManager.getFirstMatch(RecipeType.CRAFTING, craftingInventory, world)
                if (optional.isPresent) {
                    val craftingRecipe = optional.get()
                    if (resultInventory.shouldCraftRecipe(world, serverPlayerEntity, craftingRecipe)) {
                        itemStack = craftingRecipe.craft(craftingInventory)
                    }
                }
                resultInventory.setStack(0, itemStack)
                handler.setPreviousTrackedSlot(0, itemStack)
                serverPlayerEntity.networkHandler.sendPacket(
                    ScreenHandlerSlotUpdateS2CPacket(
                        handler.syncId,
                        handler.nextRevision(),
                        0,
                        itemStack
                    )
                )
            }
        }
    }

    init {
        this.context = context
        player = playerInventory.player
        addSlot(CraftingResultSlot(playerInventory.player, input, result, 0, 124, 35))
        var l: Int
        var m = 0
        while (m < 3) {
            l = 0
            while (l < 3) {
                addSlot(Slot(input, l + m * 3, 30 + l * 18, 17 + m * 18))
                ++l
            }
            ++m
        }
        m = 0
        while (m < 3) {
            l = 0
            while (l < 9) {
                addSlot(Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18))
                ++l
            }
            ++m
        }
        m = 0
        while (m < 9) {
            addSlot(Slot(playerInventory, m, 8 + m * 18, 142))
            ++m
        }
    }
}