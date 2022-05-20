package malte0811.industrialwires.blocks.stuff;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.IBlockBoundsIW.IBlockBoundsDirectional;
import malte0811.industrialwires.blocks.IHasDummyBlocksIW;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import malte0811.industrialwires.blocks.TileEntityIWBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ITickable;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;

import javax.annotation.Nonnull;

public class TileEntityControlTransformer extends TileEntityIWBase implements ITickable, IHasDummyBlocksIW, IPlayerInteraction, IBlockBoundsDirectional, IDirectionalTile {
	private static final String FACING = "facing";
        private static final String DUMY = "dummys";
        private static final String RSV = "rsvalue";
	EnumFacing facing = EnumFacing.NORTH;
        private int dummy = 0;
        private int redstonevalue = 0;        

        @Override
	public void update() {
		ApiUtils.checkForNeedlessTicking(this);
		if (isDummy()) {
			return;
		}
                redstonevalue = world.getRedstonePowerFromNeighbors(pos);
	}
        
	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		out.setByte(FACING, (byte) facing.getHorizontalIndex());
                out.setInteger(DUMY, dummy);
                out.setInteger(RSV, redstonevalue);
	}

	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		facing = EnumFacing.byHorizontalIndex(in.getByte(FACING));
		aabb = null;
                dummy = in.getInteger(DUMY);
                redstonevalue = in.getInteger(RSV);
	}

        @Override
	public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand,
							@Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) 
                {
				player.sendMessage(redstonevalue);
		}
		return true;
	}

        @Override
	public boolean isDummy() {
		return dummy != 0;
	}

        @Override
	public void placeDummies(IBlockState state) {
		for (int i = 1; i <= 1; i++) {
                        switch (facing) {
			       case south:
			              BlockPos pos2 = pos.offset(EnumFacing.WEST, i);
			              world.setBlockState(pos2, state);
                                      break;
                               case north:
			              BlockPos pos2 = pos.offset(EnumFacing.EAST, i);
			              world.setBlockState(pos2, state);
                                      break;
			       case east:
			              BlockPos pos2 = pos.offset(EnumFacing.SOUTH, i);
			              world.setBlockState(pos2, state);
                                      break;
                               case west:
			              BlockPos pos2 = pos.offset(EnumFacing.NORTH, i);
			              world.setBlockState(pos2, state);
                                      break;
			}
			TileEntity te = world.getTileEntity(pos2);
			if (te instanceof TileEntityControlTransformer) {
				((TileEntityControlTransformer) te).dummy = i;
				((TileEntityControlTransformer) te).facing = facing;
			}
		}
	}

        @Override
	public void breakDummies() {
		for (int i = 0; i <= 1; i++) {
                        switch (facing) {
			       case south:
			              if (i != dummy && world.getTileEntity(pos.offset(EnumFacing.WEST, i - dummy)) instanceof TileEntityControlTransformer) {
				             world.setBlockToAir(pos.offset(EnumFacing.WEST, i - dummy));
			              }
                                      break;
                               case north:
			              if (i != dummy && world.getTileEntity(pos.offset(EnumFacing.EAST, i - dummy)) instanceof TileEntityControlTransformer) {
				             world.setBlockToAir(pos.offset(EnumFacing.EAST, i - dummy));
			              }
                                      break;
			       case east:
			              if (i != dummy && world.getTileEntity(pos.offset(EnumFacing.SOUTH, i - dummy)) instanceof TileEntityControlTransformer) {
				             world.setBlockToAir(pos.offset(EnumFacing.SOUTH, i - dummy));
			              }
                                      break;
                               case west:
			              if (i != dummy && world.getTileEntity(pos.offset(EnumFacing.NORTH, i - dummy)) instanceof TileEntityControlTransformer) {
				             world.setBlockToAir(pos.offset(EnumFacing.NORTH, i - dummy));
			              }
                                      break;
			}
		}
	}

	AxisAlignedBB aabb = null;
	@Override
	public AxisAlignedBB getBoundingBoxNoRot() {
		return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		if (aabb==null) {
			aabb = IBlockBoundsDirectional.super.getBoundingBox();
		}
		return aabb;
	}

	@Nonnull
	@Override
	public EnumFacing getFacing() {
		return facing;
	}

	@Override
	public void setFacing(@Nonnull EnumFacing facing) {
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation() {
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(@Nonnull EntityLivingBase placer) {
		return false;
	}

	@Override
	public boolean canHammerRotate(@Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EntityLivingBase entity) {
		return false;
	}

	@Override
	public boolean canRotate(@Nonnull EnumFacing axis) {
		return false;
	}
}
