package net.reederhome.colin.mods.exparvis

import net.minecraft.block.Block
import net.minecraft.item.{Item, ItemBlock}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.registry.GameRegistry

class CommonProxy {
  def preInit(): Unit = {
    registerBlock(BlockMelter, "melter")
    registerItem(ItemPebble, "pebble")
    registerItem(ItemHammer.Stone, "hammerStone")
    registerItem(ItemHammer.Iron, "hammerIron")
    registerItem(ItemOreNugget, "oreNugget")

    GameRegistry.registerTileEntity(classOf[TileEntityMelter], "Melter")
  }

  def init(): Unit = {

  }

  def registerBlock(block: Block, name: String): Unit = {
    block.setRegistryName(name)
    GameRegistry.register(block)
    registerItem(new ItemBlock(block), name)
  }

  def registerItem(item: Item, name: String): Unit = {
    item.setRegistryName(name)
    GameRegistry.register(item)
  }
}
