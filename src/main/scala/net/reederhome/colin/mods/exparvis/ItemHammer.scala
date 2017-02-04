package net.reederhome.colin.mods.exparvis

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item.ToolMaterial
import net.minecraft.item.ItemPickaxe

class ItemHammer(toolMaterial: ToolMaterial) extends ItemPickaxe(toolMaterial) {
  setUnlocalizedName("hammer." + toolMaterial.name())
  setCreativeTab(CreativeTabs.TOOLS)
}

object ItemHammer {
  val Stone = new ItemHammer(ToolMaterial.STONE)
  val Iron = new ItemHammer(ToolMaterial.IRON)
}