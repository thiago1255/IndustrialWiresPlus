/*
|| UNDER 'GNU General Public License v3.0'
|| File made by thiago1255 based (copied a lot) of files of mods 'Industrial Wires', and 'Immersive Engineering'.
||
|| (check github for credits of this mods:)
|| IW: https://github.com/malte0811/IndustrialWires
|| IE: https://github.com/BluSunrize/ImmersiveEngineering
*/
package malte0811.industrialwires.blocks.stuff;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;

import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.IHasDummyBlocksIW;
import malte0811.industrialwires.blocks.IBlockBoundsIW.IBlockBoundsDirectional;
import malte0811.industrialwires.compat.Compat;

import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergyAcceptor;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

import net.minecraftforge.common.model.TRSRTransformation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.*;
import java.util.Set;
import java.util.Optional;

import static blusunrize.immersiveengineering.api.energy.wires.WireType.MV_CATEGORY;

public class TileEntityRetifierValve extends TileEntityImmersiveConnectable implements IHasDummyBlocksIW, IBlockBoundsDirectional, IDirectionalTile, IEnergySource, ITickable
{
// VARIABLES/CONS.: --------------------------------------
	private static final double MAX_TEMP = 49152;
	private static final int MAX_ENERGY = 16384;
    EnumFacing facing = EnumFacing.NORTH;
    private int dummy = 0;
	public double temperature = 0;
	private int storedEnergy = 0;
	private boolean cabble = false;
	public boolean active = true;
	public boolean ignition = false;

// NBT DATA: --------------------------------------
    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        facing = EnumFacing.byHorizontalIndex(nbt.getByte("facing"));
        dummy = nbt.getInteger("dummys");
        temperature = nbt.getDouble("temp");
		storedEnergy = nbt.getInteger("eN");
		cabble = nbt.getBoolean("cabble");
		active = nbt.getBoolean("active");
    }
       
    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
	    nbt.setByte("facing",  (byte) facing.getHorizontalIndex());
        nbt.setInteger("dummys", dummy);
		nbt.setDouble("temp", temperature);
		nbt.setInteger("eN", storedEnergy);
		nbt.setBoolean("cabble", cabble);
		nbt.setBoolean("active", active);
    }
// ITICKABLE ---------------------------------------
    @Override
    public void update() {
        if(this.isDummy()) {return;}
        if(!world.isRemote) {
            if(temperature > MAX_TEMP) { 
                world.createExplosion(null, pos.getX(), pos.getY()-1, pos.getZ(), 8, true);
				return;
			}
			if((ignition) && (storedEnergy > 192)) {
			    storedEnergy -= 192;
				temperature += 178.01;
				if(active) { 
				    active = false; 
					markDirty();
					active = false;
					atualizar();
				}
                return;
			}
			if((temperature >= 19000) && (storedEnergy > 2)) {
				temperature -= Math.min(2.39, MAX_TEMP-temperature); 
			    if(!active) { 
				    active = true;
					markDirty();
					active = true;
					atualizar();
				}
				storedEnergy -= 2;
				return;
			} else {
			    temperature -= Math.min(0.5, MAX_TEMP-temperature); 
			    if(active) { 
				    active = false; 
					markDirty();
					active = false;
					atualizar();
				}
				return;
            }
		}
    }
	
	private void atualizar() {
	    IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 3);
		world.addBlockEvent(pos, state.getBlock(), 255, 0);
	}
