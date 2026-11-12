package malte0811.industrialwires.blocks.stuff;

import malte0811.industrialwires.blocks.IBlockBoundsIW;
import malte0811.industrialwires.blocks.TileEntityIWMultiblock;
import malte0811.industrialwires.blocks.IHasDummyBlocksIW;
import malte0811.industrialwires.crafting.RecipesMelter;
import malte0811.industrialwires.crafting.RecipesMelter.RecipeData;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;

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
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityMelter extends TileEntityIWMultiblock implements ITickable, IBlockBoundsIW, IPlayerInteraction, IHasDummyBlocksIW, IIEInventory {
	TileEntityMelter(EnumFacing facing) {
		this.facing = facing;
	}
	public TileEntityMelter() {}

	private static final int INPUT_SLOTS = 6;
	private static final int FUEL_SLOT = INPUT_SLOTS;

	public int[] mBpos = new int[3];

	public int ticksPassed = 0;

	public int ticksNeeded = 0;

	public int burnTime = 0;

	public RecipeData recipe = null;

	public NonNullList<ItemStack> inventory = NonNullList.withSize(INPUT_SLOTS + 1, ItemStack.EMPTY);

	public FluidTank outputTank = new FluidTank(20000);

	public TileEntityMelter masterTe = null;

	public boolean isPartItemInput() { return mBpos[1] == 1; }

	public boolean isPartFuelInput() { return mBpos[1] == -1; }

	public boolean isPartFluidOutput() { return !isDummy(); }

	//Utils
	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		super.readNBT(in, updatePacket);
		facing = EnumFacing.byHorizontalIndex(in.getByte("facing"));
		mBpos = in.getIntArray("offsetMb");
		ticksPassed = in.getInteger("ticksPassed");
		burnTime = in.getInteger("burnTime");
		int recipeIndex = in.hasKey("recipeIndex") ? in.getInteger("recipeIndex") : -1;
		recipe = (recipeIndex >= 0 && recipeIndex < RecipesMelter.allRecipes.size()) ? RecipesMelter.allRecipes.get(recipeIndex) : null;
		ticksNeeded = recipe != null ? recipe.time : 0;
		if(recipe == null) {ticksPassed = 0;}
		outputTank.readFromNBT(in.getCompoundTag("outputTank"));
		inventory = Utils.readInventory(in.getTagList("inventory", 10), INPUT_SLOTS + 1);
	}

	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		super.writeNBT(out, updatePacket);
		out.setByte("facing", (byte) facing.getHorizontalIndex());
		out.setIntArray("offsetMb", mBpos);
		out.setInteger("ticksPassed", ticksPassed);
		out.setInteger("burnTime", burnTime);
		out.setInteger("recipeIndex", RecipesMelter.allRecipes.indexOf(recipe));
		out.setTag("outputTank", outputTank.writeToNBT(new NBTTagCompound()));
		out.setTag("inventory", Utils.writeInventory(inventory));
	}

	@Override
	public void update() {
		if((masterTe == null || masterTe.isInvalid()) && world.getTileEntity(getOrigin()) instanceof TileEntityMelter) {
			masterTe = (TileEntityMelter)world.getTileEntity(getOrigin());
			markDirty();
		}
		if(world.isRemote) {return;}
		if(isDummy() || !formed) {return;}

		if(outputTank.getFluidAmount() > 0) {
			IFluidHandler out = FluidUtil.getFluidHandler(world, pos.offset(facing.getOpposite()), facing);
			if(out != null) {
				FluidStack moved = FluidUtil.tryFluidTransfer(out, outputTank, 100, true);
				if(moved != null && moved.amount != 0) {markDirty();}
			}
		}

		if(burnTime <= 0) {
			ItemStack fuel = inventory.get(FUEL_SLOT);
			if(fuel.isEmpty()) {return;}
			burnTime = TileEntityFurnace.getItemBurnTime(fuel);
			if(burnTime <= 0) {return;}
			ItemStack container = fuel.getItem().getContainerItem(fuel);
			fuel.shrink(1);
			if(fuel.isEmpty()) {inventory.set(FUEL_SLOT, container);}
		}
		burnTime--;
		markDirty();
		if(ticksPassed != 0) {
			ticksPassed++;
			if(ticksPassed >= ticksNeeded) {
				outputTank.fill(recipe.output.get(), true);
				ticksPassed = 0;
			}
			markDirty();
			return;
		}
		RecipeData found = RecipesMelter.recipeByInputs(inventory.subList(0, INPUT_SLOTS));
		if(found == null) {return;}
		FluidStack result = found.output.get();
		if(outputTank.fill(result, false) < result.amount) {return;}
		recipe = found;
		for(IngredientStack ing : recipe.inputs.get()) {
			int left = ing.inputSize;
			for(int i = 0; i < INPUT_SLOTS && left > 0; i++) {
				ItemStack stack = inventory.get(i);
				if(!ing.matchesItemStackIgnoringSize(stack)) {continue;}
				int taken = Math.min(left, stack.getCount());
				stack.shrink(taken);
				left -= taken;
				if(stack.isEmpty()) {inventory.set(i, ItemStack.EMPTY);}
			}
		}
		ticksNeeded = recipe.time;
		ticksPassed = 1;
		markDirty();
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
		if(masterTe == null) {return false;}
		if(world.isRemote) {return false;}

		if(heldItem.isEmpty()) {
			if(!player.isSneaking()) {return false;}
			boolean taken = false;
			for(int i = 0; i < masterTe.inventory.size(); i++) {
				ItemStack stack = masterTe.inventory.get(i);
				if(stack.isEmpty()) {continue;}
				masterTe.inventory.set(i, ItemStack.EMPTY);
				ItemHandlerHelper.giveItemToPlayer(player, stack);
				taken = true;
			}
			if(taken) {masterTe.markDirty();}
			return taken;
		}

		if(FluidUtil.getFluidHandler(heldItem) != null) {
			return isPartFluidOutput() && FluidUtil.interactWithFluidHandler(player, hand, masterTe.fluidHandler);
		}

		ItemStack rest = ItemHandlerHelper.insertItemStacked(masterTe.fuelHandler, heldItem.copy(), false);
		rest = ItemHandlerHelper.insertItemStacked(masterTe.inputHandler, rest, false);
		if(rest.getCount() == heldItem.getCount()) {return false;}
		if(!player.isCreative()) {player.setHeldItem(hand, rest);}
		masterTe.markDirty();
		return true;
	}

	// properties
	@Nonnull
	@Override
	protected BlockPos getOrigin() {
		return pos.offset(facing, -mBpos[0]).offset(facing.rotateY(), -mBpos[2]).add(0, -mBpos[1], 0);
	}

	@Override
	public IBlockState getOriginalBlock() {
		return Utils.getStateFromItemStack(getOriginalItem());
	}

	@Override
	public ItemStack getOriginalItem() {
		return MultiblockMelter.INSTANCE.getStructureManual()[mBpos[1]+1][mBpos[0]][mBpos[2]];
	}

	@Override
	public void disassemble() {
		if(world.isRemote) {return;}

		if(isDummy()) {
			if(formed) {
				TileEntity tem = world.getTileEntity(getOrigin());
				if (tem instanceof TileEntityMelter) {
					TileEntityMelter tevfm = (TileEntityMelter)tem;
					if(!tevfm.isDummy()) {
						tevfm.disassemble();
					}
				}
			}
			return;
		}

		if (!formed) {return;}
		formed = false;
		markDirty();


		for(ItemStack stack : inventory) {
			if(!stack.isEmpty()) {Utils.dropStackAtPos(world, pos, stack);}
		}

		for (int yy = -1; yy <= 1; yy++) {
			for (int zz = 1; zz >= 0; zz--) {
				for (int xx = 0; xx <= 1; xx++) {
					if((xx==0)&&(yy==0)&&(zz==0)) {continue;}
						BlockPos position = pos.offset(facing, xx).offset(facing.rotateY(), zz).add(0, yy, 0);
						TileEntity te = world.getTileEntity(position);
						if (te instanceof TileEntityMelter) {
							TileEntityMelter tevf = (TileEntityMelter)te;
							tevf.formed = false;
							tevf.markDirty();
							world.setBlockState(position, tevf.getOriginalBlock());
						}
				}
			}
		}
		world.setBlockState(pos, getOriginalBlock());
	}

	@Override
	public AxisAlignedBB getBoundingBox() { return new AxisAlignedBB(0,0,0,1,1,1); }

	// Fluids & items
	IFluidHandler fluidHandler = new TheFluidHandler(this);

	IItemHandler inputHandler = new IEInventoryHandler(INPUT_SLOTS, this, 0, true, false);

	IItemHandler fuelHandler = new IEInventoryHandler(1, this, FUEL_SLOT, true, false);

	static class TheFluidHandler implements IFluidHandler {
		TileEntityMelter tem;

		TheFluidHandler(TileEntityMelter tem) { this.tem = tem; }

		@Override
		public int fill(FluidStack resource, boolean doFill) {return 0;}

		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {
			if(resource == null || !resource.isFluidEqual(tem.outputTank.getFluid())) {return null;}
			return drain(resource.amount, doDrain);
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			FluidStack drained = tem.outputTank.drain(maxDrain, doDrain);
			if(drained != null && doDrain) {tem.markDirty();}
			return drained;
		}

		@Override
		public IFluidTankProperties[] getTankProperties() {
			return new IFluidTankProperties[] {new FluidTankProperties(tem.outputTank.getFluid(), tem.outputTank.getCapacity(), false, true)};
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if(masterTe == null || masterTe.isInvalid()) {return false;}
		if(!formed) {return false;}
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && isPartFluidOutput() && facing.getOpposite() == this.facing) {return true;}
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && (isPartItemInput() || isPartFuelInput())) {return true;}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if(masterTe != null) {
			if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && isPartFluidOutput() && facing.getOpposite() == this.facing) {return (T)masterTe.fluidHandler;}
			if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && isPartItemInput()) {return (T)masterTe.inputHandler;}
			if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && isPartFuelInput()) {return (T)masterTe.fuelHandler;}
		}
		return super.getCapability(capability, facing);
	}

	// recipes
	@Override
	public NonNullList<ItemStack> getInventory() {return this.inventory;}

	@Override
	public boolean isStackValid(int slot, ItemStack stack) {
		if(slot == FUEL_SLOT) {return TileEntityFurnace.getItemBurnTime(stack) > 0;}
		for(RecipeData re : RecipesMelter.allRecipes) {
			for(IngredientStack ing : re.inputs.get()) {
				if(ing.matchesItemStackIgnoringSize(stack)) {return true;}
			}
		}
		return false;
	}

	@Override
	public int getSlotLimit(int slot) {return 64;}

	@Override
	public void doGraphicalUpdates(int slot) {}

	// dummy
	@Override
	public boolean isDummy() {return mBpos[0]!=0||mBpos[1]!=0||mBpos[2]!=0;}

	@Override
	public void placeDummies(IBlockState state) {}

	@Override
	public void breakDummies() {}

	@Override
	public boolean isLogicDummy() {return isDummy();}
}