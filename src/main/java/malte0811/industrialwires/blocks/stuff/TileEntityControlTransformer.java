package malte0811.industrialwires.blocks.stuff;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import malte0811.industrialwires.blocks.IBlockBoundsIW.IBlockBoundsDirectional;
import malte0811.industrialwires.blocks.TileEntityIWBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nonnull;

public class TileEntityControlTransformer extends TileEntityIWBase implements IPlayerInteraction, IMarxTarget,
		IBlockBoundsDirectional, IDirectionalTile 
{  
	  private static final String FACING = "facing";
	  EnumFacing facing = EnumFacing.NORTH;

	  @Override
	  public void writeNBT(NBTTagCompound out, boolean updatePacket) {
	   	out.setByte(FACING, (byte) facing.getHorizontalIndex());
  	}

  	@Override
	  public void readNBT(NBTTagCompound in, boolean updatePacket) {;
	  	facing = EnumFacing.byHorizontalIndex(in.getByte(FACING));
	  	aabb = null;
   	}


  //NEED FINISH UNDER
	AxisAlignedBB aabb = null;
	@Override
	public AxisAlignedBB getBoundingBoxNoRot() {
		return new AxisAlignedBB(1F/16, 0, 5F/16,
					10F/16, (hasWire?15F:14F)/16, 11F/16);
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
