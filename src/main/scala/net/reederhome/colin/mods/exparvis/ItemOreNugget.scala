package net.reederhome.colin.mods.exparvis

import net.minecraft.item.{Item, ItemStack}

import scala.collection.mutable

object ItemOreNugget extends Item {
  setHasSubtypes(true)
  setMaxDamage(0)
  setUnlocalizedName("oreNugget")

  override def getUnlocalizedName(stack: ItemStack): String = {
    super.getUnlocalizedName(stack) + "." +
      (Type.map.get(stack.getMetadata) match {
        case Some(x) =>
          x.getName
        case None =>
          "invalid"
      })
  }

  def getColor(stack: ItemStack, layer: Int): Int = {
    if(layer == 1) {
      Type.map.get(stack.getMetadata) match {
        case Some(x) => return x.color
        case None =>
      }
    }
    0xFFFFFF
  }

  def getStack(typ: Type): ItemStack = new ItemStack(this, 1, typ.id)

  case class Type private(id: Int, base: String, resource: String, color: Int) {
    if (Type.map.contains(id)) {
      throw new IllegalArgumentException("Nugget type ID conflict: " + id + " used by " + getName + " and " + Type.map(id).getName)
    }
    Type.map.put(id, this)

    def getStack: ItemStack = ItemOreNugget.getStack(this)

    def getName: String = base+"_"+resource
  }

  object Type {
    def getTypes: Iterable[Type] = map.values

    val map = new mutable.HashMap[Int, Type]()

    val GRAVEL_IRON = Type(1, "gravel", "iron", 0xd8af93)
    val GRAVEL_GOLD = Type(2, "gravel", "gold", 0xfcee4b)
  }

}
