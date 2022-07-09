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
import blusunrize.immersiveengineering.api.energy.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.energy.wires.redstone.RedstoneWireNetwork;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;

import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.IBlockBoundsIW.IBlockBoundsDirectional;
import malte0811.industrialwires.blocks.IHasDummyBlocksIW;

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
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.model.TRSRTransformation;

import javax.annotation.Nonnull;

import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;

import static blusunrize.immersiveengineering.api.energy.wires.WireType.LV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.MV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.HV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.REDSTONE_CATEGORY;

public class TileEntityCurrentTransformer extends TileEntityImmersiveConnectable implements IHasDummyBlocksIW, ITickable, IBlockBoundsDirectional, IDirectionalTile, IRedstoneConnector, IPlayerInteraction  
{
// VARIABLES/CONS.: --------------------------------------
    private static final String SOUTH = "south";
    private static final String NORTH = "north";
    private static final String EAST = "east";
    private static final String WEST = "west";
    EnumFacing facing = EnumFacing.NORTH;
    protected int redstoneChannel = 0;
    private int redstoneValueFine = 0;
    private int redstoneValueCoarse = 0;
    private boolean wirers = false;
    protected RedstoneWireNetwork wireNetwork = new RedstoneWireNetwork().add(this);
    boolean firstTick = true;
    public WireType electricWt = null;
    public double energyToMeasure = 0;
    private int dummy = 0;
    public final ArrayList<Double> lastPackets = new ArrayList<>(25);

// NBT DATA: --------------------------------------
    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        facing = EnumFacing.byHorizontalIndex(nbt.getByte("facing"));
        wirers = nbt.getBoolean("wirers");
	redstoneChannel = nbt.getInteger("redstoneChannel");
    }
       
    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
	nbt.setByte("facing",  (byte) facing.getHorizontalIndex());
        nbt.setBoolean("wirers", wirers);
	nbt.setInteger("redstoneChannel", redstoneChannel);
    }

// ITICKABLE: --------------------------------------
    @Override
    public void update() {
        if (!world.isRemote) {
	    if((world.getTotalWorldTime()&31)==(pos.toLong()&31)) {
	        this.getRsvalues();
	    }
	    lastPackets.add(energyToMeasure);
	    if(lastPackets.size() > 20) { lastPackets.remove(0); }
	    energyToMeasure = 0;
        } else if(firstTick) {
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
        if(isDummy()) { return false; }
        if(wirers && REDSTONE_CATEGORY.equals(cableType.getCategory())) { return false; }        
        if(!((MV_CATEGORY.equals(cableType.getCategory())||HV_CATEGORY.equals(cableType.getCategory()))||(REDSTONE_CATEGORY.equals(cableType.getCategory())||LV_CATEGORY.equals(cableType.getCategory())))) { return false; }
	return true;
    }

    @Override
    public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other) {
        if(WireType.REDSTONE_CATEGORY.equals(cableType.getCategory())) {
            wirers = true;
	}
	if(cableType.isEnergyWire()) { 
            electricWt = cableType;
            limitType = cableType;
	}
	markDirty();
    }

    @Override 
    public void removeCable(ImmersiveNetHandler.Connection connection) {
        if(connection.cableType == WireType.REDSTONE) {
	    wirers = false;
	} else {   
            electricWt = null; 
	}
	limitType = null;
	markDirty();
    }
  
    @Override
    public Vec3d getConnectionOffset(Connection con) {
        if(con.cableType == WireType.REDSTONE) { return new Vec3d(0.5, 0.9, 0.5); }
        int xDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)?con.end.getX()-getPos().getX(): (con.end.equals(Utils.toCC(this))&&con.start!=null)?con.start.getX()-getPos().getX(): 0;
	int zDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)?con.end.getZ()-getPos().getZ(): (con.end.equals(Utils.toCC(this))&&con.start!=null)?con.start.getZ()-getPos().getZ(): 0;
	if(facing.getAxis()==Axis.X) {
	    return new Vec3d(.5, .4375, zDif > 0?.8125: .1875);
	} else {
	    return new Vec3d(xDif > 0?.8125: .1875, .4375, .5);
	}
    }
    
    @Override
    public void onEnergyPassthrough(double amount) {
        energyToMeasure += amount;
    }

    @Override
    public boolean moveConnectionTo(Connection c, BlockPos newEnd) { return true; }
	
