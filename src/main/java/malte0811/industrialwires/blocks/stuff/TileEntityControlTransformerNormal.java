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
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;

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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
import static malte0811.industrialwires.util.MiscUtils.offset;

public class TileEntityControlTransformerNormal extends TileEntityImmersiveConnectable implements ITickable, IIEInternalFluxHandler, IBlockBoundsDirectional, IDirectionalTile, IHasDummyBlocksIW 
{
// VARIABLES/CONS.: --------------------------------------
    private static final String SOUTH = "south";
    private static final String NORTH = "north";
    private static final String EAST = "east";
    private static final String WEST = "west";
    EnumFacing facing = EnumFacing.NORTH;
    public BlockPos endOfLeftConnection = null;
    public boolean wire = false;
    public FluxStorage energyStorage = new FluxStorage(getMaxStorage());
    boolean firstTick = true;
    TileEntity tiEn = null;

// NBT DATA: --------------------------------------
    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        facing = EnumFacing.byHorizontalIndex(nbt.getByte("facing"));
        wire = nbt.getBoolean("wire");
        energyStorage.readFromNBT(nbt);
    }
       
    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
	    nbt.setByte("facing",  (byte) facing.getHorizontalIndex());
        nbt.setBoolean("wire", wire);
	    energyStorage.writeToNBT(nbt);
    }

// ITICKABLE: --------------------------------------
    @Override
    public void update() {
        if (!world.isRemote) { 
	        BlockPos rigth = null;
	        switch (facing) {
	            case SOUTH: rigth = pos.offset(EnumFacing.WEST, -1); break;
                case NORTH: rigth = pos.offset(EnumFacing.EAST, -1); break;
	            case EAST: rigth = pos.offset(EnumFacing.SOUTH, -1); break;
                case WEST: rigth = pos.offset(EnumFacing.NORTH, -1); break;
	        }
            tiEn = world.getTileEntity(rigth);
            if(energyStorage.getEnergyStored() > 0){
	            int temp = this.transferEnergy(energyStorage.getEnergyStored(), true, 0);
		        if(temp > 0){
		            energyStorage.modifyEnergyStored(-this.transferEnergy(temp, false, 0));
			        markDirty();
		        }
		        addAvailableEnergy(-1F, null);
		        notifyAvailableEnergy(energyStorage.getEnergyStored(), null);
            }
	    } else if(firstTick) {
	        Set<Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
	        if(conns!=null) { 
			    for(Connection conn : conns) { 
				    if(pos.compareTo(conn.end) < 0&&world.isBlockLoaded(conn.end)) { 
					    this.markContainingBlockForUpdate(null); 
				    } 
				} 
			}
	        firstTick = false;
	    }               
    }
    
//WIRE STUFF: --------------------------------------
    @Override
    public boolean allowEnergyToPass(Connection con) { return true; }

    @Override
    public boolean isEnergyOutput() { return true; }

    @Override
    public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset) {
        if(wire) { return false; }
        if(tiEn instanceof TileEntityControlTransformerRs) {
	        if(((TileEntityControlTransformerRs)tiEn).wireenergy) {
	            return ((TileEntityControlTransformerRs)tiEn).electricWt == cableType;
            }
        }
	    return false;
    }

    @Override
    public WireType getCableLimiter(TargetingInfo target) { return limitType; }

    @Override
    public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other) {
        if(this.limitType==null) { this.limitType = cableType; }
	    wire = true;
	    markDirty();
    }

    @Override 
    public void removeCable(Connection connection) {
        wire = false;
	    limitType = null;
	    markDirty();
    }
  
    @Override
    public Vec3d getConnectionOffset(Connection con) {
        Vec3d val = new Vec3d(0.5, 1.7, 0.5);
        switch (facing) {
	        case SOUTH: val = new Vec3d(0.4, 1.7, 0.5); break;
            case NORTH: val = new Vec3d(0.6, 1.7, 0.5); break;
	        case EAST: val = new Vec3d(0.5, 1.7, 0.6); break;
            case WEST: val = new Vec3d(0.5, 1.7, 0.4); break;
	    }
	    return val; 
    }
    
//ENERGY STRG: --------------------------------------       
    public int getMaxStorage() { return 32768; }

    public int getMaxInput() { 
        if(tiEn instanceof TileEntityControlTransformerRs) {
            return ((TileEntityControlTransformerRs)tiEn).maxvalue;
        }
	    return 0;
    }

    public int getMaxOutput() { return this.getMaxInput(); }

    @Override
    public int outputEnergy(int amount, boolean simulate, int energyType) { return 0; }

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
    
