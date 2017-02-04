package net.reederhome.colin.mods.exparvis

import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.{ModelBakery, ModelResourceLocation}
import net.minecraft.client.renderer.color.{IBlockColor, IItemColor}
import net.minecraft.item.{Item, ItemBlock, ItemStack}
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

import scala.collection.mutable.ArrayBuffer

class ClientProxy extends CommonProxy {
  var items: ArrayBuffer[Item] = new ArrayBuffer[Item]

  override def registerItem(item: Item, name: String): Unit = {
    super.registerItem(item, name)
    items.append(item)
  }

  override def preInit(): Unit = {
    super.preInit()
    ModelBakery.registerItemVariants(ItemOreNugget,
      ItemOreNugget.Type.getTypes.map((f) => f.base).toSet
        .map((f: String) => new ResourceLocation(ExParvis.MODID, "oreNugget_" + f)).toSeq: _*)
  }

  override def init(): Unit = {
    super.init()
    for (item <- items) {
      registerModel(item)
    }

    ItemOreNugget.Type.getTypes.foreach((f) => registerModel(ItemOreNugget, f.id, "oreNugget_" + f.base))
    Minecraft.getMinecraft.getItemColors.registerItemColorHandler(new IItemColor {
      override def getColorFromItemstack(itemStack: ItemStack, i: Int): Int = ItemOreNugget.getColor(itemStack, i)
    }, ItemOreNugget)

    Minecraft.getMinecraft.getBlockColors.registerBlockColorHandler(new IBlockColor {
      override def colorMultiplier(iBlockState: IBlockState, iBlockAccess: IBlockAccess, blockPos: BlockPos, i: Int): Int = iBlockState.getBlock match {
        case b: BlockNuggetOre => b.getColor
        case _ => 0xFFFFFF
      }
    }, BlockNuggetOre.getBlocks.toSeq: _*)
    Minecraft.getMinecraft.getItemColors.registerItemColorHandler(new IItemColor {
      override def getColorFromItemstack(itemStack: ItemStack, i: Int): Int = itemStack.getItem match {
        case ib: ItemBlock => ib.block match {
          case b: BlockNuggetOre => b.getColor
          case _ => 0xFFFFFF
        }
        case _ => 0xFFFFFF
      }
    }, BlockNuggetOre.getBlocks.map((f) => Item.getItemFromBlock(f)).toSeq: _*)
  }

  def registerModel(item: Item, meta: Int, model: String): Unit = {
    val loc = item.getRegistryName
    Minecraft.getMinecraft.getRenderItem.getItemModelMesher.register(item, meta, new ModelResourceLocation(loc.getResourceDomain + ":" + model, "inventory"))
  }

  def registerModel(item: Item): Unit = {
    val loc = item.getRegistryName
    registerModel(item, 0, loc.getResourcePath)
  }
}
