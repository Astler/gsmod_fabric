package com.astler.gsmod.blocks

import net.minecraft.block.*
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.item.ItemPlacementContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView
import java.util.*


class PathBlock constructor(settings: Settings, private val defaultBlock: Block) : Block(settings) {

    override fun hasSidedTransparency(state: BlockState) = true
    override fun canPathfindThrough(state: BlockState, world: BlockView, pos: BlockPos, type: NavigationType) = false

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return if (!defaultState.canPlaceAt(ctx.world, ctx.blockPos)) pushEntitiesUpBeforeBlockChange(
            defaultState, defaultBlock.defaultState, ctx.world, ctx.blockPos
        ) else super.getPlacementState(ctx)
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: WorldAccess,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        if (direction == Direction.UP && !state.canPlaceAt(world, pos)) {
            world.blockTickScheduler.schedule(pos, this, 1)
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        world.setBlockState(pos, pushEntitiesUpBeforeBlockChange(state, defaultBlock.defaultState, world, pos))
    }

    override fun canPlaceAt(state: BlockState, world: WorldView, pos: BlockPos): Boolean {
        val blockState = world.getBlockState(pos.up())
        return !blockState.material.isSolid || blockState.block is FenceGateBlock
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape {
        return SHAPE!!
    }

    companion object {
        protected var SHAPE: VoxelShape? = null

        init {
            SHAPE = createCuboidShape(0.0, 0.0, 0.0, 16.0, 15.0, 16.0)
        }
    }
}
