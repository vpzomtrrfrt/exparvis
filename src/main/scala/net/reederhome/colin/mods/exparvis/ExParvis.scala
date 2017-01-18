package net.reederhome.colin.mods.exparvis

import com.google.common.base.Predicate
import net.minecraft.block.{Block, IGrowable}
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
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
import scala.util.control.Breaks

@Mod(name = ExParvis.NAME, modid = ExParvis.MODID, modLanguage = "scala")
object ExParvis {
  final val NAME = "Ex Parvis"
  final val MODID = "exparvis"

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
    val world = entity.world
    val data = entity.getEntityData
    val lastSneakKey = "LastTickSneaking"
    val range = 2

    if(world.isRemote) {
      return
    }

    if (data.hasKey(lastSneakKey) && data.getBoolean(lastSneakKey) != entity.isSneaking && Math.random() < 0.05) {
      var success = false
      Breaks.breakable {
        for (i <- 1 to 5) {
          val x = event.getEntity.posX + Random.nextGaussian() * range
          val y = event.getEntity.posY + Random.nextInt(3) - 1
          val z = event.getEntity.posZ + Random.nextGaussian() * range
          val pos = new BlockPos(x, y, z)
          val state = world.getBlockState(pos)
          val block = state.getBlock
          if (block.isInstanceOf[IGrowable]) {
            val growable = block.asInstanceOf[IGrowable]
            if (growable.canGrow(world, pos, state, world.isRemote) && growable.canUseBonemeal(world, Random.self, pos, state)) {
              growable.grow(world, new java.util.Random(), pos, state)
              success = true
              Breaks.break()
            }
          }
        }
      }
      if(!success) {
        println("Sorry")
      }
    }
    data.setBoolean(lastSneakKey, entity.isSneaking)
  }

  def getAge(input : EntityItem): Short = {
    val tag = new NBTTagCompound
    input.writeEntityToNBT(tag)
    tag.getShort("Age")
  }

  @SubscribeEvent
  def onTick(event: WorldTickEvent): Unit = {
    val numToCompost = 8
    val timeToCompost = 200

    val items: List[EntityItem] = event.world.getEntities[EntityItem](classOf[EntityItem], new Predicate[EntityItem] {

      override def apply(input: EntityItem): Boolean = {
        getAge(input) >= timeToCompost &&
          ((input.getEntityItem.getItem == Item.getItemFromBlock(Blocks.SAPLING) && input.getEntityItem.getCount >= numToCompost) ||
            input.getEntityItem.getItem == Items.BUCKET && event.world.isRainingAt(new BlockPos(input.posX, input.posY, input.posZ)))
      }
    }).asScala.toList
    for (item: EntityItem <- items) {
      val stack = item.getEntityItem
      var result: ItemStack = null
      if (stack.getItem == Items.BUCKET) {
        stack.shrink(1)
        result = new ItemStack(Items.WATER_BUCKET)
      }
      else {
        var composted = 0
        while (stack.getCount >= numToCompost) {
          stack.shrink(numToCompost)
          composted += 1
        }
        result = new ItemStack(Blocks.DIRT, composted)
      }
      val newItem = new EntityItem(event.world, item.posX, item.posY, item.posZ)
      newItem.setEntityItemStack(result)
      event.world.spawnEntity(newItem)
      if (stack.isEmpty) {
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
