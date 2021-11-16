package com.astler.gsmod.blocks

import com.astler.gsmod.blocks.state.properties.ModBlockStateProperties
import com.astler.gsmod.utils.RandomCollection
import com.astler.gsmod.utils.VoxelShapeHelper
import net.minecraft.block.*
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.stat.Stats
import net.minecraft.state.StateManager
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldEvents
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SieveTableBlock(pBlockProperties: Settings) : Block(pBlockProperties), InventoryProvider {
    private val mShape: VoxelShape

    init {
        mShape = generateShape()
        this.defaultState = this.stateManager.defaultState.with(HAS_LOOT, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>) {
        builder
            .add(HAS_LOOT)
            .add(LOOT_MODEL)
            .add(PROGRESS)
            .add(BLOCK_TYPE)
    }

    override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos?, oldState: BlockState?, notify: Boolean) {
        if (state.get(HAS_LOOT) && state.get(PROGRESS) == 1) {
            world.blockTickScheduler.schedule(pos, state.block, mDelay)
        }
    }

    override fun getOutlineShape(state: BlockState, world: BlockView?, pos: BlockPos?, context: ShapeContext?) = mShape

    override fun getRaycastShape(state: BlockState?, world: BlockView?, pos: BlockPos?) = mShape

    override fun getCollisionShape(s: BlockState?, w: BlockView?, p: BlockPos?, c: ShapeContext?) = mShape

    private fun generateShape(): VoxelShape {
        val shapes: MutableList<VoxelShape> = ArrayList()
        shapes.add(createCuboidShape(0.0, 0.0, 0.0, 2.0, 12.0, 2.0))
        shapes.add(createCuboidShape(0.0, 12.0, 0.0, 16.0, 14.0, 16.0))
        shapes.add(createCuboidShape(0.0, 0.0, 14.0, 2.0, 12.0, 16.0))
        shapes.add(createCuboidShape(14.0, 0.0, 0.0, 16.0, 12.0, 2.0))
        shapes.add(createCuboidShape(14.0, 0.0, 14.0, 16.0, 12.0, 16.0))

        return VoxelShapeHelper.combineAll(shapes)
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult?
    ): ActionResult {
        val hasLoot = state.get(HAS_LOOT)
        val isNotInProgress = state.get(PROGRESS) != 1
        val itemStack = player.getStackInHand(hand)
        val isItemCorrect = sieveAcceptableItems.contains(itemStack.item)

        return if (hasLoot && isNotInProgress) {
            emptyFullSieve(state, world, pos)
            ActionResult.success(world.isClient)
        } else if (isItemCorrect) {
            if (isNotInProgress && !world.isClient) {
                addToSieve(state, world, pos, itemStack)
                player.incrementStat(Stats.USED.getOrCreateStat(itemStack.item))
                if (!player.abilities.creativeMode) {
                    itemStack.decrement(1)
                }
            }

            ActionResult.success(world.isClient)
        } else {
            ActionResult.PASS
        }
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos?, random: Random?) {
        val hasLoot = state.get(HAS_LOOT)
        val isInProgress = state.get(PROGRESS) == 1

        if (hasLoot && isInProgress) {
            world.setBlockState(pos, state.with(LOOT_MODEL, Random().nextInt(3)).with(PROGRESS, 0), NOTIFY_ALL)
            world.playSound(
                null as PlayerEntity?,
                pos,
                SoundEvents.BLOCK_NETHER_GOLD_ORE_HIT,
                SoundCategory.BLOCKS,
                1.0f,
                1.0f
            )
        } else {
            world.setBlockState(pos, state.with(PROGRESS, 0), NOTIFY_ALL)
        }

        val blockForSieve: Block = when (state.get(BLOCK_TYPE)) {
            1 -> {
                Blocks.SAND
            }
            2 -> {
                Blocks.RED_SAND
            }
            3 -> {
                Blocks.GRAVEL
            }
            else -> {
                Blocks.DIRT
            }
        }

        world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, getRawIdFromState(blockForSieve.defaultState))
    }

    override fun getInventory(state: BlockState, world: WorldAccess, pos: BlockPos): SidedInventory {
        val hasLoot = state.get(HAS_LOOT)
        val isInProgress = state.get(PROGRESS) == 1

        if (isInProgress) return DummyInventory()

        return if (hasLoot) {
            OutputInventory(state, world, pos, ItemStack(giveItem(state)))
        } else {
            InputInventory(state, world, pos)
        }
    }

    private class OutputInventory(
        private val state: BlockState,
        private val world: WorldAccess,
        private val pos: BlockPos,
        outputItem: ItemStack?
    ) :
        SimpleInventory(outputItem), SidedInventory {
        private var dirty = false

        override fun getMaxCountPerStack() = 1

        override fun getAvailableSlots(side: Direction): IntArray {
            return if (side == Direction.DOWN) intArrayOf(0) else IntArray(0)
        }

        override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?) = false

        override fun canExtract(slot: Int, stack: ItemStack, dir: Direction): Boolean {
            return !dirty && dir == Direction.DOWN && !stack.isEmpty
        }

        override fun markDirty() {
            emptySieve(state, world, pos)
            dirty = true
        }
    }

    private class InputInventory(
        private val state: BlockState,
        private val world: WorldAccess,
        private val pos: BlockPos
    ) : SimpleInventory(1), SidedInventory {
        private var dirty = false

        override fun getMaxCountPerStack() = 1

        override fun getAvailableSlots(side: Direction): IntArray {
            return if (side == Direction.UP) intArrayOf(0) else IntArray(0)
        }

        override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
            return !dirty && dir == Direction.UP && sieveAcceptableItems.contains(stack.item)
        }

        override fun canExtract(slot: Int, stack: ItemStack, dir: Direction): Boolean {
            return false
        }

        override fun markDirty() {
            val itemStack = getStack(0)
            if (!itemStack.isEmpty) {
                dirty = true
                addToSieve(state, world, pos, itemStack)
                this.removeStack(0)
            }
        }
    }

    internal class DummyInventory : SimpleInventory(0), SidedInventory {
        override fun getAvailableSlots(side: Direction): IntArray {
            return IntArray(0)
        }

        override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
            return false
        }

        override fun canExtract(slot: Int, stack: ItemStack, dir: Direction): Boolean {
            return false
        }
    }

    companion object {
        const val mDelay = 30

        val HAS_LOOT = ModBlockStateProperties.SIEVE_HAS_LOOT
        val LOOT_MODEL = ModBlockStateProperties.SIEVE_LOOT_MODEL
        val PROGRESS = ModBlockStateProperties.SIEVE_PROGRESS
        val BLOCK_TYPE = ModBlockStateProperties.SIEVE_BLOCK_TYPE

        var sieveAcceptableItems = arrayOf(
            Items.DIRT,
            Items.SAND,
            Items.RED_SAND,
            Items.GRAVEL,
            Items.GRASS_BLOCK,
            Items.PODZOL,
            Items.MYCELIUM,
            Items.ROOTED_DIRT,
            Items.DIRT_PATH,
            Items.COARSE_DIRT
        )

        var sieveDefaultDrop: RandomCollection<String> = RandomCollection()
        var sieveRecipes: HashMap<String, RandomCollection<String>> = HashMap()

        fun generateRecipes() {
            val itemChance: RandomCollection<String> = RandomCollection()
            itemChance.add(10.0, "wheat_seeds")
            itemChance.add(20.0, "stick")
            itemChance.add(20.0, "iron_nugget")
            itemChance.add(8.0, "gold_nugget")
            itemChance.add(1.5, "iron_ingot")
            itemChance.add(0.5, "gold_ingot")

            sieveDefaultDrop = itemChance
        }

        fun addToSieve(state: BlockState, world: WorldAccess, pos: BlockPos?, itemStack: ItemStack): BlockState? {
            val hasLoot = state.get(HAS_LOOT)
            val item = itemStack.item

            val blockForSieve: Block
            var blockType = 0

            if (Items.SAND.equals(item)) {
                blockForSieve = Blocks.SAND
                blockType = 1
            } else if (Items.RED_SAND.equals(item)) {
                blockForSieve = Blocks.RED_SAND
                blockType = 2
            } else if (Items.GRAVEL.equals(item)) {
                blockForSieve = Blocks.GRAVEL
                blockType = 3
            } else {
                blockForSieve = Blocks.DIRT
            }

            world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, getRawIdFromState(blockForSieve.defaultState))

            val chance = 0.3f
            var blockState = state.with(PROGRESS, 1).with(BLOCK_TYPE, blockType)
            world.setBlockState(pos, blockState, NOTIFY_ALL)
            world.blockTickScheduler.schedule(pos, state.block, mDelay)

            if (hasLoot || world.random.nextDouble() < chance.toDouble()) {
                blockState = blockState.with(HAS_LOOT, true)
                world.setBlockState(pos, blockState, NOTIFY_ALL)
            }

            return blockState
        }

        fun giveItem(state: BlockState): Item {
            val blockType = when (state.get(BLOCK_TYPE)) {
                0 -> {
                    "dirt_type"
                }
                1, 2 -> {
                    "sand_type"
                }
                3 -> {
                    "gravel_type"
                }
                else -> {
                    "default"
                }
            }

            return if (blockType == "default" || !sieveRecipes.containsKey(blockType)) {
                Registry.ITEM.get(Identifier(sieveDefaultDrop.next()))
            } else {
                Registry.ITEM.get(Identifier(sieveRecipes[blockType]?.next() ?: "air"))
            }
        }

        fun emptyFullSieve(state: BlockState, world: World, pos: BlockPos): BlockState {
            if (!world.isClient) {
                val f = 0.7f
                val d = (world.random.nextFloat() * 0.7f).toDouble() + 0.15000000596046448
                val e = (world.random.nextFloat() * 0.7f).toDouble() + 0.06000000238418579 + 0.6
                val g = (world.random.nextFloat() * 0.7f).toDouble() + 0.15000000596046448
                val itemEntity = ItemEntity(
                    world, pos.x.toDouble() + d, pos.y.toDouble() + e, pos.z.toDouble() + g, ItemStack(
                        giveItem(state)
                    )
                )
                itemEntity.setToDefaultPickupDelay()
                world.spawnEntity(itemEntity)
            }

            val blockState = emptySieve(state, world, pos)
            world.playSound(
                null as PlayerEntity?,
                pos,
                SoundEvents.BLOCK_COMPOSTER_EMPTY,
                SoundCategory.BLOCKS,
                1.0f,
                1.0f
            )
            return blockState
        }

        fun emptySieve(state: BlockState, world: WorldAccess, pos: BlockPos?): BlockState {
            val blockState = state.with(HAS_LOOT, false).with(PROGRESS, 0)
            world.setBlockState(pos, blockState, NOTIFY_ALL)
            return blockState
        }
    }
}