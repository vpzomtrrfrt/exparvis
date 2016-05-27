package net.reederhome.colin.mods.exparvis

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.item.Item

import scala.collection.mutable.ArrayBuffer

class ClientProxy extends CommonProxy {
  var items: ArrayBuffer[Item] = new ArrayBuffer[Item]

  override def registerItem(item: Item, name: String): Unit = {
    super.registerItem(item, name)
    items.append(item)
  }

  override def init(): Unit = {
    super.init()
    for(item <- items) {
      val loc = item.getRegistryName
      Minecraft.getMinecraft.getRenderItem.getItemModelMesher.register(item, 0, new ModelResourceLocation(loc.getResourceDomain+":"+loc.getResourcePath, "inventory"))
    }
  }
}