// REDSTONE WIRE: -------------------------------------------
    @Override
    public void setNetwork(RedstoneWireNetwork net) { wireNetwork = net; }

    @Override
    public RedstoneWireNetwork getNetwork() { return wireNetwork; }

    @Override
    public void onChange() { 
    }
    
    @Override
    public void updateInput(byte[] signals) {
        signals[redstoneChannel] = (byte)Math.max(redstoneValueCoarse, signals[redstoneChannel]);
        signals[(redstoneChannel+1)] = (byte)Math.max(redstoneValueFine, signals[(redstoneChannel+1)]);
    }

    @Override
    public World getConnectorWorld() { return getWorld(); }
    
    private void getRsvalues() {
        if(lastPackets.size()==0) { return; }
	double sum = 0;
	for(double transfer : lastPackets) {
            sum += transfer;
	}
	sum = sum/lastPackets.size();
        sum = sum/(int)electricWt.getTransferRate();
	sum = Math.ceil(sum*256);
        redstoneValueCoarse = 0;
	redstoneValueFine = (int)sum;
        for (redstoneValueFine = (int)sum; redstoneValueFine >= 16; redstoneValueFine -= 16) {
            redstoneValueCoarse++;    
        }
        wireNetwork.updateValues();
    }
	
// GENERAL PROPERTYES: --------------------------------------           
    @Override
    public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if(isDummy()) {return false;}
	if(Utils.isHammer(heldItem)&&!world.isRemote){
	    if(redstoneChannel == 14) { 
	        redstoneChannel = 0; 
	    } else { 
	        redstoneChannel++; 
	    }
	    player.sendMessage(new TextComponentTranslation(IndustrialWires.MODID + ".chat.transformerRs", String.format("%s", nameOfColorOfWire())));
	    markDirty();      
	    return true;
	}
        return false;
    }
	
    protected String nameOfColorOfWire() {
        switch(redstoneChannel) {
            case 0: return "White - Orange";
	    case 1: return "Orange - Magenta";
	    case 2: return "Magenta - L. Blue";
	    case 3: return "L. Blue - Yellow";
	    case 4: return "Yellow - L. Green";
	    case 5: return "L. Green - Pink";
	    case 6: return "Pink - D. Gray";
	    case 7: return "D. Gray - L. Gray";
	    case 8: return "L. Gray - Cyan";
	    case 9: return "Cyan - Purple";
            case 10: return "Purple - D. Blue";
	    case 11: return "D. Blue - Brown";
	    case 12: return "Brown - D. Green";
	    case 13: return "D. Green - Red";
	    case 14: return "Red - Black";
        }
        return "ERROR";
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

// DUMMY BLOCKS: --------------------------------------
    @Override
    public boolean isDummy() { return dummy != 0; }

    @Override
    public void placeDummies(IBlockState state) {
        for(int i = 1; i <= 1; i++){
	    world.setBlockState(pos.add(0, -i, 0), state);
            ((TileEntityCurrentTransformer)world.getTileEntity(pos.add(0, -i, 0))).dummy = i;
	    ((TileEntityCurrentTransformer)world.getTileEntity(pos.add(0, -i, 0))).facing = this.facing;
        }
    }

    @Override
    public void breakDummies() {
        for(int i = 0; i <= 1; i++) {
	    if(world.getTileEntity(getPos().add(0, dummy, 0).add(0, -i, 0)) instanceof TileEntityCurrentTransformer) {
	        world.setBlockToAir(getPos().add(0, dummy, 0).add(0, -i, 0));
	    }
        }
    }
    
// FINISH OF THIS CLASS ------------------------------------------------------------------------
}
