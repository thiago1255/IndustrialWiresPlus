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
          private int dummy = 0;
	  private static final String FACING = "facing";
	  EnumFacing facing = EnumFacing.NORTH;

	  @Override
	  public void writeNBT(NBTTagCompound out, boolean updatePacket) {
                nbt.setInteger("dummy", dummy);
	   	out.setByte(FACING, (byte) facing.getHorizontalIndex());
  	}

  	@Override
	  public void readNBT(NBTTagCompound in, boolean updatePacket) {;
                dummy = nbt.getInteger("dummy");
	  	facing = EnumFacing.byHorizontalIndex(in.getByte(FACING));
	  	aabb = null;
   	}

	AxisAlignedBB aabb = null;
	@Override
	public AxisAlignedBB getBoundingBoxNoRot() {
		return new AxisAlignedBB(0, 0, 0, 1, 1, 2);
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
        
        @Override
	public boolean isDummy()
	{
		return dummy!=0;
	}

        @Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
			for(int i = 1; i <= 1; i++)
			{
				world.setBlockState(pos.add(0, 0, i), state);
				((TileEntityControlTransformer)world.getTileEntity(pos.add(0, 0, i))).dummy = i;
				((TileEntityControlTransformer)world.getTileEntity(pos.add(0, 0, i))).facing = this.facing;
			}
	}
        @Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		for(int i = 0; i <= 1; i++)
                {
		       world.setBlockToAir(getPos().add(0, 0, -dummy).add(0, 0, i));
                }
	}

}
