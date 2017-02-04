package net.reederhome.colin.mods.exparvis

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.util.BlockRenderLayer
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.mutable

class BlockNuggetOre(typ: ItemOreNugget.Type, material: Material) extends Block(material) {
  setUnlocalizedName("nuggetOre_" + typ.getName)
  setHardness(1.2f)
  setCreativeTab(CreativeTabs.MATERIALS)
  BlockNuggetOre.map.put(typ.id, this)

  def this(typ: ItemOreNugget.Type) {
    this(typ, typ.base match {
      case _ => Material.SAND
    })
  }

  @SideOnly(Side.CLIENT) override def getBlockLayer = BlockRenderLayer.CUTOUT_MIPPED

  def getColor: Int = typ.color
}

object BlockNuggetOre {
  def getBlock(id: Int): BlockNuggetOre = map(id)

  private val map = new mutable.HashMap[Int, BlockNuggetOre]()

  def getBlocks: Iterable[BlockNuggetOre] = map.values
}
