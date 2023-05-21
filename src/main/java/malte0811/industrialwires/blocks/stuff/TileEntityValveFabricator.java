/*
|| UNDER 'GNU General Public License v3.0'
|| File made by thiago based (copied a lot) of files of mods 'Industrial Wires', and 'Immersive Engineering'.
||
|| (check github for credits of this mods:)
|| IW: https://github.com/malte0811/IndustrialWires
|| IE: https://github.com/BluSunrize/ImmersiveEngineering
*/

package malte0811.industrialwires.blocks.stuff;

import malte0811.industrialwires.blocks.IBlockBoundsIW;
import malte0811.industrialwires.blocks.ISyncReceiver;
import malte0811.industrialwires.blocks.IWProperties;
import malte0811.industrialwires.blocks.TileEntityIWMultiblock;
import malte0811.industrialwires.blocks.IHasDummyBlocksIW;
import malte0811.industrialwires.blocks.stuff.MultiblockValveFabricator;
import malte0811.industrialwires.util.MiscUtils;
import malte0811.industrialwires.IEObjects;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsIE;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.Utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.LIGHT_ENGINEERING;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.HEAVY_ENGINEERING;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.RS_ENGINEERING;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice0.FLUID_PUMP;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1.FLUID_PIPE;
import static blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll.STEEL;
import static blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll.IRON;
import static blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_TreatedWood.HORIZONTAL;
import static malte0811.industrialwires.util.MiscUtils.offset;

public class TileEntityValveFabricator extends TileEntityIWMultiblock implements ITickable, IBlockBoundsIW, IPlayerInteraction, IHasDummyBlocksIW, IIEInternalFluxHandler
{
    TileEntityValveFabricator(EnumFacing facing) {
		this.facing = facing;
	}
	public TileEntityValveFabricator() {}
	
	protected final static String FACING = "facing";
	
	public int[] mBpos = new int[3];
	
	public boolean isPartConveyorBool = isPartConveyorVoid();
	
	private boolean isPartConveyorVoid() {
	    if(mBpos[1] != 0) { return false; }
		if( (mBpos[0] == 1) && (mBpos[2] == 0) ) { return true; }
		if( (mBpos[0] == 0) && (mBpos[2] != 0) ) { return true; }
		return false;
	}
	
	private boolean isPartEnergyInput() {
	    if(mBpos[1] != 0) { return false; }
		if( (mBpos[0] == 1) && (mBpos[2] == -1) ) { return true; }
		return false;
	}
	
	private boolean isPartRedstonePort() {
	    return false;
	}
	
