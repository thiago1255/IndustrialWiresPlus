/*
|| UNDER 'GNU General Public License v3.0'
|| File made by thiago based (copied a lot) of files of mods 'Industrial Wires', and 'Immersive Engineering'.
||
|| (check github for credits of this mods:)
|| IW: https://github.com/malte0811/IndustrialWires
|| IE: https://github.com/BluSunrize/ImmersiveEngineering
*/

package malte0811.industrialwires.blocks.stuff;

import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.IBlockBoundsIW;
import malte0811.industrialwires.blocks.ISyncReceiver;
import malte0811.industrialwires.blocks.IWProperties;
import malte0811.industrialwires.blocks.TileEntityIWMultiblock;
import malte0811.industrialwires.blocks.IHasDummyBlocksIW;
import malte0811.industrialwires.blocks.stuff.MultiblockValveFabricator;
import malte0811.industrialwires.crafting.RecipesValveFabricator;
import malte0811.industrialwires.crafting.RecipesValveFabricator.RecipeData;
import malte0811.industrialwires.util.MiscUtils;
import malte0811.industrialwires.IEObjects;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsIE;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import blusunrize.immersiveengineering.common.util.ChatUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
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
import net.minecraft.util.text.TextComponentTranslation;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraft.item.ItemStack.areItemStacksEqual;

import static malte0811.industrialwires.util.MiscUtils.offset;

public class TileEntityValveMelter extends TileEntityIWMultiblock implements ITickable, IBlockBoundsIW, IPlayerInteraction, IHasDummyBlocksIW, IIEInventory {
    TileEntityValveMelter(EnumFacing facing) {
		this.facing = facing;
	}
	public TileEntityValveMelter() {}
	
	public int[] mBpos = new int[3];
	
	public NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY); //0-glass 1-component
	
	public FluidTank outputTank = new FluidTank(10000);
	
	public TileEntityValveMelter masterTe = null;
	
	private boolean isPartFluidInput() {
	    return mBpos[1] == 1 || (mBpos[0] == -1 && mBpos[1] == -1 && mBpos[2] == 1);
	}
	
	//utils ===============================================================================
	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		super.readNBT(in, updatePacket);
		facing = EnumFacing.byHorizontalIndex(in.getByte("facing"));
		mBpos = in.getIntArray("offsetMb");
		ticksPassed = in.getInteger("ticksPassed");
		if(recipe != null) {
			ticksNeeded = recipe.time;
		}
		outputTank.readFromNBT(in.getCompoundTag("outputTank"));
		inventory = Utils.readInventory(in.getTagList("inventory", 10), 2);
	}

	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		super.writeNBT(out, updatePacket);
		out.setByte("facing",  (byte) facing.getHorizontalIndex());
		out.setIntArray("offsetMb", mBpos);
		out.setInteger("ticksPassed", ticksPassed);
		out.setTag("outputTank", outputTank.writeToNBT(new NBTTagCompound()));
		out.setTag("inventory", Utils.writeInventory(inventory));
	}

	@Override
	public void update() {
		if(masterTe == null && world.getTileEntity(getOrigin()) instanceof TileEntityValveMelter) {
			masterTe = (TileEntityValveMelter)world.getTileEntity(getOrigin());
		}
	    if(world.isRemote) {return;}
	}
	
	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
		if(masterTe == null) {return false;}
		if(world.isRemote) {return false;}

		return true;
	}
	
	//properties: ====================================================================================
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
		return MultiblockMelter.INSTANCE.getStructureManual()[mBpos[1]+1][mBpos[0]+1][(-mBpos[2])+1];
	}
	
	@Override
	public void disassemble() {
        if(world.isRemote) {return;}	
	
	    if (isDummy()) {
		    if(formed) {
			    TileEntity tem = world.getTileEntity(getOrigin());
			    if (tem instanceof TileEntityValveMelter) {
			        TileEntityValveMelter tevfm = (TileEntityValveMelter)tem;
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
			        if (te instanceof TileEntityValveMelter) {
					    TileEntityValveMelter tevf = (TileEntityValveMelter)te;
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
	public AxisAlignedBB getBoundingBox() { return new AxisAlignedBB(0, 0, 0, 1, 1, 1); }
    
    //Fluids and items =====================================================================
    
	//Recipes: =============================================================================
	@Override
	public NonNullList<ItemStack> getInventory() { return this.inventory; }
	
	@Override
	public boolean isStackValid(int slot, ItemStack stack) { return true; }
	
	@Override
	public int getSlotLimit(int slot) { return 64; }

    @Override
	public void doGraphicalUpdates(int slot) {}
	
	//Dummy blocks: ========================================================================
	@Override
    public boolean isDummy() { return mBpos[0]!=0||mBpos[1]!=0||mBpos[2]!=0; }
	   
    @Override
	public void placeDummies(IBlockState state) { }
    
    @Override
    public void breakDummies() { }
	
	@Override
	public boolean isLogicDummy() { return mBpos[0]!=0||mBpos[1]!=0||mBpos[2]!=0; }
}