// OUTPUT TO GRID (copy from: https://github.com/BluSunrize/ImmersiveEngineering/blob/master/src/main/java/blusunrize/immersiveengineering/common/blocks/metal/TileEntityConnectorLV.java) -----------------------------
    public int transferEnergy(int energy, boolean simulate, final int energyType)
	{
		int received = 0;
		if(!world.isRemote)
		{
			Set<AbstractConnection> outputs = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(Utils.toCC(this),
					world, true);
			int powerLeft = Math.min(Math.min(getMaxOutput(), getMaxInput()), energy);
			final int powerForSort = powerLeft;

			if(outputs.isEmpty())
				return 0;

			int sum = 0;
			//TreeMap to prioritize outputs close to this connector if more energy is requested than available
			//(energy will be provided to the nearby outputs rather than some random ones)
			Map<AbstractConnection, Integer> powerSorting = new TreeMap<>();
			for(AbstractConnection con : outputs)
				if(con.isEnergyOutput)
				{
					IImmersiveConnectable end = ApiUtils.toIIC(con.end, world);
					if(con.cableType!=null&&end!=null)
					{
						int atmOut = Math.min(powerForSort, con.cableType.getTransferRate());
						int tempR = end.outputEnergy(atmOut, true, energyType);
						if(tempR > 0)
						{
							powerSorting.put(con, tempR);
							sum += tempR;
						}
					}
				}

			if(sum > 0)
				for(AbstractConnection con : powerSorting.keySet())
				{
					IImmersiveConnectable end = ApiUtils.toIIC(con.end, world);
					if(con.cableType!=null&&end!=null)
					{
						float prio = powerSorting.get(con)/(float)sum;
						int output = Math.min(MathHelper.ceil(powerForSort*prio), powerLeft);

						int tempR = end.outputEnergy(Math.min(output, con.cableType.getTransferRate()), true, energyType);
						int r = tempR;
						int maxInput = getMaxInput();
						tempR -= (int)Math.max(0, Math.floor(tempR*con.getPreciseLossRate(tempR, maxInput)));
						end.outputEnergy(tempR, simulate, energyType);
						HashSet<IImmersiveConnectable> passedConnectors = new HashSet<IImmersiveConnectable>();
						float intermediaryLoss = 0;
						//<editor-fold desc="Transfer rate and passed energy">
						for(Connection sub : con.subConnections)
						{
							float length = sub.length/(float)sub.cableType.getMaxLength();
							float baseLoss = (float)sub.cableType.getLossRatio();
							float mod = (((maxInput-tempR)/(float)maxInput)/.25f)*.1f;
							intermediaryLoss = MathHelper.clamp(intermediaryLoss+length*(baseLoss+baseLoss*mod), 0, 1);

							int transferredPerCon = ImmersiveNetHandler.INSTANCE.getTransferedRates(world.provider.getDimension()).getOrDefault(sub, 0);
							transferredPerCon += r;
							if(!simulate)
							{
								ImmersiveNetHandler.INSTANCE.getTransferedRates(world.provider.getDimension()).put(sub, transferredPerCon);
								IImmersiveConnectable subStart = ApiUtils.toIIC(sub.start, world);
								IImmersiveConnectable subEnd = ApiUtils.toIIC(sub.end, world);
								if(subStart!=null&&passedConnectors.add(subStart))
									subStart.onEnergyPassthrough(r-r*intermediaryLoss);
								if(subEnd!=null&&passedConnectors.add(subEnd))
									subEnd.onEnergyPassthrough(r-r*intermediaryLoss);
							}
						}
						//</editor-fold>
						received += r;
						powerLeft -= r;
						if(powerLeft <= 0)
							break;
					}
				}
		}
		return received;
	}

	private void notifyAvailableEnergy(int energyStored, @Nullable Set<AbstractConnection> outputs)
	{
		if(outputs==null)
			outputs = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(pos, world, true);
		for(AbstractConnection con : outputs)
		{
			IImmersiveConnectable end = ApiUtils.toIIC(con.end, world);
			if(con.cableType!=null&&end!=null&&end.allowEnergyToPass(null))
			{
				Pair<Float, Consumer<Float>> e = getEnergyForConnection(con);
				end.addAvailableEnergy(e.getKey(), e.getValue());
			}
		}
	}

	private Pair<Float, Consumer<Float>> getEnergyForConnection(@Nullable AbstractConnection c)
	{
		float loss = c!=null?c.getAverageLossRate(): 0;
		float max = (1-loss)*energyStorage.getEnergyStored();
		Consumer<Float> extract = (energy) -> {
			energyStorage.modifyEnergyStored((int)(-energy/(1-loss)));
		};
		return new ImmutablePair<>(max, extract);
	}

    @Nullable
	@Override
	protected Pair<Float, Consumer<Float>> getOwnEnergy()
	{
		return getEnergyForConnection(null);
	}
	
// "Dummy blocks" ------------------------------------------------------------------------------
	@Override
    public boolean isDummy() { return false; }
    
    @Override
	public void placeDummies(IBlockState state) { 
	    BlockPos position = pos.offset(facing.rotateY(), -1);
        world.setBlockState(position, IndustrialWires.generalStuff.getStateFromMeta(0));
        TileEntity te = world.getTileEntity(position);
		if (te instanceof TileEntityControlTransformerRs) {
			((TileEntityControlTransformerRs) te).facing = this.facing;
		}
	}
    
    @Override
    public void breakDummies() {
	    BlockPos position = pos.offset(facing.rotateY(), -1);
		if (world.getTileEntity(position) instanceof TileEntityControlTransformerRs) { 
	        world.setBlockToAir(position); 
		}
	}

// FINISH OF THIS CLASS ------------------------------------------------------------------------
}
