/*
|| UNDER 'GNU General Public License v3.0'
|| File made by thiago based (copied a lot) of files of mods 'Industrial Wires', and 'Immersive Engineering'.
||
|| (check github for credits of this mods:)
|| IW: https://github.com/malte0811/IndustrialWires
|| IE: https://github.com/BluSunrize/ImmersiveEngineering
*/

package malte0811.industrialwires.blocks.stuff;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.api.TargetingInfo;

import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.IBlockBoundsIW.IBlockBoundsDirectional;
import malte0811.industrialwires.blocks.TileEntityIWBase;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nonnull;

import static blusunrize.immersiveengineering.api.energy.wires.WireType.LV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.MV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.HV_CATEGORY;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.REDSTONE_CATEGORY;

public class TileEntityVaristor extends TileEntityImmersiveConnectable implements IBlockBoundsDirectional, IDirectionalTile {
	private static final String FACING = "facing";
	private boolean wireenergy = false;
	EnumFacing facing = EnumFacing.NORTH;

	@Override
	public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
            super.writeCustomNBT(nbt, descPacket);
	    nbt.setByte("facing",  (byte) facing.getHorizontalIndex());
	    nbt.setBoolean("wireenergy", wireenergy);
	}

	@Override
	public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
            super.readCustomNBT(nbt, descPacket);
            facing = EnumFacing.byHorizontalIndex(nbt.getByte("facing"));
	    wireenergy = nbt.getBoolean("wireenergy");
	}

	AxisAlignedBB aabb = null;
	@Override
	public AxisAlignedBB getBoundingBoxNoRot() {
		return new AxisAlignedBB(0.2f, 0, 0.2f, 0.8f, 1, 0.8f);
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
	
	@Override
        public boolean allowEnergyToPass(Connection con) { return false; }

        @Override
        public boolean isEnergyOutput() { return true; }

        @Override
        public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset) {
            if(wireenergy) { return false; }
	    if(!cableType.isEnergyWire()) { return false; }
	    if(!HV_CATEGORY.equals(cableType.getCategory())) { return false; }
	    return limitType==null||WireApi.canMix(cableType, limitType);
        }

        @Override
        public WireType getCableLimiter(TargetingInfo target) { return limitType; }

        @Override
        public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other) {
            if(this.limitType==null) { this.limitType = cableType; }
	    wireenergy = true;
        }

        @Override 
        public void removeCable(Connection connection) {
            wireenergy = false;
	    limitType = null;
        }
  
        @Override
        public Vec3d getConnectionOffset(Connection con) { return new Vec3d(0.5, 1, 0.5); }
}
