package net.reederhome.colin.mods.exparvis

import java.util

import com.google.common.base.Predicate
import net.minecraft.block.IGrowable
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.crafting.{FurnaceRecipes, IRecipe}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumHand, ResourceLocation}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.common.{Mod, SidedProxy}
import net.minecraftforge.oredict.{OreDictionary, ShapedOreRecipe}

import scala.collection.JavaConverters._
import scala.util.Random
import scala.util.control.Breaks

@Mod(name = ExParvis.NAME, modid = ExParvis.MODID, modLanguage = "scala")
object ExParvis {
  final val NAME = "Ex Parvis"
  final val MODID = "exparvis"
  
  final val RECIPE_GROUP = new ResourceLocation(MODID, "recipes")

  var configuration: Configuration = _

  @SidedProxy(clientSide = "net.reederhome.colin.mods.exparvis.ClientProxy",
    serverSide = "net.reederhome.colin.mods.exparvis.ServerProxy")
  var proxy: CommonProxy = _

  var enableCompost: Boolean = true
  var compostTime: Int = 200
  var compostCount: Int = 8

  var enableRainFill: Boolean = true
  var rainFillTime: Int = 200

  var enableSneakGrow: Boolean = true

  var enableStickSift: Boolean = true

  @EventHandler
  def preInit(event: FMLPreInitializationEvent): Unit = {
    proxy.preInit()
    MinecraftForge.EVENT_BUS.register(this)
    MinecraftForge.EVENT_BUS.register(proxy)

    configuration = new Configuration(event.getSuggestedConfigurationFile)

    configuration.load()

    enableCompost = configuration.get("Compost", "Enabled", enableCompost, "Whether dropped saplings should turn into dirt").getBoolean
    compostTime = configuration.get("Compost", "Time", compostTime, "Ticks taken for saplings to compost").getInt
    compostCount = configuration.get("Compost", "Count", compostCount, "Number of saplings required for each dirt block").getInt

    enableRainFill = configuration.get("BucketFill", "Enabled", enableRainFill, "Whether buckets in rain should fill with water").getBoolean
    rainFillTime = configuration.get("BucketFill", "Time", rainFillTime, "Ticks taken for buckets to fill in rain").getInt

    enableSneakGrow = configuration.get("SneakGrow", "Enabled", enableSneakGrow, "Whether players sneaking should increase crop growth speed").getBoolean

    enableStickSift = configuration.get("Sifting", "Enabled", enableStickSift, "Whether sticks should be able to sift for materials").getBoolean

    configuration.save()
  }

  @EventHandler
  def init(event: FMLInitializationEvent): Unit = {
    proxy.init()
    ItemOreNugget.Type.getTypes.foreach((f) => {
      FurnaceRecipes.instance().addSmeltingRecipeForBlock(BlockNuggetOre.getBlock(f.id), new ItemStack(f.resourceItem), 1f)
    })
  }

  @SubscribeEvent
  def onRegistryEvent(event: RegistryEvent.Register[IRecipe]) {
    event.getRegistry.registerAll(
      new ShapedOreRecipe(RECIPE_GROUP, BlockMelter, "bbb", " b ", "bbb", 'b': Character, "ingotBrick")
        .setRegistryName(MODID, "recipe_melter"),
      new ShapedOreRecipe(RECIPE_GROUP, Blocks.COBBLESTONE, "pp", "pp", 'p': Character, ItemPebble)
        .setRegistryName(MODID, "recipe_cobble_pebbles"),
      new ShapedOreRecipe(RECIPE_GROUP, ItemHammer.Stone, "mmm", "msm", " s ", 'm': Character, "cobblestone", 's': Character, "stickWood")
        .setRegistryName(MODID, "recipe_hammerStone"),
      new ShapedOreRecipe(RECIPE_GROUP, ItemHammer.Iron, "mmm", "msm", " s ", 'm': Character, "ingotIron", 's': Character, "stickWood")
        .setRegistryName(MODID, "recipe_hammerIron"))

    ItemOreNugget.Type.getTypes.foreach((f) => {
      event.getRegistry.register(new ShapedOreRecipe(RECIPE_GROUP, new ItemStack(BlockNuggetOre.getBlock(f.id)), "mm", "mm", 'm': Character, ItemOreNugget.getStack(f))
        .setRegistryName(MODID, "recipe_nuggetOre_" + f.resource))
    })
  }