//WIRE STUFF: --------------------------------------
    @Override
    public boolean allowEnergyToPass(Connection con) { return true; }

    @Override
    public boolean isEnergyOutput() { return true; }

    @Override
    public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset) {
        if(isDummy()) { return false; }     
        if(!MV_CATEGORY.equals(cableType.getCategory())) { return false; }
		if(cabble) {return false;}
	    return true;
    }

    @Override
    public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other) { limitType = cableType; cabble = true; }

    @Override 
    public void removeCable(ImmersiveNetHandler.Connection connection) { limitType = null; cabble = false;}
  
    @Override
    public Vec3d getConnectionOffset(Connection con) {
	    switch (facing) {
		    case NORTH: return new Vec3d(1.25, -0.5, 0.5);
			case SOUTH: return new Vec3d(-0.25, -0.5, 0.5);
			case EAST: return new Vec3d(0.5, -0.5, 1.25);
			case WEST: return new Vec3d(0.5, -0.5, -0.25);
		}
		return new Vec3d(0.5, 0.5, 0.5);
    }
	
	@Override
    public int outputEnergy(int amount, boolean simulate, int energyType) {
		/*if((ignition) && (amount > 190)) {
		    if(!simulate) {
		        temperature += 7299.01;
				active = true;
                if(temperature < 21900) {active = false;}
            }
            return 190;
		} else if((amount > 2) && (storedEnergy < MAX_ENERGY)) {
		    int numberToReturn = Math.min(amount, MAX_ENERGY-storedEnergy);
            if(!simulate) {
                storedEnergy += ( numberToReturn - 2 );
				temperature -= Math.min(0.79, MAX_TEMP-temperature);
				active = true;
                if(temperature < 21900) {active = false;}
            } 
			return numberToReturn;
        } else {
		    active = false;
            return 0;
        }*/

        if((amount > 2) && (storedEnergy < MAX_ENERGY)) {
		    int numberToReturn = Math.min(amount, (MAX_ENERGY - storedEnergy));
			numberToReturn = Math.min(numberToReturn, 8192);
            if(!simulate) {
			    storedEnergy += numberToReturn;
            } 
			return numberToReturn;
		}
		return 0;
    }

// IC2 STUFF: -----------------------------------------------
    @Override
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing side) {
		//return true;
		return side == facing.getOpposite();
	}
    
	@Override
	public int getSourceTier() {
		return 4;
	}
	
	@Override
	public double getOfferedEnergy() {
	    if(!active) {return 0;}
		return ((storedEnergy - 3) / 4);
	}
	
	@Override
	public void drawEnergy(double amount) { 
		storedEnergy -= amount * 4;
		temperature += amount / 100;
		markDirty();
	}
	
	@Override
	public void onLoad() {
		super.onLoad();
		if (!world.isRemote && IndustrialWires.hasIC2) { Compat.loadIC2Tile.accept(this); }
	}
	
	@Override
	public void invalidate() {
		if (!world.isRemote) { Compat.unloadIC2Tile.accept(this); }
		super.invalidate();
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (!world.isRemote) { Compat.unloadIC2Tile.accept(this); }
	}
    	
// GENERAL PROPERTYES: --------------------------------------           	  
    AxisAlignedBB aabb = null;
    @Override
    public AxisAlignedBB getBoundingBoxNoRot() { return new AxisAlignedBB(0, 0, 0, 1, 1, 1); }

    @Override
    public AxisAlignedBB getBoundingBox() {
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
	
// DUMMY BLOCKS: --------------------------------------
    @Override
    public boolean isDummy() { return dummy != 0; }
    
    @Override
	public void placeDummies(IBlockState state) {
        for(int i = 1; i <= 1; i++){
            BlockPos pos2 = pos.offset(EnumFacing.DOWN, i);
            world.setBlockState(pos2, state);
            TileEntity te = world.getTileEntity(pos2);
		    if (te instanceof TileEntityRetifierValve) {
		        ((TileEntityRetifierValve) te).dummy = i;
			    ((TileEntityRetifierValve) te).facing = this.facing;
		    }
		}
    }
    
    @Override
    public void breakDummies() {
	    for (int i = 0; i <= 1; i++) {
		     if (i != dummy && world.getTileEntity(pos.offset(EnumFacing.DOWN, i - dummy)) instanceof TileEntityRetifierValve) { world.setBlockToAir(pos.offset(EnumFacing.DOWN, i - dummy)); }
	    }
	}
// FINISH OF THIS CLASS ------------------------------------------------------------------------
}