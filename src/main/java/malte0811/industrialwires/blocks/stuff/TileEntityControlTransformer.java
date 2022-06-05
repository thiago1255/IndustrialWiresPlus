/*
|| (do what u want with this, but give credits to:)
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
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;


import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.IBlockBoundsIW.IBlockBoundsDirectional;
import malte0811.industrialwires.blocks.IHasDummyBlocksIW;
import malte0811.industrialwires.IndustrialWires;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.state.IBlockState;

import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

public class TileEntityControlTransformer extends TileEntityImmersiveConnectable implements ITickable, IHasDummyBlocksIW, IIEInternalFluxHandler, IBlockBoundsDirectional, IDirectionalTile  
{
// VARIABLES/CONS.: --------------------------------------
        private static final String SOUTH = "south";
        private static final String NORTH = "north";
        private static final String EAST = "east";
        private static final String WEST = "west";
        EnumFacing facing = EnumFacing.NORTH;
	private int dummy = 0;
	private int redstonevalue = 0;
        public BlockPos endOfLeftConnection = null;
        public int maxvalue = 2048;
	private boolean wireenergy = false;
        public FluxStorage energyStorage = new FluxStorage(getMaxStorage());
        
        boolean firstTick = true;

// NBT DATA: --------------------------------------
        @Override
	public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
	    super.readCustomNBT(nbt, descPacket);
            facing = EnumFacing.byHorizontalIndex(nbt.getByte("facing"));
	    dummy = nbt.getInteger("dummys");
            wireenergy = nbt.getBoolean("wireenergy");
            energyStorage.readFromNBT(nbt);
        }
        
        @Override
	public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
	    super.writeCustomNBT(nbt, descPacket);
	    nbt.setByte("facing",  (byte) facing.getHorizontalIndex());
	    nbt.setInteger("dummys", dummy);
            nbt.setBoolean("wireenergy", wireenergy);
            energyStorage.writeToNBT(nbt);
        }
        
// ITICKABLE: --------------------------------------
        @Override
 	public void update() {
	    if (!world.isRemote) { 
	        if (!isDummy()){
                    redstonevalue = world.getRedstonePowerFromNeighbors(pos);    
                    maxvalue = ((redstonevalue + 1)*2048); 
		    if(this.energyStorage.getEnergyStored() > 0){
		        int temp = this.transferEnergy(this.energyStorage.getEnergyStored(), true, 0);
		        if(temp > 0){
		            this.energyStorage.modifyEnergyStored(-this.transferEnergy(temp, false, 0));
			    markDirty();
		        }
		        addAvailableEnergy(-1F, null);
		        notifyAvailableEnergy(this.energyStorage.getEnergyStored(), null);
		    }
		}
            }
            else if(firstTick) {
	        Set<Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
		if(conns!=null) { for(Connection conn : conns) { if(pos.compareTo(conn.end) < 0&&world.isBlockLoaded(conn.end)) { this.markContainingBlockForUpdate(null); } } }
		firstTick = false;
	    }               
     	}
//ENERGY STRG: --------------------------------------       
        public int getMaxStorage() { return 32768; }

        @Override
	public int outputEnergy(int amount, boolean simulate, int energyType){
	    int quantityenergy = this.maxvalue;
	    if(isDummy()){
                if(amount > 0&&this.energyStorage.getEnergyStored() < getMaxStorage()){
                    quantityenergy = Math.min(getMaxStorage()-this.energyStorage.getEnergyStored(), Math.min(amount, quantityenergy));
		    if(!simulate){
		        this.energyStorage.modifyEnergyStored(+quantityenergy);
		    }
		    return quantityenergy;
	        }
	    }
	    return 0;
	}

        @Override
	public boolean canConnectEnergy(EnumFacing from) { return false; }

        @Override
	public FluxStorage getFluxStorage() { 
	    if(dummy > 0){
               BlockPos pos2 = pos.offset(EnumFacing.WEST, -dummy);
               switch (facing) {
		   case SOUTH: pos2 = pos.offset(EnumFacing.WEST, -dummy); break;
                   case NORTH: pos2 = pos.offset(EnumFacing.EAST, -dummy); break;
		   case EAST: pos2 = pos.offset(EnumFacing.SOUTH, -dummy); break;
                   case WEST: pos2 = pos.offset(EnumFacing.NORTH, -dummy); break;
	       }
               TileEntity te = world.getTileEntity(pos2);
	       if(te instanceof TileEntityControlTransformer) { return ((TileEntityControlTransformer)te).getFluxStorage(); }
	   }
	   return energyStorage;
	}

        @Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing) { return null; } 
        
        @Override
	public SideConfig getEnergySideConfig(EnumFacing facing) { return SideConfig.NONE; }
	             
//WIRE STUFF: --------------------------------------
        @Override
        protected boolean canTakeLV() { return true; }
        
        @Override
	protected boolean canTakeMV() { return true; }

        @Override
	protected boolean canTakeHV() { return true; }
 
        @Override
	protected boolean isRelay() { return false; }

        @Override
	public boolean allowEnergyToPass(Connection con) {
	    return true;
	}

        @Override
	public boolean isEnergyOutput() { return true; }

        @Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset){
	    if(!cableType.isEnergyWire()&&!REDSTONE_CATEGORY.equals(cableType.getCategory())) { return false; }
	    if(wireenergy) { return false; }
	    return limitType==null||WireApi.canMix(cableType, limitType);
	}

        @Override
	public WireType getCableLimiter(TargetingInfo target) { return limitType; }

        @Override
	public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other){
	    if(this.limitType==null) { this.limitType = cableType; }
	    wireenergy = true;
	}

        @Override 
	public void removeCable(Connection connection){
	    WireType type = connection!=null?connection.cableType: null;
	    if(type==null) { wireenergy = false; }
	    else { wireenergy = false; }

	    if(!wireenergy) { limitType = null; }
	}

        @Override
	public boolean moveConnectionTo(Connection c, BlockPos newEnd){
	    if(c.end.equals(endOfLeftConnection)) { endOfLeftConnection = newEnd; }
	    return true;
	}

        @Override
        public boolean receiveClientEvent(int id, int arg){
	    if(super.receiveClientEvent(id, arg)) { return true; }
	    this.markContainingBlockForUpdate(null);
	    return true;
	}

        @Override
	public Vec3d getConnectionOffset(Connection con) { return new Vec3d(0.5, 1.7, 0.5); } 
	
// DUMMY BLOCKS: --------------------------------------
        @Override
	public boolean isDummy() { return dummy != 0; }

        @Override
	public void placeDummies(IBlockState state) {
             for (int i = 1; i <= 1; i++) {
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
       public AxisAlignedBB getBoundingBox(){
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
       

// OUTPUT ENERGY TO GRID: --------------------- (from: https://github.com/BluSunrize/ImmersiveEngineering/blob/master/src/main/java/blusunrize/immersiveengineering/common/blocks/metal/TileEntityConnectorLV.java)      
       
       public int transferEnergy(int energy, boolean simulate, final int energyType)
	{
		int received = 0;
		if(!world.isRemote)
		{
			Set<AbstractConnection> outputs = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(Utils.toCC(this),
					world, true);
			int powerLeft = Math.min(maxvalue, energy);
			final int powerForSort = powerLeft;

			if(outputs.isEmpty())
				return 0;

			int sum = 0;
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
						int maxInput = maxvalue;
						tempR -= (int)Math.max(0, Math.floor(tempR*con.getPreciseLossRate(tempR, maxInput)));
						end.outputEnergy(tempR, simulate, energyType);
						HashSet<IImmersiveConnectable> passedConnectors = new HashSet<IImmersiveConnectable>();
						float intermediaryLoss = 0;
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
		float max = (1-loss)*this.energyStorage.getEnergyStored();
		Consumer<Float> extract = (energy) -> {
			this.energyStorage.modifyEnergyStored((int)(-energy/(1-loss)));
		};
		return new ImmutablePair<>(max, extract);
	}

        @Nullable
	@Override
	protected Pair<Float, Consumer<Float>> getOwnEnergy()
	{
		return getEnergyForConnection(null);
	}

}
