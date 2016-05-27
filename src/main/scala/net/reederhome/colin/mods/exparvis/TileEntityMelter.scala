package net.reederhome.colin.mods.exparvis

import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.inventory.IInventory
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.{NetworkManager, Packet}
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{EnumFacing, ITickable}
import net.minecraft.util.text.{ITextComponent, TextComponentTranslation}
import net.minecraftforge.fluids._

class TileEntityMelter extends TileEntity with IInventory with IFluidHandler with ITickable {
  val MAX_LAVA = 4000
  val MAX_STONE = 11000
  val RATIO = 4.0

  var extraCobble = false
  var lava = 0.0
  var stone = 0
  var lastSyncState : IBlockState = _

  override def decrStackSize(i: Int, i1: Int): ItemStack = null

  override def closeInventory(entityPlayer: EntityPlayer): Unit = {}

  override def getSizeInventory: Int = 1

  override def getInventoryStackLimit: Int = 1

  override def clear(): Unit = {
    extraCobble = false
  }

  override def isItemValidForSlot(i: Int, itemStack: ItemStack): Boolean = itemStack.getItem == Item.getItemFromBlock(Blocks.cobblestone)

  override def openInventory(entityPlayer: EntityPlayer): Unit = {}

  override def getFieldCount: Int = 0

  override def getField(i: Int): Int = 0

  override def setInventorySlotContents(i: Int, itemStack: ItemStack): Unit = {
    extraCobble = isItemValidForSlot(i, itemStack) && itemStack.stackSize > 0
  }

  override def isUseableByPlayer(entityPlayer: EntityPlayer): Boolean = false

  override def getStackInSlot(i: Int): ItemStack = if (extraCobble) new ItemStack(Blocks.cobblestone) else null

  override def removeStackFromSlot(i: Int): ItemStack = null

  override def setField(i: Int, i1: Int): Unit = {}

  override def getDisplayName: ITextComponent = new TextComponentTranslation(getName)

  override def getName: String = "tile.melter.name"

  override def hasCustomName: Boolean = false

  override def drain(enumFacing: EnumFacing, fluidStack: FluidStack, really: Boolean): FluidStack = {
    if (fluidStack.getFluid == FluidRegistry.LAVA) drain(enumFacing, fluidStack.amount, really)
    else new FluidStack(fluidStack.getFluid, 0)
  }

  override def drain(enumFacing: EnumFacing, i: Int, really: Boolean): FluidStack = {
    var tr = i
    if (i > lava) tr = lava.toInt
    if (really) lava -= tr
    new FluidStack(FluidRegistry.LAVA, tr)
  }

  override def canFill(enumFacing: EnumFacing, fluid: Fluid): Boolean = false

  override def canDrain(enumFacing: EnumFacing, fluid: Fluid): Boolean = fluid == FluidRegistry.LAVA

  override def fill(enumFacing: EnumFacing, fluidStack: FluidStack, really: Boolean): Int = 0

  override def getTankInfo(enumFacing: EnumFacing): Array[FluidTankInfo] = Array(new FluidTankInfo(new FluidStack(FluidRegistry.LAVA, lava.toInt), MAX_LAVA))

  override def writeToNBT(nbt: NBTTagCompound): Unit = {
    super.writeToNBT(nbt)
    nbt.setBoolean("ExtraCobble", extraCobble)
    nbt.setInteger("Stone", stone)
    nbt.setDouble("Lava", lava)
  }

  override def readFromNBT(nbt: NBTTagCompound): Unit = {
    super.readFromNBT(nbt)
    extraCobble = nbt.getBoolean("ExtraCobble")
    stone = nbt.getInteger("Stone")
    lava = nbt.getInteger("Lava")
  }

  def getMeltSpeed: Int = {
    val state = worldObj.getBlockState(getPos.down)
    state.getBlock match {
      case Blocks.torch => 9
      case Blocks.fire => 20
      case Blocks.flowing_lava => 40
      case Blocks.lava => 50
      case other => other.getLightValue(state) * 5
    }
  }

  override def update(): Unit = {
    if(worldObj.isRemote) return
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
    if(!ExParvis.statesEqual(newstate, lastSyncState)) {
      worldObj.notifyBlockUpdate(pos, newstate, newstate, 3)
    }
  }

  def getEquivalentCobble: Int = {
    if (extraCobble) stone + 1000
    else stone
  }

  override def getDescriptionPacket: Packet[_] = {
    lastSyncState = getBlockState
    val tag = new NBTTagCompound
    writeToNBT(tag)
    new SPacketUpdateTileEntity(getPos, 1, tag)
  }

  override def onDataPacket(networkManager: NetworkManager, packet: SPacketUpdateTileEntity): Unit = {
    readFromNBT(packet.getNbtCompound)
    worldObj.markBlockRangeForRenderUpdate(pos, pos)
  }

  def getBlockState : IBlockState = BlockMelter.getActualState(worldObj.getBlockState(pos), worldObj, pos)
}