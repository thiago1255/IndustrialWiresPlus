/*
|| UNDER 'GNU General Public License v3.0'
|| File made by thiago based (copied a lot) of files of mods 'Industrial Wires', and 'Immersive Engineering'.
||
|| (check github for credits of this mods:)
|| IW: https://github.com/malte0811/IndustrialWires
|| IE: https://github.com/BluSunrize/ImmersiveEngineering
*/

package malte0811.industrialwires.blocks.stuff;

import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;

import malte0811.industrialwires.blocks.TileEntityIWBase;
import malte0811.industrialwires.blocks.IHasDummyBlocksIW;
import malte0811.industrialwires.crafting.RecipesSolidifier;
import malte0811.industrialwires.crafting.RecipesSolidifier.RecipeData;

import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.EnumFacing;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.entity.EntityLivingBase;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraft.item.ItemStack.areItemStacksEqual;

public class TileEntitySolidifier extends TileEntityIWBase implements IHasDummyBlocksIW, ITickable, IIEInventory, IDirectionalTile {
	// VARIABLES/CONS: ------------------------------------
	public FluidTank tank = new FluidTank(10000);
	public NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
	public boolean dummy = false;
	public int ticksPassed = 0;
	public RecipeData recipe = null;
	EnumFacing facing = EnumFacing.NORTH;;
	public TileEntitySolidifier masterTe = null;
	
	//utils ===============================================================================
	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		facing = EnumFacing.byHorizontalIndex(in.getInteger("facing"));
		ticksPassed = in.getInteger("ticksPassed");
		tank.readFromNBT(in.getCompoundTag("tank"));
		inventory = Utils.readInventory(in.getTagList("inventory", 10), 1);
		dummy = in.getBoolean("dummy");
	}

	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		out.setByte("facing",  (byte) facing.getHorizontalIndex());
		out.setInteger("ticksPassed", ticksPassed);
		out.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
		out.setTag("inventory", Utils.writeInventory(inventory));
		out.setBoolean("dummy", dummy);
	}

	@Override
	public void update() {
		if(masterTe == null) { 
			if(world.getTileEntity(pos.offset(EnumFacing.UP, dummy?-1:0)) instanceof TileEntitySolidifier) {
				masterTe = (TileEntitySolidifier)world.getTileEntity(pos.offset(EnumFacing.UP, dummy?-1:0));
			}
		}
		if(dummy) {return;}
		if(recipe == null) {return;}
		
		if(ticksPassed != 0) {
			ticksPassed += 1;
			if(ticksPassed >= recipe.time) {
				inventory.set(0, recipe.output.get());
				ticksPassed = 0;
			}
			this.markDirty();
			return;
		}
		
		if(!areItemStacksEqual(inventory.get(0), ItemStack.EMPTY)) {return;}
		if(tank.getFluidAmount() < recipe.input.get().amount) { return; }
		ticksPassed = 1;
		tank.drain(recipe.input.get().amount, true);
		this.markDirty();
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

	// INVENTORY: -----------------------------------------
	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return (capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && dummy && facing == EnumFacing.UP)
			|| (capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && !dummy && facing.getOpposite().rotateY() == this.facing)
			|| (super.hasCapability(capability, facing));
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && masterTe != null && dummy && facing == EnumFacing.UP) {
			return (T)thefluidhandler;
		}
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && !dummy && facing.getOpposite().rotateY() == this.facing) {
			return (T)theoutputhandler;
		}
		return super.getCapability(capability, facing);
	}
	
	@Override
	public NonNullList<ItemStack> getInventory() { return this.inventory; }
	
	@Override
	public boolean isStackValid(int slot, ItemStack stack) { return true; }
	
	@Override
	public int getSlotLimit(int slot) { return 64; }

	@Override
	public void doGraphicalUpdates(int slot) {}
	
	IItemHandler theoutputhandler = new IEInventoryHandler(1, this, 0, false, true);
	
	TheFluidHandler thefluidhandler = new TheFluidHandler(this);
	
	static class TheFluidHandler implements IFluidHandler {
		TileEntitySolidifier tes;

		TheFluidHandler(TileEntitySolidifier tes) { this.tes = tes; }
		
		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {return null;}
		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {return null;}
	
		@Override
		public int fill(FluidStack resource, boolean doFill) {
			if(tes.masterTe == null) {return 0;}
			if(resource==null || !tes.dummy) {return 0;}
			RecipeData r = RecipesSolidifier.recipeByInput(resource.getFluid());
			if(r == null) {return 0;}
			int i = tes.masterTe.tank.fill(resource, doFill);
			if(i > 0) {
				tes.masterTe.recipe = r;
				tes.masterTe.markDirty(); 
			}
			return i;
		}

		@Override
		public IFluidTankProperties[] getTankProperties() {
			IFluidTankProperties[] array = new IFluidTankProperties[1];
			if(tes.dummy) {
				array[0] = new FluidTankProperties(tes.masterTe.tank.getFluid(), tes.masterTe.tank.getCapacity());
			} else {
				array[0] = new FluidTankProperties(tes.tank.getFluid(), tes.tank.getCapacity());
			}
			return array;
		}
	}

	// DUMMY BLOCKS: --------------------------------------
	@Override
	public boolean isDummy() { return dummy; }

	@Override
	public void placeDummies(IBlockState state) {
		BlockPos pos2 = pos.offset(EnumFacing.UP, 1);
		world.setBlockState(pos2, state);
		TileEntity te = world.getTileEntity(pos2);
		if (te instanceof TileEntitySolidifier) {
			((TileEntitySolidifier) te).dummy = true;
			((TileEntitySolidifier) te).facing = this.facing;
		}
	}

	@Override
	public void breakDummies() {
		if (world.getTileEntity(pos.offset(EnumFacing.UP, dummy?-1:1)) instanceof TileEntitySolidifier) {
			world.setBlockToAir(pos.offset(EnumFacing.UP, dummy?-1:1)); 
		}
	}
	// END ------------------------------------------------
}