package net.reederhome.colin.mods.exparvis

import net.minecraft.block.Block
import net.minecraft.item.{Item, ItemBlock}
import net.minecraftforge.event.RegistryEvent.Register
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.GameRegistry

import scala.collection.mutable.ArrayBuffer

class CommonProxy {
  def preInit(): Unit = {
    println("ayy")
    registerBlock(BlockMelter, "melter")
    registerItem(ItemPebble, "pebble")
    registerItem(ItemHammer.Stone, "hammerStone")
    registerItem(ItemHammer.Iron, "hammerIron")
    registerItem(ItemOreNugget, "oreNugget")
    ItemOreNugget.Type.getTypes.foreach((f) => registerBlock(new BlockNuggetOre(f), "nuggetOre_" + f.getName))

    GameRegistry.registerTileEntity(classOf[TileEntityMelter], "Melter")
  }

  def init(): Unit = {

  }

  private val items: ArrayBuffer[Item] = new ArrayBuffer[Item]
  private val blocks: ArrayBuffer[Block] = new ArrayBuffer[Block]

  @SubscribeEvent
  def onRegisterItems(event: Register[Item]): Unit = event.getRegistry.registerAll(items : _*)

  @SubscribeEvent
  def onRegisterBlocks(event: Register[Block]): Unit = event.getRegistry.registerAll(blocks : _*)

  def registerBlock(block: Block, name: String): Unit = {
    block.setRegistryName(name)
    blocks.append(block)
    registerItem(new ItemBlock(block), name)
  }

  def registerItem(item: Item, name: String): Unit = {
    item.setRegistryName(name)
    items.append(item)
  }

  def getItems: Array[Item] = items.toArray
}
