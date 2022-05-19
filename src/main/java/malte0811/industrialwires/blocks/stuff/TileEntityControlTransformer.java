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


import javax.annotation.Nonnull;

public class TileEntityControlTransformer extends TileEntityIWBase implements ITickable, IHasDummyBlocksIW, IBlockBoundsDirectional, IDirectionalTile {
	private static final String FACING = "facing";
        private static final String DUMY = "dumyy";
	EnumFacing facing = EnumFacing.NORTH;
        private int dummy = 0;

        @Override
	public void update() {
		ApiUtils.checkForNeedlessTicking(this);
		if (isDummy()) {
			return;
		}
	}
        
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
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ) {
		world.setBlockState(pos.offset(facing), state);
		((TileEntityControlTransformer)world.getTileEntity(pos.offset(facing))).dummy = true;
		((TileEntityControlTransformer)world.getTileEntity(pos.offset(facing))).facing = facing;
	}

        @Override
	public void breakDummies() {
		for(int i = 0; i <= 1; i++)
			if(world.getTileEntity(getPos().offset(facing, dummy?-1: 0).offset(facing, i)) instanceof TileEntityControlTransformer)
				world.setBlockToAir(getPos().offset(facing, dummy?-1: 0).offset(facing, i));
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
