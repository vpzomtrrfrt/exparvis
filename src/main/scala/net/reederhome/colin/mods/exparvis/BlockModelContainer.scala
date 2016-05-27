package net.reederhome.colin.mods.exparvis

import net.minecraft.block.BlockContainer
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumBlockRenderType

abstract class BlockModelContainer(material: Material) extends BlockContainer(material) {
  override def getRenderType(p_getRenderType_1_ : IBlockState): EnumBlockRenderType = EnumBlockRenderType.MODEL
}
