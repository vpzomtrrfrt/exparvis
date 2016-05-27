package net.reederhome.colin.mods.exparvis

import com.google.common.base.Predicate
import net.minecraft.block.{Block, IGrowable}
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.common.{Mod, SidedProxy}
import net.minecraftforge.oredict.ShapedOreRecipe

import scala.collection.JavaConverters._
import scala.util.Random

@Mod(name = ExParvis.NAME, modid = ExParvis.MODID, version = ExParvis.VERSION, modLanguage = "scala")
object ExParvis {
  final val NAME = "Ex Parvis"
  final val MODID = "exparvis"
  final val VERSION = "${version}"

  @SidedProxy(clientSide = "net.reederhome.colin.mods.exparvis.ClientProxy",
    serverSide = "net.reederhome.colin.mods.exparvis.ServerProxy")
  var proxy: CommonProxy = _

  @EventHandler
  def preInit(event: FMLPreInitializationEvent): Unit = {
    proxy.preInit()
    MinecraftForge.EVENT_BUS.register(this)

    GameRegistry.addRecipe(new ShapedOreRecipe(BlockMelter, "bbb", " b ", "bbb", 'b' : Character, "ingotBrick"))
  }

  @EventHandler
  def init(event: FMLInitializationEvent): Unit = {
    proxy.init()
  }


  @SubscribeEvent
  def onLivingUpdate(event: LivingUpdateEvent): Unit = {
    val entity = event.getEntityLiving
    val world = entity.worldObj
    val data = entity.getEntityData
    val lastSneakKey = "LastTickSneaking"
    val range = 1
    if (data.hasKey(lastSneakKey) && data.getBoolean(lastSneakKey) != entity.isSneaking && Math.random() < 0.02) {
      val x = event.getEntity.posX + Random.nextInt(range * 2 + 1) - range
      val y = event.getEntity.posY
      val z = event.getEntity.posZ + Random.nextInt(range * 2 + 1) - range
      val pos = new BlockPos(x, y, z)
      val state = world.getBlockState(pos)
      val block = state.getBlock
      if (block.isInstanceOf[IGrowable]) {
        val growable = block.asInstanceOf[IGrowable]
        if (!world.isRemote && growable.canGrow(world, pos, state, world.isRemote) && growable.canUseBonemeal(world, new java.util.Random(), pos, state)) {
          growable.grow(world, new java.util.Random(), pos, state)
        }
      }
    }
    data.setBoolean(lastSneakKey, entity.isSneaking)
  }


  @SubscribeEvent
  def onTick(event: WorldTickEvent): Unit = {
    val numToCompost = 8
    val timeToCompost = 200

    val items: List[EntityItem] = event.world.getEntities[EntityItem](classOf[EntityItem], new Predicate[EntityItem] {
      override def apply(input: EntityItem): Boolean = {
        input.getAge >= timeToCompost &&
          ((input.getEntityItem.getItem == Item.getItemFromBlock(Blocks.sapling) && input.getEntityItem.stackSize >= numToCompost) ||
            input.getEntityItem.getItem == Items.bucket && event.world.isRainingAt(new BlockPos(input.posX, input.posY, input.posZ)))
      }
    }).asScala.toList
    for (item: EntityItem <- items) {
      val stack = item.getEntityItem
      var result: ItemStack = null
      if (stack.getItem == Items.bucket) {
        stack.stackSize -= 1
        result = new ItemStack(Items.water_bucket)
      }
      else {
        var composted = 0
        while (stack.stackSize >= numToCompost) {
          stack.stackSize -= numToCompost
          composted += 1
        }
        result = new ItemStack(Blocks.dirt, composted)
      }
      val newItem = new EntityItem(event.world, item.posX, item.posY, item.posZ)
      newItem.setEntityItemStack(result)
      event.world.spawnEntityInWorld(newItem)
      if (stack.stackSize == 0) {
        item.setDead()
      }
    }
  }

  def statesEqual(state1: IBlockState, state2: IBlockState) : Boolean = {
    if(state1 == null || state2 == null) {
      return state1 == state2;
    }
    state1.toString.equals(state2.toString);
  }
}
