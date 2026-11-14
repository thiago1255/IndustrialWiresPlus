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

import malte0811.industrialwires.blocks.TileEntityIWMultiblock;
import malte0811.industrialwires.blocks.IBlockBoundsIW;
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
import net.minecraft.util.math.AxisAlignedBB;

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

public class TileEntitySolidifier extends TileEntityIWMultiblock implements ITickable, IHasDummyBlocksIW, IIEInventory, IBlockBoundsIW {
	// VARIABLES/CONS: ------------------------------------
	TileEntitySolidifier(EnumFacing facing) {
		this.facing = facing;
	}
	public TileEntitySolidifier() {}

	public FluidTank tank = new FluidTank(10000);
	public NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
	public int ticksPassed = 0;
	public RecipeData recipe = null;
	public TileEntitySolidifier masterTe = null;
	public int[] mBpos = new int[3];

	//utils ===============================================================================
	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		super.readNBT(in, updatePacket);
		facing = EnumFacing.byHorizontalIndex(in.getInteger("facing"));
		int[] mb = in.getIntArray("offsetMb");
		if(mb.length == 3) {mBpos = mb;}
		int ri = in.hasKey("recipeIndex") ? in.getInteger("recipeIndex") : -1;
		recipe = ri >= 0 && ri < RecipesSolidifier.allRecipes.size() ? RecipesSolidifier.allRecipes.get(ri) : null;
		ticksPassed = in.getInteger("ticksPassed");
		tank.readFromNBT(in.getCompoundTag("tank"));
		inventory = Utils.readInventory(in.getTagList("inventory", 10), 1);
	}

	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		super.writeNBT(out, updatePacket);
		out.setByte("facing",  (byte) facing.getHorizontalIndex());
		out.setIntArray("offsetMb", mBpos);
		out.setInteger("recipeIndex", RecipesSolidifier.allRecipes.indexOf(recipe));
		out.setInteger("ticksPassed", ticksPassed);
		out.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
		out.setTag("inventory", Utils.writeInventory(inventory));
	}

	@Override
	public void update() {
		if(masterTe == null || masterTe.isInvalid()) { 
			if(world.getTileEntity(pos.offset(EnumFacing.UP, isDummy() ? -1 : 0)) instanceof TileEntitySolidifier) {
				masterTe = (TileEntitySolidifier)world.getTileEntity(pos.offset(EnumFacing.UP, isDummy() ? -1 : 0));
			}
		}
		if(world.isRemote) {return;}
		if(isDummy()) {return;}
		if(recipe == null) {return;}
		
		if(ticksPassed != 0) {
			ticksPassed += 1;
			if(ticksPassed >= recipe.time) {
				ItemStack out = recipe.output.get();
				inventory.set(0, out == null ? ItemStack.EMPTY : out);
				ticksPassed = 0;
			}
			this.markDirty();
			return;
		}
		
		if(!areItemStacksEqual(inventory.get(0), ItemStack.EMPTY)) {return;}

		FluidStack fs = tank.getFluid();
		if(fs == null) {return;}
		recipe = RecipesSolidifier.recipeByInput(fs.getFluid());
		if(recipe == null) {return;}

		if(tank.getFluidAmount() < recipe.input.get().amount) { return; }
		ticksPassed = 1;
		tank.drain(recipe.input.get().amount, true);
		this.markDirty();
	}

	@Override
	public AxisAlignedBB getBoundingBox() { return new AxisAlignedBB(0,0,0,1,1,1); }

	@Nonnull
	@Override
	protected BlockPos getOrigin() {
		return pos.add(0, -mBpos[1], 0);
	}

	@Override
	public IBlockState getOriginalBlock() {
		return Utils.getStateFromItemStack(getOriginalItem());
	}

	@Override
	public ItemStack getOriginalItem() {
		return MultiblockSolidifier.INSTANCE.getStructureManual()[mBpos[1]][0][0];
	}

	// INVENTORY: -----------------------------------------

	private boolean isOutputSide(@Nullable EnumFacing side) {
		if(side == null || side.getAxis() == EnumFacing.Axis.Y) {return false;}
		return side.getOpposite().rotateY() == this.facing;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (!formed) {return false;}
		return (capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && isDummy() && facing == EnumFacing.UP)
			|| (capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && !isDummy() && isOutputSide(facing) )
			|| (super.hasCapability(capability, facing));
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && formed && masterTe != null && isDummy() && facing == EnumFacing.UP) {
			return (T)thefluidhandler;
		}
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && formed && !isDummy() && isOutputSide(facing)) {
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
			if(resource==null || !tes.isDummy()) {return 0;}
			RecipeData r = RecipesSolidifier.recipeByInput(resource.getFluid());
			if(r == null) {return 0;}
			int i = tes.masterTe.tank.fill(resource, doFill);
			if(i > 0 && doFill && tes.masterTe.ticksPassed == 0) {
				tes.masterTe.recipe = r;
				tes.masterTe.markDirty(); 
			}
			return i;
		}

		@Override
		public IFluidTankProperties[] getTankProperties() {
			IFluidTankProperties[] array = new IFluidTankProperties[1];
			if(tes.isDummy() && tes.masterTe != null) {
				array[0] = new FluidTankProperties(tes.masterTe.tank.getFluid(), tes.masterTe.tank.getCapacity());
			} else {
				array[0] = new FluidTankProperties(tes.tank.getFluid(), tes.tank.getCapacity());
			}
			return array;
		}
	}

	// DUMMY BLOCKS: --------------------------------------
	@Override
	public boolean isDummy() { return mBpos[1] != 0; }

	@Override
	public boolean isLogicDummy() {return isDummy();}

	@Override
	public void placeDummies(IBlockState state) {}

	@Override
	public void breakDummies() {}

	@Override
	public void disassemble() {
		if(world.isRemote || !formed) {return;}

		if(isDummy()) {
			TileEntity te = world.getTileEntity(getOrigin());
			if (te instanceof TileEntitySolidifier ) {
					TileEntitySolidifier tes = (TileEntitySolidifier)te;
					if(!tes.isDummy()) { tes.disassemble(); }
			}
			return;
		}
		formed = false;
		markDirty();

		for(ItemStack stack : inventory) {
			if(!stack.isEmpty()) {Utils.dropStackAtPos(world, pos, stack);}
		}

		TileEntity top = world.getTileEntity(pos.up());
		if (top instanceof TileEntitySolidifier) {
			TileEntitySolidifier teso = (TileEntitySolidifier)top;
			teso.formed = false;
			teso.markDirty();
			world.setBlockState(pos.up(), teso.getOriginalBlock());
		}

		world.setBlockState(pos, getOriginalBlock());
	}
	// END ------------------------------------------------
}