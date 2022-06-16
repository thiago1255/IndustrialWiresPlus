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
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.energy.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.energy.wires.redstone.RedstoneWireNetwork;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.Optional;

import static blusunrize.immersiveengineering.api.energy.wires.WireType.LV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.MV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.HV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.REDSTONE_CATEGORY;

public class TileEntityControlTransformerRs extends TileEntityImmersiveConnectable implements ITickable, IIEInternalFluxHandler, IPlayerInteraction, IBlockBoundsDirectional, IDirectionalTile, IRedstoneConnector  
{
// VARIABLES/CONS.: --------------------------------------
    private static final String SOUTH = "south";
    private static final String NORTH = "north";
    private static final String EAST = "east";
    private static final String WEST = "west";
    EnumFacing facing = EnumFacing.NORTH;
    public int maxvalue = 128;
    private int redstoneChannel = 0;
    private int redstoneValueFine = 0;
    private int redstoneValueCoarse = 0;
    public boolean wireenergy = false;
    private boolean wirers = false;
    public FluxStorage energyStorage = new FluxStorage(getMaxStorage());
    protected RedstoneWireNetwork wireNetwork = new RedstoneWireNetwork().add(this);
    boolean firstTick = true;
    TileEntity te = null;
    public WireType limitType = null;

// NBT DATA: --------------------------------------
    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        facing = EnumFacing.byHorizontalIndex(nbt.getByte("facing"));
        wireenergy = nbt.getBoolean("wireenergy");
        wirers = nbt.getBoolean("wirers");
	redstoneChannel = nbt.getInteger("redstoneChannel");
        energyStorage.readFromNBT(nbt);
    }
       
    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
	nbt.setByte("facing",  (byte) facing.getHorizontalIndex());
        nbt.setBoolean("wireenergy", wireenergy);
        nbt.setBoolean("wirers", wirers);
	nbt.setInteger("redstoneChannel", redstoneChannel);
	energyStorage.writeToNBT(nbt);
    }

// ITICKABLE: --------------------------------------
    @Override
    public void update() {
        if (!world.isRemote) {  
            int maxWire = 0;
            if(HV_CATEGORY.equals(limitType)) { maxWire = 128; }  
	    if(MV_CATEGORY.equals(limitType)) { maxWire = 32; } 
	    if(LV_CATEGORY.equals(limitType)) { maxWire = 8; } 
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
		if(this.energyStorage.getEnergyStored() > 0&&((TileEntityControlTransformerNormal)te).energyStorage.getEnergyStored() < getMaxStorage()){
                    int energytoblock = Math.min(getMaxStorage()-((TileEntityControlTransformerNormal)te).energyStorage.getEnergyStored(), Math.min(this.energyStorage.getEnergyStored(), maxvalue));
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
    protected boolean canTakeLV() { return false; }
        
    @Override
    protected boolean canTakeMV() { return false; }

    @Override
    protected boolean canTakeHV() { return true; }
 
    @Override
    protected boolean isRelay() { return false; }

    @Override
    public boolean allowEnergyToPass(Connection con) { return false; }

    @Override
    public boolean isEnergyOutput() { return true; }

    @Override
    public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset) {
        if(wireenergy && cableType.isEnergyWire()) { return false; }
        if(wirers && REDSTONE_CATEGORY.equals(cableType.getCategory())) { return false; }        
        if(!cableType.isEnergyWire() && !REDSTONE_CATEGORY.equals(cableType.getCategory())) { return false; }
        if(te instanceof TileEntityControlTransformerNormal) {
	    if(((TileEntityControlTransformerNormal)te).wire) { return false; }
	}
	return true;
    }

    @Override
    public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other) {
	if(REDSTONE_CATEGORY.equals(cableType.getCategory())) {
            wirers = true;
	    RedstoneWireNetwork.updateConnectors(pos, world, getNetwork());
	}
	if(cableType.isEnergyWire()) { 
	    wireenergy = true; 
            this.limitType = cableType;
	}
    }

    @Override 
    public void removeCable(ImmersiveNetHandler.Connection connection) {
        if(REDSTONE_CATEGORY.equals(connection.cableType)) {
	    wirers = false;
            super.removeCable(connection);
	    wireNetwork.removeFromNetwork(this);
	} else {
            limitType = null;
	    wireenergy = false; 
	}
    }
  
    @Override
    public Vec3d getConnectionOffset(Connection con) {
        boolean isRs = REDSTONE_CATEGORY.equals(con.cableType);
        Vec3d val = mat.apply(new Vec3d(isRs?1.1: 0.5, isRs?0.5: 1.7, 0.5)); //1.1, 0.5, 0.5 | 0.5, 1.7, 0.5
	return val;	
    }
    
    @Override
    public WireType getCableLimiter(TargetingInfo target) { return limitType; }

//ENERGY STRG: --------------------------------------       
    public int getMaxStorage() { return 32768; }

    public int getMaxInput() { return maxvalue; }

    public int getMaxOutput() { return maxvalue; }

    @Override
    public int outputEnergy(int amount, boolean simulate, int energyType) {
        if(amount > 0&&this.energyStorage.getEnergyStored() < getMaxStorage()){
            int quantityenergy = Math.min(getMaxStorage()-this.energyStorage.getEnergyStored(), Math.min(amount, maxvalue));
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

// REDSTONE WIRE: -------------------------------------------
    @Override
    public void setNetwork(RedstoneWireNetwork net) {
        wireNetwork = net;
    }

    @Override
    public RedstoneWireNetwork getNetwork() {
        return wireNetwork;
    }

    @Override
    public void onChange() { 
        redstoneValueCoarse = wireNetwork!=null?wireNetwork.getPowerOutput(redstoneChannel): 0;
	redstoneValueFine = wireNetwork!=null?wireNetwork.getPowerOutput(redstoneChannel+1): 0;
    }
    
    @Override
    public void updateInput(byte[] signals) {
    }

    @Override
    public World getConnectorWorld() {
        return getWorld();
    }
	
// GENERAL PROPERTYES: --------------------------------------         
    @Override
    public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
	    player.sendMessage(new TextComponentTranslation(IndustrialWires.MODID + ".chat.transformer", String.format("%d", maxvalue)));
	}
	return true;
    }
    
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