	//utils
	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket)
	{
		super.readNBT(in, updatePacket);
		facing = EnumFacing.byHorizontalIndex(in.getInteger(FACING));
		mBpos = in.getIntArray("offsetMb");
		isPartConveyorBool = isPartConveyorVoid();
		if(!isDummy()) {
		    energyStorage.readFromNBT(in);
		}
	}

	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket)
	{
		super.writeNBT(out, updatePacket);	
		out.setIntArray("offsetMb", mBpos);
		isPartConveyorBool = isPartConveyorVoid();
		if(!isDummy()) {
		    energyStorage.writeToNBT(out);
		}
	}

	@Override
	public void update(){
	}
	
	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
	    if(!isPartRedstonePort()) { return false; }
		return true;
	}
	
	//properties:
	@Nonnull
	@Override
	protected BlockPos getOrigin() {
		return pos.offset(facing, -mBpos[0]).offset(facing.rotateY(), -mBpos[2]).add(0, -mBpos[1], 0);
	}
	
	@Override
	public IBlockState getOriginalBlock() {
	    return Utils.getStateFromItemStack(MultiblockValveFabricator.INSTANCE.getStructureManual()[mBpos[1]+1][mBpos[0]+1][(-mBpos[2])+1]);
	}
	
	@Override
	public ItemStack getOriginalItem() {
		return MultiblockValveFabricator.INSTANCE.getStructureManual()[mBpos[1]+1][mBpos[0]+1][(-mBpos[2])+1];
	}
	
	@Override
	public void disassemble() {
        if(world.isRemote) {return;}	
	
	    if (isDummy()) {
		    if(formed) {
			    TileEntity tem = world.getTileEntity(getOrigin());
			    if (tem instanceof TileEntityValveFabricator) {
			        TileEntityValveFabricator tevfm = (TileEntityValveFabricator)tem;
			    	if(!tevfm.isDummy()) {
			    	    tevfm.disassemble();
			    	}
			    }
			}
			return;
		}
		
		if (!formed) { return; }
		
		for(int yy = -1; yy <= 2; yy++) {
		    if(yy==2) {
			    formed = false;
				markDirty();
				ItemStack drop = new ItemStack(IEContent.blockMetalDevice0, 1, FLUID_PUMP.getMeta());
				BlockPos positionDrop = pos.offset(facing, -1).offset(facing.rotateY(), -1);
		        world.spawnEntity(new EntityItem(world, positionDrop.getX()+.5,positionDrop.getY()+.5,positionDrop.getZ()+.5, drop));
				drop = Utils.copyStackWithAmount(ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID+":conveyor"), 3);
				positionDrop = pos.offset(facing, 1).offset(facing.rotateY(), 1);
				world.spawnEntity(new EntityItem(world, positionDrop.getX()+.5,positionDrop.getY()+.5,positionDrop.getZ()+.5, drop));
				world.setBlockState(pos, getOriginalBlock());
				return;
			}
			for(int zz = 1; zz >= -1; zz--) { 
				for(int xx = -1; xx <= 1; xx++) {
				    if((zz==1)&&(xx==1)) { continue; }
					if((yy==1)&&((xx!=0)||(zz!=0))) { continue; }
					if((xx==0)&&((yy==0)&&(zz==0))) { continue; }
					BlockPos position = pos.offset(facing, xx).offset(facing.rotateY(), zz).add(0, yy, 0);
					TileEntity te = world.getTileEntity(position);
			        if (te instanceof TileEntityValveFabricator) {
					    TileEntityValveFabricator tevf = (TileEntityValveFabricator)te;
						tevf.formed = false;
						tevf.markDirty();
						if(((xx == -1)&&(zz == -1))||tevf.isPartConveyorBool) {
						    world.setBlockToAir(position);
						} else {
					        world.setBlockState(position, tevf.getOriginalBlock());
						}
			        } 
		        }
			}
		}
	}
	
	@Override
	public AxisAlignedBB getBoundingBox() {
		return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	}
	
    @Override
	public Vec3i getSize() {
		return new Vec3i(3, 3, 3);
	}
	
	//Energy:
	public FluxStorage energyStorage = new FluxStorage(32000);
	
	@Nonnull
	@Override
	public FluxStorage getFluxStorage() {
		if(isDummy()) {
		    TileEntity tem = world.getTileEntity(getOrigin());
		    if (tem instanceof TileEntityValveFabricator) {
		        return ((TileEntityValveFabricator)tem).getFluxStorage();
		    }
		}
		return energyStorage;
	}
	
	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(EnumFacing facing) {
	    if(isPartEnergyInput()&&(facing==EnumFacing.UP)) {
		    return SideConfig.INPUT;
		}
		return SideConfig.NONE;
	}
	
	IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this, EnumFacing.UP);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing)
	{
		if(isPartEnergyInput()&&(facing==EnumFacing.UP))
			return wrapper;
		return null;
	}
	
	//Dummy blocks:
	@Override
    public boolean isDummy() { return mBpos[0]!=0||mBpos[1]!=0||mBpos[2]!=0; }
	   
    @Override
	public void placeDummies(IBlockState state) { }
    
    @Override
    public void breakDummies() { }
	
	@Override
	public boolean isLogicDummy() { return mBpos[0]!=0||mBpos[1]!=0||mBpos[2]!=0; }
}