/*
|| UNDER 'GNU General Public License v3.0'
|| File made by thiago based (copied a lot) of files of mods 'Industrial Wires', and 'Immersive Engineering'.
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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;

import com.google.common.collect.Lists;

import net.minecraftforge.common.model.TRSRTransformation;

import javax.annotation.Nonnull;

import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import static blusunrize.immersiveengineering.api.energy.wires.WireType.LV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.MV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.HV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.REDSTONE_CATEGORY;

public class TileEntityCurrentTransformer extends TileEntityImmersiveConnectable implements IHasDummyBlocksIW, ITickable, IAdvancedCollisionBounds, IAdvancedSelectionBounds, IDirectionalTile, IPlayerInteraction  
{
// VARIABLES/CONS.: --------------------------------------
    private static final String SOUTH = "south";
    private static final String NORTH = "north";
    private static final String EAST = "east";
    private static final String WEST = "west";
    EnumFacing facing = EnumFacing.NORTH;
    public int redstoneValueFine = 0;
    public int redstoneValueCoarse = 0;
    boolean firstTick = true;
    public WireType electricWt = null;
    public double energyToMeasure = 0;
    private int dummy = 0;
    public final ArrayList<Double> lastPackets = new ArrayList<>(25);
	private int clock = 0;

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
	    if (isDummy()) { return; }
        if (!world.isRemote) {
	        if((world.getTotalWorldTime()&31)==(pos.toLong()&31)) { getRsvalues(); }
	        lastPackets.add(energyToMeasure);
	        if(lastPackets.size() > 20) { lastPackets.remove(0); }
	        energyToMeasure = 0;
        }
    }
    
//WIRE STUFF: --------------------------------------
    @Override
    public boolean allowEnergyToPass(Connection con) { return true; }

    @Override
    public boolean isEnergyOutput() { return true; }

    @Override
    public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset) {
        if(isDummy()) { return false; }
        if(REDSTONE_CATEGORY.equals(cableType.getCategory())) { return false; }        
        if(!((MV_CATEGORY.equals(cableType.getCategory())||HV_CATEGORY.equals(cableType.getCategory()))||(REDSTONE_CATEGORY.equals(cableType.getCategory())||LV_CATEGORY.equals(cableType.getCategory())))) { return false; }
	return true;
    }

    @Override
    public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other) { 
        electricWt = cableType;
        limitType = cableType;
    }

    @Override 
    public void removeCable(ImmersiveNetHandler.Connection connection) {
        electricWt = null; 
	    limitType = null;
    }
  
    @Override
    public Vec3d getConnectionOffset(Connection con) {
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
	
// REDSTONE STUFF: -------------------------------------------
    
    private void getRsvalues() {
        if(lastPackets.size()==0) { return; }
	    double sum = 0;
	    for(double transfer : lastPackets) {
            sum += transfer;
	    }
	    sum = sum/lastPackets.size();
        if(electricWt == null) { return; }
        sum = sum/(int)electricWt.getTransferRate();
	    sum = Math.ceil(sum*256);
        int redstoneValueCoarseInt = 0;
	    int redstoneValueFineInt = (int)sum;
		if(redstoneValueFineInt > 15) {
            for (redstoneValueFineInt = (int)sum; redstoneValueFineInt >= 16; redstoneValueFineInt -= 16) {
                redstoneValueCoarseInt++;    
            }
		}
		redstoneValueCoarse = redstoneValueCoarseInt;
		redstoneValueFine = redstoneValueFineInt;
		//the use of 2 ints is to TileEntityRedstoneControler take ALWAYS a correct value
    }
	
// GENERAL PROPERTYES: --------------------------------------           
    @Override
    public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if(isDummy()) {return false;}
        double sum = 0;
	    for(double transfer : lastPackets) {
            sum += transfer;
	    }
	    sum = sum/lastPackets.size();
        player.sendMessage(new TextComponentTranslation(IndustrialWires.MODID + ".chat.currentTransformer", String.format("%s", Utils.formatDouble(sum, "0.###"))));
        return true;
    }
	  
    @Override
	public float[] getBlockBounds() { return null; }

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds() {
		List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(.1875f, -.625f, .1875f, .8125f, .8125f, .8125f).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		if(isDummy()) {
			list.set(0, list.get(0).offset(0, 1, 0));
			list.add(new AxisAlignedBB(0, 0, 0, 1, .375f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(!isDummy()) {
		    list.add(new AxisAlignedBB(0.375, 0.625, 0.375, 0.625, 0.969, 0.625).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
        }
		return list;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list) { return false; }

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds() { return getAdvancedSelectionBounds(); }

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
        BlockPos pos2 = pos.offset(EnumFacing.DOWN, 1);
        world.setBlockState(pos2, state);
        TileEntity te = world.getTileEntity(pos2);
		if (te instanceof TileEntityCurrentTransformer) {
		    ((TileEntityCurrentTransformer) te).dummy = 1;
			((TileEntityCurrentTransformer) te).facing = this.facing;
		}
    }
    
    @Override
    public void breakDummies() {
	    for (int i = 0; i <= 1; i++) {
		     if (i != dummy && world.getTileEntity(pos.offset(EnumFacing.DOWN, i - dummy)) instanceof TileEntityCurrentTransformer) { world.setBlockToAir(pos.offset(EnumFacing.DOWN, i - dummy)); }
	    }
	}
// FINISH OF THIS CLASS ------------------------------------------------------------------------
}
