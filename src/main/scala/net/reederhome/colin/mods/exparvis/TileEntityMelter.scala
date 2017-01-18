package net.reederhome.colin.mods.exparvis

import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.inventory.IInventory
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.text.{ITextComponent, TextComponentTranslation}
import net.minecraft.util.{EnumFacing, ITickable}
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.{Fluid, FluidRegistry, FluidStack, FluidTankInfo}
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, FluidTankProperties, IFluidHandler, IFluidTankProperties}

class TileEntityMelter extends TileEntity with IInventory with ITickable {
  val MAX_LAVA = 4000
  val MAX_STONE = 11000
  val RATIO = 4.0

  var extraCobble = false
  var lava = 0.0
  var stone = 0
  var lastSyncState: IBlockState = _

  override def decrStackSize(i: Int, i1: Int): ItemStack = null

  override def closeInventory(entityPlayer: EntityPlayer): Unit = {}

  override def getSizeInventory: Int = 1

  override def getInventoryStackLimit: Int = 1

  override def clear(): Unit = {
    extraCobble = false
  }

  override def isItemValidForSlot(i: Int, itemStack: ItemStack): Boolean = itemStack.getItem == Item.getItemFromBlock(Blocks.COBBLESTONE)

  override def openInventory(entityPlayer: EntityPlayer): Unit = {}

  override def getFieldCount: Int = 0

  override def getField(i: Int): Int = 0

  override def setInventorySlotContents(i: Int, itemStack: ItemStack): Unit = {
    extraCobble = isItemValidForSlot(i, itemStack) && itemStack.stackSize > 0
  }

  override def getStackInSlot(i: Int): ItemStack = if (extraCobble) new ItemStack(Blocks.COBBLESTONE) else null

  override def removeStackFromSlot(i: Int): ItemStack = null

  override def setField(i: Int, i1: Int): Unit = {}

  override def getDisplayName: ITextComponent = new TextComponentTranslation(getName)

  override def getName: String = "tile.melter.name"

  override def hasCustomName: Boolean = false

  override def hasCapability(p_hasCapability_1_ : Capability[_], p_hasCapability_2_ : EnumFacing): Boolean = p_hasCapability_1_ == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(p_hasCapability_1_, p_hasCapability_2_)

  override def getCapability[T](p_getCapability_1_ : Capability[T], p_getCapability_2_ : EnumFacing): T = {
    if(p_getCapability_1_ == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
      return Handler.asInstanceOf[T]
    }
    super.getCapability(p_getCapability_1_, p_getCapability_2_)
  }

  override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(nbt)
    nbt.setBoolean("ExtraCobble", extraCobble)
    nbt.setInteger("Stone", stone)
    nbt.setDouble("Lava", lava)
    nbt
  }

  override def readFromNBT(nbt: NBTTagCompound): Unit = {
    super.readFromNBT(nbt)
    extraCobble = nbt.getBoolean("ExtraCobble")
    stone = nbt.getInteger("Stone")
    lava = nbt.getInteger("Lava")
  }

  def getMeltSpeed: Int = {
    val state = world.getBlockState(getPos.down)
    state.getBlock match {
      case Blocks.TORCH => 9
      case Blocks.FIRE => 20
      case Blocks.FLOWING_LAVA => 40
      case Blocks.LAVA => 50
      case other => other.getLightValue(state) * 5
    }
  }

  override def update(): Unit = {
    if (world.isRemote) return
    if (stone > 0) {
      val speed = getMeltSpeed
      var tr = speed
      if (stone < speed) {
        tr = stone
      }
      if (lava + tr > MAX_LAVA) {
        tr = MAX_LAVA - lava.toInt
      }
      if (tr < 0) {
        tr = 0
      }
      lava += tr / RATIO
      stone -= tr
    }
    if (extraCobble && stone + 1000 <= MAX_STONE) {
      extraCobble = false
      stone += 1000
    }
    val newstate = getBlockState
    if (!ExParvis.statesEqual(newstate, lastSyncState)) {
      world.notifyBlockUpdate(pos, newstate, newstate, 3)
    }
  }

  def getEquivalentCobble: Int = {
    if (extraCobble) stone + 1000
    else stone
  }

  override def getUpdatePacket: SPacketUpdateTileEntity = {
    lastSyncState = getBlockState
    val tag = new NBTTagCompound
    writeToNBT(tag)
    new SPacketUpdateTileEntity(getPos, 1, tag)
  }

  override def onDataPacket(networkManager: NetworkManager, packet: SPacketUpdateTileEntity): Unit = {
    readFromNBT(packet.getNbtCompound)
    world.markBlockRangeForRenderUpdate(pos, pos)
  }

  def getBlockState: IBlockState = BlockMelter.getActualState(world.getBlockState(pos), world, pos)

  override def isUsableByPlayer(entityPlayer: EntityPlayer): Boolean = false

  object Handler extends IFluidHandler {
    override def drain(fluidStack: FluidStack, really: Boolean): FluidStack = {
      if (fluidStack.getFluid == FluidRegistry.LAVA) drain(fluidStack.amount, really)
      else new FluidStack(fluidStack.getFluid, 0)
    }

    override def drain(i: Int, really: Boolean): FluidStack = {
      var tr = i
      if (i > lava) tr = lava.toInt
      if (really) lava -= tr
      new FluidStack(FluidRegistry.LAVA, tr)
    }

    override def fill(fluidStack: FluidStack, really: Boolean): Int = 0

    override def getTankProperties: Array[IFluidTankProperties] = Array(new FluidTankProperties(new FluidStack(FluidRegistry.LAVA, lava.toInt), MAX_LAVA, false, lava > 0))
  }

}