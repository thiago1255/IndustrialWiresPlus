/*
|| (do what u want with this, but give credits to:)
|| File made by thiago1255 based (copied a lot) of files of mods 'Industrial Wires', and 'Immersive Engineering'.
||
|| (check github for credits of this mods:)
|| IW: https://github.com/malte0811/IndustrialWires
|| IE: https://github.com/BluSunrize/ImmersiveEngineering
*/

package malte0811.industrialwires.blocks.stuff;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;

import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.IBlockBoundsIW.IBlockBoundsDirectional;
import malte0811.industrialwires.blocks.IHasDummyBlocksIW;
import malte0811.industrialwires.IndustrialWires;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.*;

public class TileEntityControlTransformer extends TileEntityImmersiveConnectable implements ITickable, IHasDummyBlocksIW, IBlockBoundsDirectional, IDirectionalTile  
{
// VARIABLES/CONS.: --------------------------------------
        private static final String SOUTH = "south";
        private static final String NORTH = "north";
        private static final String EAST = "east";
        private static final String WEST = "west";
        EnumFacing facing = EnumFacing.NORTH;
        private int dummy = 0;
        private int redstonevalue = 0;
        private int maxvalue = 0;

// NBT DATA: --------------------------------------
        @Override
	public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
                facing = EnumFacing.byHorizontalIndex(nbt.getByte("facing"));
                dummy = nbt.getInteger("dummys");
        }
        
        @Override
	public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		nbt.setByte("facing",  (byte) facing.getHorizontalIndex());
                nbt.setInteger("dummys", dummy);
        }

// ITICKABLE: --------------------------------------
        @Override
 	public void update() {
		if (isDummy() || world.isRemote) {
		    return;
		}

                redstonevalue = world.getRedstonePowerFromNeighbors(pos);  
                      
                maxvalue = ((redstonevalue + 1)*2048); 
                //HV: 32768 cable 4096 conector
                /*
                divided by 16:
                    redstone | max value
                            0: 2048
                            1: 4096
                            2: 6144
                            3: 8192
                            4: 10240
		            5: 12288
		            6: 14336
		            7: 16384
		            8: 18432
		            9: 20480
	                   10: 22528
		           11: 24576
                           12: 26624
		           13: 28672
		           14: 30720
		           15: 32768
                */
	}

// DUMMY BLOCKS: --------------------------------------
        @Override
	public boolean isDummy() { return dummy != 0; }

        @Override
	public void placeDummies(IBlockState state) {
             BlockPos pos2 = pos.offset(EnumFacing.WEST, i);
             switch (facing) {
		case SOUTH: pos2 = pos.offset(EnumFacing.WEST, i); break;
                case NORTH: pos2 = pos.offset(EnumFacing.EAST, i); break;
		case EAST: pos2 = pos.offset(EnumFacing.SOUTH, i); break;
                case WEST: pos2 = pos.offset(EnumFacing.NORTH, i); break;
	     }
             world.setBlockState(pos2, state);
             TileEntity te = world.getTileEntity(pos2);
	     if (te instanceof TileEntityControlTransformer) {
	         ((TileEntityControlTransformer) te).dummy = i;
		 ((TileEntityControlTransformer) te).facing = facing;
             }
	}

        @Override
	public void breakDummies() {
	    for (int i = 0; i <= 1; i++) {
                switch (facing) {
                    case SOUTH: if (i != dummy && world.getTileEntity(pos.offset(EnumFacing.WEST, i - dummy)) instanceof TileEntityControlTransformer) { world.setBlockToAir(pos.offset(EnumFacing.WEST, i - dummy)); } break;
                    case NORTH: if (i != dummy && world.getTileEntity(pos.offset(EnumFacing.EAST, i - dummy)) instanceof TileEntityControlTransformer) { world.setBlockToAir(pos.offset(EnumFacing.EAST, i - dummy)); } break;
	            case EAST: if (i != dummy && world.getTileEntity(pos.offset(EnumFacing.SOUTH, i - dummy)) instanceof TileEntityControlTransformer) { world.setBlockToAir(pos.offset(EnumFacing.SOUTH, i - dummy)); } break;
                    case WEST: if (i != dummy && world.getTileEntity(pos.offset(EnumFacing.NORTH, i - dummy)) instanceof TileEntityControlTransformer) { world.setBlockToAir(pos.offset(EnumFacing.NORTH, i - dummy)); } break;
                }
            }
       }

// GENERAL PROPERTYES: --------------------------------------       
       AxisAlignedBB aabb = null;
       @Override
       public AxisAlignedBB getBoundingBoxNoRot() { return new AxisAlignedBB(0, 0, 0, 1, 1, 1); }

       @Override
       public AxisAlignedBB getBoundingBox() 
       {
           if (aabb==null) { aabb = IBlockBoundsDirectional.super.getBoundingBox(); }
	   return aabb;
       }

       @Nonnull
       @Override
       public EnumFacing getFacing() { return facing; }

       @Override
       public void setFacing(@Nonnull EnumFacing facing) { this.facing = facing; }

       @Override
       public int getFacingLimitation() { return 2; }

       @Override
       public boolean mirrorFacingOnPlacement(@Nonnull EntityLivingBase placer) { return false; }

       @Override
       public boolean canHammerRotate(@Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EntityLivingBase entity) { return false; }

       @Override
       public boolean canRotate(@Nonnull EnumFacing axis) { return false; }

}
