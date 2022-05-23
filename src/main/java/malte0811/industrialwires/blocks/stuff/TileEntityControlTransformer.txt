package malte0811.industrialwires.blocks.stuff;

import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.IBlockBoundsIW.IBlockBoundsDirectional;
import malte0811.industrialwires.blocks.IHasDummyBlocksIW;
import malte0811.industrialwires.IndustrialWires;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import malte0811.industrialwires.blocks.TileEntityIWBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;

import static blusunrize.immersiveengineering.api.energy.wires.WireApi.canMix;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.*;

import javax.annotation.Nonnull;

public class TileEntityControlTransformer extends TileEntityIWBase implements ITickable, IHasDummyBlocksIW, IPlayerInteraction, IImmersiveConnectable, IIEInternalFluxHandler, IBlockBoundsDirectional, IDirectionalTile {
	private static final String FACING = "facing";
        private static final String DUMY = "dummys";
        private static final String SOUTH = "south";
        private static final String NORTH = "north";
        private static final String EAST = "east";
        private static final String WEST = "west";
        private static final String WIRES = "wires";
	EnumFacing facing = EnumFacing.NORTH;
        private int dummy = 0;   
        public int wires = 0;
        public int redstonevalue = 0;
        public int maxvalue = 0;
        private FluxStorage energyStorage = new FluxStorage(32768, getMaxValue(), getMaxValue());
        protected WireType limitType = null;        

        @Override
	public void update() {
                int redstonevalue = 0;
                int maxvalue = 0; 

		if (isDummy() || world.isRemote) {
		    return;
		}

                redstonevalue = world.getRedstonePowerFromNeighbors(pos);        
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
                
                maxvalue = ((redstonevalue + 1)*2048);     
	}
        
	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		out.setByte(FACING, (byte) facing.getHorizontalIndex());
                out.setInteger(DUMY, dummy);
                out.setInteger(WIRES, wires);
                if(limitType!=null){ out.setString("limitType", limitType.getUniqueName()); }
		energyStorage.writeToNBT(out);
                
	}

	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		facing = EnumFacing.byHorizontalIndex(in.getByte(FACING));
		aabb = null;
                dummy = in.getInteger(DUMY);
                wires = in.getInteger(WIRES);
                if(in.hasKey("limitType")) { limitType = ApiUtils.getWireTypeFromNBT(in, "limitType"); }
		else {limitType = null;}
		energyStorage.readFromNBT(in);
	}

        @Override
	public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand,
							@Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) 
                {
			ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation("RS:", redstonevalue));
		}
		return true;
	}
        
        

        @Override
	public boolean isDummy() {
		return dummy != 0;
	}

        @Override
	public void placeDummies(IBlockState state) {
		for (int i = 1; i <= 1; i++) {
                        BlockPos pos2 = pos.offset(EnumFacing.WEST, i);
                        switch (facing) 
                        {
			     case SOUTH:
			            pos2 = pos.offset(EnumFacing.WEST, i);
                                    break;
                             case NORTH:
			            pos2 = pos.offset(EnumFacing.EAST, i);
                                    break;
			     case EAST:
			            pos2 = pos.offset(EnumFacing.SOUTH, i);
                                    break;
                             case WEST:
			            pos2 = pos.offset(EnumFacing.NORTH, i);
                                    break;
			}
			world.setBlockState(pos2, state);
                        TileEntity te = world.getTileEntity(pos2);
			if (te instanceof TileEntityControlTransformer) 
                        {
			    ((TileEntityControlTransformer) te).dummy = i;
			    ((TileEntityControlTransformer) te).facing = facing;
		        }
		}
	}

        @Override
	public void breakDummies() {
		for (int i = 0; i <= 1; i++) {
                        switch (facing) {
			       case SOUTH:
			              if (i != dummy && world.getTileEntity(pos.offset(EnumFacing.WEST, i - dummy)) instanceof TileEntityControlTransformer) {
				             world.setBlockToAir(pos.offset(EnumFacing.WEST, i - dummy));
			              }
                                      break;
                               case NORTH:
			              if (i != dummy && world.getTileEntity(pos.offset(EnumFacing.EAST, i - dummy)) instanceof TileEntityControlTransformer) {
				             world.setBlockToAir(pos.offset(EnumFacing.EAST, i - dummy));
			              }
                                      break;
			       case EAST:
			              if (i != dummy && world.getTileEntity(pos.offset(EnumFacing.SOUTH, i - dummy)) instanceof TileEntityControlTransformer) {
				             world.setBlockToAir(pos.offset(EnumFacing.SOUTH, i - dummy));
			              }
                                      break;
                               case WEST:
			              if (i != dummy && world.getTileEntity(pos.offset(EnumFacing.NORTH, i - dummy)) instanceof TileEntityControlTransformer) {
				             world.setBlockToAir(pos.offset(EnumFacing.NORTH, i - dummy));
			              }
                                      break;
			}
		}
	}

	AxisAlignedBB aabb = null;
	@Override
	public AxisAlignedBB getBoundingBoxNoRot() {
		return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		if (aabb==null) {
			aabb = IBlockBoundsDirectional.super.getBoundingBox();
		}
		return aabb;
	}

	@Nonnull
	@Override
	public EnumFacing getFacing() {
		return facing;
	}

	@Override
	public void setFacing(@Nonnull EnumFacing facing) {
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation() {
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(@Nonnull EntityLivingBase placer) {
		return false;
	}

	@Override
	public boolean canHammerRotate(@Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EntityLivingBase entity) {
		return false;
	}

	@Override
	public boolean canRotate(@Nonnull EnumFacing axis) {
		return false;
	}

        //energy:

        public int getMaxValue()
	{
		return maxvalue;
	}
        
        @Override
	public FluxStorage getFluxStorage()
	{
		return energyStorage;
	}
        
        @Override
	public boolean allowEnergyToPass(Connection con)
	{
		return false;
	}

	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset)
	{
		if(!cableType.isEnergyWire()) { return false; }
		if(LV_CATEGORY.equals(cableType.getCategory())) { return false; }
		if(MV_CATEGORY.equals(cableType.getCategory())) { return false; }
                if(!HV_CATEGORY.equals(cableType.getCategory())) { return false; }
		if(wires >= 2) { return false; }
		return limitType==null||WireApi.canMix(cableType, limitType);
	}

	@Override
	public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other)
	{
		if(this.limitType==null)
			this.limitType = cableType;
		wires++;
	}

	@Override
	public WireType getCableLimiter(TargetingInfo target)
	{
		return limitType;
	}

	@Override
	public void removeCable(Connection connection)
	{
		WireType type = connection!=null?connection.cableType: null;
		if(type==null)
                {
		    wires = 0;
		}
		else
		{
		    wires--;
		}
		if(wires <= 0)
		{
		    limitType = null;
		}
	}
        
        @Override
	public Vec3d getConnectionOffset(Connection con)
	{
		return new Vec3d(1, 1, 1);
	}

        @Override
	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		return getPos();
	}

        @Override
	public boolean isEnergyOutput()
	{
		return false;
	}

	@Override
	public int outputEnergy(int amount, boolean simulate, int energyType)
	{
		return 0;
	}

        @Override
	public boolean canConnect()
	{
		return true;
	}
}
