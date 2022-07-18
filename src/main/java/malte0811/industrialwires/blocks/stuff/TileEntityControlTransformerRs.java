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
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.IBlockBoundsIW.IBlockBoundsDirectional;

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
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

import net.minecraftforge.common.model.TRSRTransformation;

import javax.annotation.Nonnull;

import java.util.Optional;
import java.util.Set;

import static blusunrize.immersiveengineering.api.energy.wires.WireType.LV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.MV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.HV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.REDSTONE_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.COPPER;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.COPPER_INSULATED;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.ELECTRUM;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.ELECTRUM_INSULATED;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.STEEL;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.REDSTONE;

public class TileEntityControlTransformerRs extends TileEntityImmersiveConnectable implements ITickable, IIEInternalFluxHandler, IBlockBoundsDirectional, IDirectionalTile  
{
// VARIABLES/CONS.: --------------------------------------
    private static final String SOUTH = "south";
    private static final String NORTH = "north";
    private static final String EAST = "east";
    private static final String WEST = "west";
    EnumFacing facing = EnumFacing.NORTH;
    public int maxvalue = 8;
    public int redstoneValueFine = 0;
    public int redstoneValueCoarse = 0;
    public boolean wireenergy = false;
    public FluxStorage energyStorage = new FluxStorage(getMaxStorage());
    protected RedstoneWireNetwork wireNetwork = new RedstoneWireNetwork().add(this);
    boolean firstTick = true;
    TileEntity te = null;
    public WireType electricWt = null;

// NBT DATA: --------------------------------------
    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        facing = EnumFacing.byHorizontalIndex(nbt.getByte("facing"));
        wireenergy = nbt.getBoolean("wireenergy");
	    redstoneChannel = nbt.getInteger("redstoneChannel");
        energyStorage.readFromNBT(nbt);
    }
       
    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
	    nbt.setByte("facing",  (byte) facing.getHorizontalIndex());
        nbt.setBoolean("wireenergy", wireenergy);
	    nbt.setInteger("redstoneChannel", redstoneChannel);
	    energyStorage.writeToNBT(nbt);
    }

// ITICKABLE: --------------------------------------
    @Override
    public void update() {
        if (!world.isRemote) {  
            int maxWire = 0;
            if(electricWt == WireType.STEEL) { maxWire = 128; }  
	    if(electricWt == WireType.ELECTRUM_INSULATED || electricWt == WireType.ELECTRUM) { maxWire = 32; } 
	    if(electricWt == WireType.COPPER_INSULATED || electricWt == WireType.COPPER) { maxWire = 8; } 
            int rsValue = (((redstoneValueCoarse*15)+redstoneValueCoarse)+(redstoneValueFine+1)); 
            maxvalue = (rsValue*maxWire);
            BlockPos left = null;
            switch (facing) {
	        case SOUTH: left = pos.offset(EnumFacing.EAST, -1); break;
                case NORTH: left = pos.offset(EnumFacing.WEST, -1); break;
	        case EAST: left = pos.offset(EnumFacing.NORTH, -1); break;
                case WEST: left = pos.offset(EnumFacing.SOUTH, -1); break;
	    }
            te = world.getTileEntity(left);
	    if(te instanceof TileEntityControlTransformerNormal) { 
		if(this.energyStorage.getEnergyStored() > 0&&((TileEntityControlTransformerNormal)te).energyStorage.getEnergyStored() < this.getMaxStorage()){
                    int energytoblock = Math.min(this.getMaxStorage()-((TileEntityControlTransformerNormal)te).energyStorage.getEnergyStored(), Math.min(this.energyStorage.getEnergyStored(), this.maxvalue));
	            ((TileEntityControlTransformerNormal)te).energyStorage.modifyEnergyStored(+energytoblock);
                    this.energyStorage.modifyEnergyStored(-energytoblock);
	        }
            }
	}
        else if(firstTick) {
	    Set<Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
	    if(conns!=null) { for(Connection conn : conns) { if(pos.compareTo(conn.end) < 0&&world.isBlockLoaded(conn.end)) { this.markContainingBlockForUpdate(null); } } }
	    firstTick = false;
	}               
    }
    
//WIRE STUFF: --------------------------------------
    @Override
    public boolean allowEnergyToPass(Connection con) { return false; }

    @Override
    public boolean isEnergyOutput() { return true; }

    @Override
    public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset) {
        if(wireenergy && cableType.isEnergyWire()) { return false; }
        if(REDSTONE_CATEGORY.equals(cableType.getCategory())) { return false; }        
        if(!cableType.isEnergyWire() && !REDSTONE_CATEGORY.equals(cableType.getCategory())) { return false; }
        if(te instanceof TileEntityControlTransformerNormal) {
	        if(((TileEntityControlTransformerNormal)te).wire) { return false; }
	    }
	    return true;
    }

    @Override
    public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other) {
	    wireenergy = true; 
        electricWt = cableType;
    }

    @Override 
    public void removeCable(ImmersiveNetHandler.Connection connection) {  
        electricWt = null;
	    wireenergy = false; 
	    limitType = null;
    }
  
    @Override
    public Vec3d getConnectionOffset(Connection con) {
        boolean isRs = (con.cableType == WireType.REDSTONE);
        Vec3d val = new Vec3d(isRs?1.1: 0.5, isRs?0.5: 1.7, 0.5); //1.1, 0.5, 0.5 | 0.5, 1.7, 0.5
        switch (facing) {
	    case SOUTH: val = new Vec3d(isRs?0.4: 0.6, isRs?0.5: 1.7, isRs?0: 0.5); break;
            case NORTH: val = new Vec3d(isRs?0.6: 0.4, isRs?0.5: 1.7, isRs?1: 0.5); break;
	    case EAST: val = new Vec3d(isRs?0: 0.5, isRs?0.5: 1.7, isRs?0.6: 0.4); break;
            case WEST: val = new Vec3d(isRs?1: 0.5, isRs?0.5: 1.7, isRs?0.4: 0.6); break;
	}
	return val;	
    }
    
//ENERGY STRG: --------------------------------------       
    public int getMaxStorage() { return 32768; }

    public int getMaxInput() { return maxvalue; }

    public int getMaxOutput() { return maxvalue; }

    @Override
    public int outputEnergy(int amount, boolean simulate, int energyType) {
        if(energyType != 0) { return 0; }
        if(amount > 0&&this.energyStorage.getEnergyStored() < getMaxStorage()){
            int quantityenergy = Math.min(getMaxStorage()-this.energyStorage.getEnergyStored(), amount);
	    quantityenergy = Math.min(quantityenergy, maxvalue);
	    if(!simulate){
	        this.energyStorage.modifyEnergyStored(+quantityenergy);
	    }
	    return quantityenergy;
	}
	return 0;
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) { return false; }

    @Override
    public FluxStorage getFluxStorage() { return energyStorage; }

    @Override
    public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing) { return null; } 
        
    @Override
    public SideConfig getEnergySideConfig(EnumFacing facing) { return SideConfig.NONE; }   
	
	@Override
	public boolean moveConnectionTo(Connection c, BlockPos newEnd) { return true; }

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
    
// FINISH OF THIS CLASS ------------------------------------------------------------------------
}
