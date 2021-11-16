package com.astler.gsmod.utils

import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes


object VoxelShapeHelper {
    fun combineAll(shapes: Collection<VoxelShape?>): VoxelShape {
        var result: VoxelShape = VoxelShapes.empty()
        for (shape in shapes) {
            result = VoxelShapes.combine(result, shape, BooleanBiFunction.OR)
        }
        return result.simplify()
    }
}