  @SubscribeEvent
  def onLivingUpdate(event: LivingUpdateEvent): Unit = {
    if(enableSneakGrow) {
      val entity = event.getEntityLiving
      val world = entity.world
      val data = entity.getEntityData
      val lastSneakKey = "LastTickSneaking"
      val range = 2

      if (world.isRemote) {
        return
      }

      if (data.hasKey(lastSneakKey) && data.getBoolean(lastSneakKey) != entity.isSneaking && Math.random() < 0.03) {
        var success = false
        Breaks.breakable {
          for (i <- 1 to 5) {
            val x = event.getEntity.posX + Random.nextGaussian() * range
            val y = event.getEntity.posY + Random.nextInt(3) - 1
            val z = event.getEntity.posZ + Random.nextGaussian() * range
            val pos = new BlockPos(x, y, z)
            val state = world.getBlockState(pos)
            val block = state.getBlock
            if (block.isInstanceOf[IGrowable] && block != Blocks.GRASS) {
              val growable = block.asInstanceOf[IGrowable]
              if (growable.canGrow(world, pos, state, world.isRemote) && growable.canUseBonemeal(world, Random.self, pos, state)) {
                growable.grow(world, new java.util.Random(), pos, state)
                success = true
                world.playEvent(2005, pos, 0)
                Breaks.break()
              }
            }
          }
        }
      }
      data.setBoolean(lastSneakKey, entity.isSneaking)
    }
  }

  @SubscribeEvent
  def onBlockDrops(event: HarvestDropsEvent): Unit = {
    if (event.getHarvester != null) {
      val sneaking = event.getHarvester.isSneaking

      def applyDrops(siftingType: SiftingType): Unit = {
        SiftingRecipes.getDrops(siftingType, event.getState.getBlock) match {
          case Some(x) =>
            while (event.getDrops.size() > 0) {
              event.getDrops.remove(0)
            }
            event.getDrops.addAll(util.Arrays.asList(x: _*))
          case None =>
        }
      }

      val heldItem = event.getHarvester.getHeldItem(EnumHand.MAIN_HAND)
      if (heldItem != null) {
        if (enableStickSift && sneaking && OreDictionary.itemMatches(new ItemStack(Items.STICK), heldItem, false)) {
          // it's a stick
          applyDrops(SiftingType.STICK)
        }
        else if (heldItem != null && heldItem.getItem.isInstanceOf[ItemHammer]) {
          applyDrops(SiftingType.GRIND)
        }
      }
    }
  }

  def getAge(input: EntityItem): Short = {
    val tag = new NBTTagCompound
    input.writeEntityToNBT(tag)
    tag.getShort("Age")
  }

  @SubscribeEvent
  def onTick(event: WorldTickEvent): Unit = {
    val items: List[EntityItem] = event.world.getEntities[EntityItem](classOf[EntityItem], new Predicate[EntityItem] {

      override def apply(input: EntityItem): Boolean = {
        val age = getAge(input)
        if(enableRainFill && age >= rainFillTime && input.getEntityItem.getItem == Items.BUCKET) {
          event.world.isRainingAt(new BlockPos(input.posX, input.posY, input.posZ))
        } else if(age >= compostTime && enableCompost && input.getEntityItem.getItem == Item.getItemFromBlock(Blocks.SAPLING)) {
          input.getEntityItem.getCount >= compostCount
        } else {
          false
        }
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
        while (stack.getCount >= compostCount) {
          stack.shrink(compostCount)
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

  def statesEqual(state1: IBlockState, state2: IBlockState): Boolean = {
    if (state1 == null || state2 == null) {
      return state1 == state2
    }
    state1.toString.equals(state2.toString)
  }
}
