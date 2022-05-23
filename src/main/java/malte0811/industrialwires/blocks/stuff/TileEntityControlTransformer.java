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

public class TileEntityControlTransformer extends TileEntityImmersiveConnectable implements ITickable, IHasDummyBlocksIW, IIEInternalFluxHandler, IBlockBoundsDirectional, IDirectionalTile  
{
// VARIABLES/CONS.: --------------------------------------
        private static final String FACING = "facing";
        private static final String DUMY = "dummys";
        private static final String SOUTH = "south";
        private static final String NORTH = "north";
        private static final String EAST = "east";
        private static final String WEST = "west";
        EnumFacing facing = EnumFacing.NORTH;
        private int dummy = 0;

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
