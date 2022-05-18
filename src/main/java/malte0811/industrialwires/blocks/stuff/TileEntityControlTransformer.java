package malte0811.industrialwires.blocks.stuff;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.IBlockBoundsIW.IBlockBoundsDirectional;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import malte0811.industrialwires.blocks.TileEntityIWBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nonnull;

public class TileEntityControlTransformer extends TileEntityIWBase implements IBlockBoundsDirectional, IDirectionalTile {
	private static final String FACING = "facing";
	EnumFacing facing = EnumFacing.NORTH;
        private int dummy = 0;

	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		out.setByte(FACING, (byte) facing.getHorizontalIndex());
                out.setInteger(DUMY, dummy);
	}

	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		facing = EnumFacing.byHorizontalIndex(in.getByte(FACING));
		aabb = null;
                dummy = in.getInteger(DUMY);
	}
        
        @Override
	public boolean isDummy() {
		return dummy != 0;
	}

	@Override
	public void placeDummies(IBlockState state) {
		for (int i = 1; i <= 1; i++) {
			BlockPos pos2 = pos.offset(EnumFacing.WEST, i);
			world.setBlockState(pos2, state);
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
			if (i != dummy && world.getTileEntity(pos.offset(EnumFacing.WEST, i - dummy)) instanceof TileEntityControlTransformer) {
				world.setBlockToAir(pos.offset(EnumFacing.UP, i - dummy));
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
