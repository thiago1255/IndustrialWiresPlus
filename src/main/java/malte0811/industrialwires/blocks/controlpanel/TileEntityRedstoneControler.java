/*
|| UNDER 'GNU General Public License v3.0'
|| File made by thiago1255 based (copied a lot) of files of mods 'Industrial Wires', and 'Immersive Engineering'.
||
|| (check github for credits of this mods:)
|| IW: https://github.com/malte0811/IndustrialWires
|| IE: https://github.com/BluSunrize/ImmersiveEngineering
*/
package malte0811.industrialwires.blocks.controlpanel;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.energy.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.energy.wires.redstone.RedstoneWireNetwork;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.IBlockBoundsIW.IBlockBoundsDirectional;
import malte0811.industrialwires.blocks.stuff.*;

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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.model.TRSRTransformation;

import javax.annotation.Nonnull;

import java.util.Optional;
import java.util.Set;

import static blusunrize.immersiveengineering.api.energy.wires.WireType.REDSTONE_CATEGORY;

public class TileEntityRedstoneControler extends TileEntityImmersiveConnectable implements ITickable, IBlockBoundsDirectional, IDirectionalTile, IRedstoneConnector, IPlayerInteraction  
{
// VARIABLES/CONS.: --------------------------------------
    EnumFacing facing = EnumFacing.NORTH;
    protected int redstoneChannel = 0;
    protected RedstoneWireNetwork wireNetwork = new RedstoneWireNetwork().add(this);
    boolean firstTick = true;
    private TileEntity te = null;

// NBT DATA: --------------------------------------
    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        facing = EnumFacing.VALUES[nbt.getInteger("facing")];
	    redstoneChannel = nbt.getInteger("redstoneChannel");
    }
       
    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
	    nbt.setInteger("facing", facing.getIndex());
	    nbt.setInteger("redstoneChannel", redstoneChannel);
    }

// ITICKABLE: --------------------------------------
    @Override
    public void update() {
        if (!world.isRemote) {
		        if((world.getTotalWorldTime()&31)==(pos.toLong()&31)) { return; }
                BlockPos sideBlock = pos.offset(this.facing, 1);
                te = world.getTileEntity(sideBlock);
				if(te instanceof TileEntityCurrentTransformer|| te instanceof TileEntityPotentiometer) {
				    wireNetwork.updateValues();
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
    public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset) {
        if(!REDSTONE_CATEGORY.equals(cableType.getCategory())) { return false; }        
	    return true;
    }

    @Override
    public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other) {
	    super.connectCable(cableType, target, other);
		RedstoneWireNetwork.updateConnectors(pos, world, wireNetwork);
    }

    @Override 
    public void removeCable(ImmersiveNetHandler.Connection connection) {
        super.removeCable(connection);
		wireNetwork.removeFromNetwork(this);
    }
  
    @Override
    public Vec3d getConnectionOffset(Connection con) { return new Vec3d(0.5, 0.5, 0.5); }
    
	@Override
	public boolean moveConnectionTo(Connection c, BlockPos newEnd) { return true; }
// REDSTONE WIRE: -------------------------------------------
    @Override
    public void setNetwork(RedstoneWireNetwork net) { wireNetwork = net; }

    @Override
    public RedstoneWireNetwork getNetwork() { return wireNetwork; }

    @Override
    public void onChange() { 
	    if(te instanceof TileEntityControlTransformerRs) {
            ((TileEntityControlTransformerRs) te).redstoneValueCoarse = wireNetwork!=null?wireNetwork.getPowerOutput(redstoneChannel): 0;
			((TileEntityControlTransformerRs) te).redstoneValueFine = wireNetwork!=null?wireNetwork.getPowerOutput(redstoneChannel+1): 0;
		} else {
		    return;
		}
    }
    
    @Override
    public void updateInput(byte[] signals) {
	    if((te instanceof TileEntityCurrentTransformer)&&(facing == down)) {
		    signals[redstoneChannel] = (byte)Math.max(((TileEntityCurrentTransformer)te).redstoneValueCoarse, signals[redstoneChannel]);
			signals[(redstoneChannel+1)] = (byte)Math.max(((TileEntityCurrentTransformer)te).redstoneValueFine, signals[(redstoneChannel+1)]);
		} else if((te instanceof TileEntityPotentiometer)&&(facing == up)) {
		    signals[redstoneChannel] = (byte)Math.max(((TileEntityPotentiometer)te).redstoneValueCoarse, signals[redstoneChannel]);
			signals[(redstoneChannel+1)] = (byte)Math.max(((TileEntityPotentiometer)te).redstoneValueFine, signals[(redstoneChannel+1)]);
		}
	}

    @Override
    public World getConnectorWorld() { return getWorld(); }
	
// GENERAL PROPERTYES: --------------------------------------           
	@Override
	public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
	    if(Utils.isHammer(heldItem)&&!world.isRemote){
		    if(redstoneChannel == 14) { 
		        redstoneChannel = 0; 
		    } else { 
		        redstoneChannel++; 
		    }
		    player.sendMessage(new TextComponentTranslation(IndustrialWires.MODID + ".chat.transformerRs", String.format("%s", nameOfColorOfWire())));
		    markDirty();
		    this.onChange();
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
    public int getFacingLimitation() { return 0; }

    @Override
    public boolean mirrorFacingOnPlacement(@Nonnull EntityLivingBase placer) { return true; }

    @Override
    public boolean canHammerRotate(@Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EntityLivingBase entity) { return false; }

    @Override
    public boolean canRotate(@Nonnull EnumFacing axis) { return false; }
    
// FINISH OF THIS CLASS ------------------------------------------------------------------------
}
