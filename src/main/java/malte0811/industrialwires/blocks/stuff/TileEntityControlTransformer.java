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
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
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
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.api.TargetingInfo;

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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.*;
import net.minecraft.block.state.IBlockState;

import net.minecraftforge.common.model.TRSRTransformation;

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

public class TileEntityControlTransformer extends TileEntityImmersiveConnectable implements ITickable, IIEInternalFluxHandler, IHasDummyBlocksIW, IBlockBoundsDirectional, IDirectionalTile  
{
// VARIABLES/CONS.: --------------------------------------
        private static final String SOUTH = "south";
        private static final String NORTH = "north";
        private static final String EAST = "east";
        private static final String WEST = "west";
        EnumFacing facing = EnumFacing.NORTH;
        public BlockPos endOfLeftConnection = null;
        private int quantityenergy = 0;
        private int dummy = 0;
        private int redstonevalue = 0;
        private int maxvalue = 0;
	private int wires = 0;
        private FluxStorage energyStorage = new FluxStorage(getMaxStorage(), getMaxInput(), getMaxOutput());
        
        boolean firstTick = true;

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
                if (isDummy()) { return; }
		if (!world.isRemote) { 
                    redstonevalue = world.getRedstonePowerFromNeighbors(pos);    
                    maxvalue = ((redstonevalue + 1)*2048); 
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
	public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset)
	{
		if(!cableType.isEnergyWire()) { return false; }
                if(isDummy()) { return false; }
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
		boolean isLeft = con.end.equals(endOfLeftConnection)||con.start.equals(endOfLeftConnection);
		Vec3d ret = mat.apply(new Vec3d(isLeft?.5: 1.5, 1.7, .5));
		return ret;
                */
                //return new Vec3d(0.5, 1.75, 0.5);
                boolean isLeft = con.end.equals(endOfLeftConnection)||con.start.equals(endOfLeftConnection);
                return new Vec3d(0.5, 1.7, isLeft?0.5: 1.5);
	}
//ENERGY STRG: --------------------------------------       
        private int getMaxStorage() { return 32768; }

	private int getMaxInput() { return maxvalue; }

	private int getMaxOutput() { return maxvalue; }

        @Override
	public int outputEnergy(int amount, boolean simulate, int energyType)
	{
           //need know what is
                if(amount > 0&&energyStorage < maximumStorage)
		{
			if(!simulate)
			{
				int rec = Math.min(maximumStorage-energyStorage, amount);
				energyStorage += rec;
				return rec;
			}
			return Math.min(maximumStorage-energyStorage, amount);
		}
		return 0;
	}
        
/* comment will make addAvailableEnergy from Main class ?
        @Override
	public void addAvailableEnergy(float amount, Consumer<Float> consume)
	{
            //THIS WILL BE CHANGED SOON
	    return amount;
        }
*/
        @Override
	public int getEnergyStored(EnumFacing from) { return energyStorage.getEnergyStored(); }

	@Override
	public int getMaxEnergyStored(EnumFacing from) { return getMaxStorage(); }
        
        @Override
	public boolean canConnectEnergy(EnumFacing from) { return false; }

        @Override
	public FluxStorage getFluxStorage() { return energyStorage; }

        @Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing) { return null; } 
        
        @Override
	public SideConfig getEnergySideConfig(EnumFacing facing) { return SideConfig.NONE; }
        
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

/*
public int transferEnergy(int energy, boolean simulate, final int energyType) {
	    int received = 0;
	    if(!world.isRemote) {
	        Set<AbstractConnection> outputs = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(Utils.toCC(this), world, true);
		int powerLeft = Math.min(Math.min(getMaxOutput(), getMaxInput()), energy);
		final int powerForSort = powerLeft;
		if(outputs.isEmpty()) { return 0; }
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
*/
// MATHMATIC NOTES: --------------------------------------  

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
