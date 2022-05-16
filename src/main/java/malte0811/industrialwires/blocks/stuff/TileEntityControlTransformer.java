package malte0811.industrialwires.blocks.stuff;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import malte0811.industrialwires.blocks.IBlockBoundsIW;
import malte0811.industrialwires.blocks.TileEntityIWBase;
import malte0811.industrialwires.blocks.IHasDummyBlocksIW;
import malte0811.industrialwires.blocks.IWProperties;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nonnull;

public class TileEntityControlTransformer extends TileEntityIWBase implements IHasDummyBlocksIW, IBlockBoundsIW, IDirectionalTile 
{  
          private int dummy = 0;
	  private static final String FACING = "facing";
	  EnumFacing facing = EnumFacing.NORTH;

        @Override
	public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
                dummy = nbt.getInteger("dummy");
	  	facing = EnumFacing.HORIZONTALS[nbt.getInteger("facing")];
	  	aabb = null;
   	}

	@Override
	public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
                nbt.setInteger("dummy", dummy);
	   	nbt.setInteger("facing", facing.getHorizontalIndex());
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
	public void placeDummies(IBlockState state)
	{
			for(int i = 1; i <= 1; i++)
			{
				world.setBlockState(pos.add(0, 0, i), state);
				((TileEntityControlTransformer)world.getTileEntity(pos.add(0, 0, i))).dummy = i;
				((TileEntityControlTransformer)world.getTileEntity(pos.add(0, 0, i))).facing = this.facing;
			}
	}
        @Override
	public void breakDummies()
	{
		for(int i = 0; i <= 1; i++)
                {
		       world.setBlockToAir(pos.offset(EnumFacing.WEST, i - dummy));
                }
	}

}
