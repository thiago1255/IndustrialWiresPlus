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
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.ImmersiveEngineering;

import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.IBlockBoundsIW.IBlockBoundsDirectional;
import malte0811.industrialwires.blocks.IHasDummyBlocksIW;
import malte0811.industrialwires.IndustrialWires;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.*;
import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;

import static blusunrize.immersiveengineering.api.energy.wires.WireType.LV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.MV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.HV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.REDSTONE_CATEGORY;

public class TileEntityControlTransformer extends TileEntityImmersiveConnectable implements ITickable, IIEInternalFluxHandler, IHasDummyBlocksIW, IBlockBoundsDirectional, IDirectionalTile  
{
// VARIABLES/CONS.: --------------------------------------
        private static final String SOUTH = "south";
        private static final String NORTH = "north";
        private static final String EAST = "east";
        private static final String WEST = "west";
        EnumFacing facing = EnumFacing.NORTH;
        public BlockPos endOfLeftConnection = null;
        private int dummy = 0;
        private int redstonevalue = 0;
        private int maxvalue = 0;
	private int wires = 0;
        private int i = 0;
        private FluxStorage energyStorage = new FluxStorage(getMaxStorage(), getMaxInput(), getMaxOutput());

// NBT DATA: --------------------------------------
        @Override
	public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
                facing = EnumFacing.byHorizontalIndex(nbt.getByte("facing"));
                dummy = nbt.getInteger("dummys");
                wires = nbt.getInteger("wires");
                energyStorage.readFromNBT(nbt);
        }
        
        @Override
	public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		nbt.setByte("facing",  (byte) facing.getHorizontalIndex());
                nbt.setInteger("dummys", dummy);
                nbt.setInteger("wires", wires);
		energyStorage.writeToNBT(nbt);
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

//WIRE STUFF: --------------------------------------
        @Override
        protected boolean canTakeLV() { return false; }
        
        @Override
	protected boolean canTakeMV() { return false; }

        @Override
	protected boolean canTakeHV() { return true; }
 
        @Override
	protected boolean isRelay() { return false; }

        @Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset)
	{
		if(!cableType.isEnergyWire()) { return false; }
		if(MV_CATEGORY.equals(cableType.getCategory())&&!canTakeMV()) { return false; }
		if(LV_CATEGORY.equals(cableType.getCategory())&&!canTakeLV()) { return false; }
		if(wires >= 2) { return false; }
		return limitType==null||WireApi.canMix(cableType, limitType);
	}

        @Override
	public WireType getCableLimiter(TargetingInfo target) { return limitType; }

        @Override
	public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other)
	{
		if(this.limitType==null) { this.limitType = cableType; }
		wires++;
		onConnectionChange();
	}

        @Override 
	public void removeCable(Connection connection)
	{
		WireType type = connection!=null?connection.cableType: null;
		if(type==null) { wires = 0; }
		else { wires--; }

		if(wires <= 0) { limitType = null; }
		onConnectionChange();
	}

        protected void onConnectionChange()
	{
		if(world!=null&&world.isRemote) {
			endOfLeftConnection = null;
			ImmersiveEngineering.proxy.clearConnectionModelCache();
			// reset cached connection vertices
			Set<Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
			if(conns!=null) {
				for(Connection c : conns)
				{
					c.catenaryVertices = null;
					world.markBlockRangeForRenderUpdate(c.end, c.end);
					Set<Connection> connsThere = ImmersiveNetHandler.INSTANCE.getConnections(world, c.end);
					if(connsThere!=null) { for(Connection c2 : connsThere) { if(c2.end.equals(pos)) { c2.catenaryVertices = null; } } }
				}
                        }
		}
		if(world!=null) { markContainingBlockForUpdate(null); }
	}

        @Override
	public boolean moveConnectionTo(Connection c, BlockPos newEnd) {
		if(c.end.equals(endOfLeftConnection)) { endOfLeftConnection = newEnd; }
		return true;
	}

        @Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(super.receiveClientEvent(id, arg)) { return true; }
		// IDK WHAT IS: this.active = id==1;
		this.markContainingBlockForUpdate(null);
		return true;
	}

        @Override
	public Vec3d getConnectionOffset(Connection con)
	{
                /*
		Matrix4 mat = new Matrix4(facing);
		mat.translate(.5, .5, 0).rotate(Math.PI/2*rotation, 0, 0, 1).translate(-.5, -.5, 0);
		if(endOfLeftConnection==null)
			calculateLeftConn(mat);
		boolean isLeft = con.end.equals(endOfLeftConnection)||con.start.equals(endOfLeftConnection);
		Vec3d ret = mat.apply(new Vec3d(isLeft?.25: .75, .5, .125));
		return ret;
                */
                return new Vec3d(0.5, 1.5, 0.5);
	}
//ENERGY STRG: --------------------------------------       
        private int getMaxStorage() { return 32768; }

	private int getMaxInput() { return maxvalue; }

	private int getMaxOutput() { return maxvalue; }
        
        @Override
	public int getEnergyStored(EnumFacing from) { return energyStorage.getEnergyStored(); }

	@Override
	public int getMaxEnergyStored(EnumFacing from) { return getMaxStorage(); }
        
        @Override
	public boolean canConnectEnergy(EnumFacing from) { return false; }

        @Override
	public FluxStorage getFluxStorage() { return energyStorage; }

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
