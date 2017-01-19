package net.reederhome.colin.mods.exparvis

import net.minecraft.block.material.Material
import net.minecraft.block.state.{BlockStateContainer, IBlockState}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.{EnumFacing, EnumHand}
import net.minecraft.world.{IBlockAccess, World}

object BlockMelter extends BlockModelContainer(Material.ROCK) {
  setDefaultState(blockState.getBaseState.withProperty(Properties.LAVA_LEVEL, 0: Integer).withProperty(Properties.STONE_LEVEL, 0: Integer))
  setUnlocalizedName("melter")
  setCreativeTab(CreativeTabs.MISC)

  override def createBlockState(): BlockStateContainer = new BlockStateContainer(this, Properties.LAVA_LEVEL, Properties.STONE_LEVEL)

  override def createNewTileEntity(world: World, i: Int): TileEntity = new TileEntityMelter

  override def onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, p_onBlockActivated_6_ : ItemStack, p_onBlockActivated_7_ : EnumFacing, p_onBlockActivated_8_ : Float, p_onBlockActivated_9_ : Float, p_onBlockActivated_10_ : Float): Boolean = {
    if (world.isRemote) return true
    val held = player.getHeldItem(hand)
    val te = world.getTileEntity(pos).asInstanceOf[TileEntityMelter]
    if (held != null && held.getItem == Item.getItemFromBlock(Blocks.COBBLESTONE) && !te.extraCobble) {
      te.extraCobble = true
      held.stackSize -= 1
    }
    else if (held != null && held.getItem == Items.BUCKET && te.lava > 1000) {
      te.lava -= 1000
      held.stackSize -= 1
      if(!player.inventory.addItemStackToInventory(new ItemStack(Items.LAVA_BUCKET))) {
        player.dropItem(Items.LAVA_BUCKET, 1)
      }
    }
    else {
      player.sendMessage(new TextComponentTranslation("tile.melter.status", new Integer(te.getEquivalentCobble), new Integer(te.lava.toInt), new Integer(te.getMeltSpeed)))
      if(te.getMeltSpeed == 0) {
        player.sendMessage(new TextComponentTranslation("tile.melter.advice1"))
      }
    }
    true
  }

  override def getActualState(state: IBlockState, world: IBlockAccess, pos: BlockPos): IBlockState = {
    val te = world.getTileEntity(pos).asInstanceOf[TileEntityMelter]
    val tr = state.withProperty(Properties.STONE_LEVEL, Math.min(12, te.getEquivalentCobble * 12 / (te.MAX_STONE + 1000)): Integer).withProperty(Properties.LAVA_LEVEL, Math.min(12, (te.lava * 12 / te.MAX_LAVA).toInt): Integer)
    tr
  }

  override def getMetaFromState(p_getMetaFromState_1_ : IBlockState): Int = 0

  override def getStateFromMeta(p_getStateFromMeta_1_ : Int): IBlockState = getDefaultState

  override def isOpaqueCube(p_isOpaqueCube_1_ : IBlockState): Boolean = false
}