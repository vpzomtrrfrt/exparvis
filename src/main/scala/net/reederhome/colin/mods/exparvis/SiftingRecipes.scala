package net.reederhome.colin.mods.exparvis

import net.minecraft.block.Block
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{EnumDyeColor, ItemStack}

import scala.collection.mutable
import scala.util.Random

object SiftingRecipes {
  private val map = new mutable.HashMap[SiftingType, mutable.HashMap[Block, mutable.MutableList[ItemChance]]]()

  addItem(SiftingType.STICK, Blocks.DIRT, ItemChance(0.5f, new ItemStack(ItemPebble)))
  addItem(SiftingType.STICK, Blocks.DIRT, ItemChance(0.5f, new ItemStack(ItemPebble)))
  addItem(SiftingType.STICK, Blocks.DIRT, ItemChance(0.5f, new ItemStack(ItemPebble)))
  addItem(SiftingType.STICK, Blocks.DIRT, ItemChance(0.5f, new ItemStack(ItemPebble)))

  addItem(SiftingType.STICK, Blocks.GRAVEL, ItemChance(.3f * .55f, ItemOreNugget.Type.GRAVEL_IRON.getStack))
  addItem(SiftingType.STICK, Blocks.GRAVEL, ItemChance(.3f * .11f, ItemOreNugget.Type.GRAVEL_GOLD.getStack))
  addItem(SiftingType.STICK, Blocks.GRAVEL, ItemChance(.12f * .09f, new ItemStack(Items.DIAMOND)))
  addItem(SiftingType.STICK, Blocks.GRAVEL, ItemChance(.12f * 1f, new ItemStack(Items.COAL)))
  for (_ <- 1 to 6) {
    addItem(SiftingType.STICK, Blocks.GRAVEL, ItemChance(.12f * .075f, new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage)))
  }
  addItem(SiftingType.STICK, Blocks.GRAVEL, ItemChance(.12f * .075f, new ItemStack(Items.EMERALD)))
  for (_ <- 1 to 5) {
    addItem(SiftingType.STICK, Blocks.GRAVEL, ItemChance(.12f * .7f, new ItemStack(Items.REDSTONE)))
  }
  addItem(SiftingType.STICK, Blocks.GRAVEL, ItemChance(.2f, new ItemStack(ItemPebble)))

  addItem(SiftingType.GRIND, Blocks.COBBLESTONE, ItemChance(1f, new ItemStack(Blocks.GRAVEL)))
  addItem(SiftingType.GRIND, Blocks.GRAVEL, ItemChance(1f, new ItemStack(Blocks.SAND)))

  def addItem(siftingType: SiftingType, block: Block, itemChance: ItemChance): Unit = {
    if (!map.contains(siftingType)) {
      map.put(siftingType, new mutable.HashMap[Block, mutable.MutableList[ItemChance]]())
    }
    var blockMap = map(siftingType)
    if (!blockMap.contains(block)) {
      blockMap.put(block, new mutable.MutableList[ItemChance])
    }
    blockMap(block) += itemChance
  }

  def getDrops(siftingType: SiftingType, block: Block): Option[Array[ItemStack]] = {
    val tr = new mutable.ArrayBuffer[ItemStack]()
    map.get(siftingType) match {
      case Some(blockMap) =>
        blockMap.get(block) match {
          case Some(x) => x.foreach((chance: ItemChance) => {
            val value = Random.nextFloat()
            if (value < chance.chance) {
              tr += chance.item.copy()
            }
          })
          case None => return None
        }
      case None => return None
    }
    Some(tr.toArray)
  }
}
