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
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.LIGHT_ENGINEERING;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.HEAVY_ENGINEERING;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.RS_ENGINEERING;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice0.FLUID_PUMP;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1.FLUID_PIPE;
import static blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll.STEEL;
import static blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll.IRON;
import static blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_TreatedWood.HORIZONTAL;
import static blusunrize.immersiveengineering.api.energy.DieselHandler.isValidDrillFuel;
import static malte0811.industrialwires.util.MiscUtils.offset;

public class TileEntityValveFabricator extends TileEntityIWMultiblock implements ITickable, IBlockBoundsIW, IPlayerInteraction, IHasDummyBlocksIW, IIEInternalFluxHandler, IIEInventory
{
    TileEntityValveFabricator(EnumFacing facing) {
		this.facing = facing;
	}
	public TileEntityValveFabricator() {}
	
	protected final static String FACING = "facing";
	
	public int[] mBpos = new int[3];
	
	public int recipeId = 0;
	
	public int ticksPassed = 0;
	
	public int ticksNeeded = 0;
	
	public boolean inverted = false;
	
	public RecipeData recipe = null;
	
	public boolean isPartConveyorBool = false;
	
	public int tempId = 0;
	public boolean confirmedId = false;
	
	public NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY); //0-glass 1-component
	
	public FluidTank fuelTank = new FluidTank(10000);
	
	public FluidTank internalTank = new FluidTank(10000);
	
	public TileEntityValveFabricator masterTe = null;
	
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
	    return mBpos[0]==1 && mBpos[1]==-1 && mBpos[2]==-1;
	}
	
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
		recipeId = in.getInteger("recipeId");
		if (recipeId != 0) {
		    recipe = RecipesValveFabricator.getRecipeById(recipeId);
		}
		if(recipe != null) {
			ticksNeeded = recipe.time;
		}
		energyStorage.readFromNBT(in);
		fuelTank.readFromNBT(in.getCompoundTag("fuelTank"));
		internalTank.readFromNBT(in.getCompoundTag("internalTank"));
		inventory = Utils.readInventory(in.getTagList("inventory", 10), 2);
		inverted = in.getBoolean("inverted");
	}

	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		super.writeNBT(out, updatePacket);
		out.setByte("facing",  (byte) facing.getHorizontalIndex());
		out.setIntArray("offsetMb", mBpos);
		out.setInteger("ticksPassed", ticksPassed);
		out.setInteger("recipeId", recipeId);
		energyStorage.writeToNBT(out);
		out.setTag("fuelTank", fuelTank.writeToNBT(new NBTTagCompound()));
		out.setTag("internalTank", internalTank.writeToNBT(new NBTTagCompound()));
		out.setTag("inventory", Utils.writeInventory(inventory));
		out.setBoolean("inverted", inverted);
	}

	@Override
	public void update() {
		if(masterTe == null && world.getTileEntity(getOrigin()) instanceof TileEntityValveFabricator) {
			masterTe = (TileEntityValveFabricator)world.getTileEntity(getOrigin());
			isPartConveyorBool = isPartConveyorVoid();
		}
	    if(world.isRemote) {return;}
		if(recipe == null) {return;}
		if((world.getRedstonePowerFromNeighbors(pos.offset(facing, 1).offset(facing.rotateY(), -1).add(0, -1, 0)) != 0) != inverted) {return;}
		
		if(ticksPassed != 0) {
		    ticksPassed += 1;
		    if(ticksPassed >= ticksNeeded) {
			    ticksPassed = 0;
				Utils.dropStackAtPos(world, pos.offset(facing.rotateY(), -2), recipe.output.get(), facing.getOpposite().rotateY()); //conveyor -1
			}
			atualizar(); //Update to render.
			return;
		}
		
		if(!areItemStacksEqual(inventory.get(0), recipe.inputComponent.get())) {return;} 
		if(!areItemStacksEqual(inventory.get(1), recipe.inputGlass.get())) {return;}
		if(energyStorage.getEnergyStored() < recipe.energy) {return;}
		if(fuelTank.getFluidAmount() < recipe.fuel) {return;}
		if(recipe.internal != null) {
			if(internalTank.getFluidAmount() < recipe.internal.get().amount) {return;}
			internalTank.drain(recipe.internal.get().amount, true);
		}
		ticksPassed = 1; //Should be the first.
		inventory.set(0, ItemStack.EMPTY);
		inventory.set(1, ItemStack.EMPTY);
		fuelTank.drain(recipe.fuel, true);
		energyStorage.modifyEnergyStored(-recipe.energy);
		atualizar(); //Update to render.
	}

	public void atualizar() {
	    this.markDirty();
	    IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 3);
		world.addBlockEvent(pos, state.getBlock(), 255, 0);
	}
	
	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
		if(masterTe == null) {return false;}
	    if(!isPartRedstonePort()) {return false;}
		if(world.isRemote) {return false;}
		if(!Utils.isHammer(heldItem)) {return false;}
		
		if(masterTe.recipeId == 0) {
			if(player.isSneaking()) {
			    if(confirmedId) {
				    masterTe.recipeId = tempId;
					masterTe.recipe = RecipesValveFabricator.getRecipeById(tempId);
					masterTe.markDirty();
					player.sendMessage(new TextComponentTranslation(IndustrialWires.MODID + ".ValveFabricator.confirmedTempId", String.format("%s", tempId)));
				} else {
				    confirmedId = true;
					player.sendMessage(new TextComponentTranslation(IndustrialWires.MODID + ".ValveFabricator.askTempId", String.format("%s", tempId)));
				}
			} else {
				int max_id = RecipesValveFabricator.getMaxId();
				confirmedId = false;
				do {
					tempId++;
					if(tempId > max_id) {tempId = 1;}
				} while (RecipesValveFabricator.getRecipeById(tempId) == null);
				player.sendMessage(new TextComponentTranslation(IndustrialWires.MODID + ".ValveFabricator.selectedTempId", String.format("%s", tempId)));
			}
			return true;
		}
		
		if (player.isSneaking()) {
			masterTe.inverted = !masterTe.inverted;
			masterTe.markDirty();
			ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(Lib.CHAT_INFO+"rsControl."+(masterTe.inverted?"invertedOn": "invertedOff")));
		} else {
			ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(IndustrialWires.MODID + ".ValveFabricator.confirmedTempId", String.format("%s", masterTe.recipeId)));
		}
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
		if(isPartConveyorBool){
			return new AxisAlignedBB(0, 0, 0, 1, 0.0625, 1);
		}
		return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	}
	
	//Energy: ======================================================================
	public FluxStorage energyStorage = new FluxStorage(32000);
	
	@Nonnull
	@Override
	public FluxStorage getFluxStorage() {
		if(isDummy() && masterTe != null) {return masterTe.getFluxStorage();}
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
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing) {
		if(isPartEnergyInput()&&(facing==EnumFacing.UP))
			return wrapper;
		return null;
	}
    
    //Fluidhandler: ======================================================================== Based on: TileEntityWoodenBarrel.java[
	
	TheFluidHandler thefluidhandler = new TheFluidHandler(this);
	
	static class TheFluidHandler implements IFluidHandler {
		TileEntityValveFabricator tevf;

		TheFluidHandler(TileEntityValveFabricator tevf) { this.tevf = tevf; }
		
		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {return null;}
		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {return null;}
	
		@Override
		public int fill(FluidStack resource, boolean doFill) {
			if(tevf.masterTe == null) {return 0;}
			if(resource==null || !tevf.isPartFluidInput() || tevf.masterTe.recipe == null) {return 0;}
			int i = 0;
			if(tevf.mBpos[1] == 1) {
				if(!isValidDrillFuel(resource.getFluid())) {return 0;}
				i = tevf.masterTe.fuelTank.fill(resource, doFill);
			} else {
				if(tevf.masterTe.recipe.internal == null) {return 0;}
				if(tevf.masterTe.recipe.internal.get().getFluid() != resource.getFluid()) {return 0;}
				i = tevf.masterTe.internalTank.fill(resource, doFill);
			}
			if(i > 0) { tevf.masterTe.markDirty(); }
			return i;
		}

		@Override
		public IFluidTankProperties[] getTankProperties() {
			IFluidTankProperties[] array = new IFluidTankProperties[2];
			if(tevf.masterTe == null) {
				array[0] = new FluidTankProperties(tevf.fuelTank.getFluid(), tevf.fuelTank.getCapacity());
				array[1] = new FluidTankProperties(tevf.internalTank.getFluid(), tevf.internalTank.getCapacity());
			}
			array[0] = new FluidTankProperties(tevf.masterTe.fuelTank.getFluid(), tevf.masterTe.fuelTank.getCapacity());
			array[1] = new FluidTankProperties(tevf.masterTe.internalTank.getFluid(), tevf.masterTe.internalTank.getCapacity());
			return array;
		}
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
		   && isPartFluidInput()
		   && facing.getOpposite() == this.facing) { return true; }
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && masterTe != null && isPartFluidInput() && facing.getOpposite() == this.facing) {
			return (T)thefluidhandler;
		}
		return super.getCapability(capability, facing);
	}
    
	//Recipes: =============================================================================
	@Override
	public NonNullList<ItemStack> getInventory() { return this.inventory; }
	
	@Override
	public boolean isStackValid(int slot, ItemStack stack) { return true; }
	
	@Override
	public int getSlotLimit(int slot) { return 64; }

    @Override
	public void doGraphicalUpdates(int slot) {}
	
	public void entityInteractionWithBlock(World world, Entity entity) {
	    if(world.isRemote || entity==null) {return;}
		if(!isPartConveyorBool) {return;}
		if(mBpos[2]==-1) {return;}
		
		if(!(entity instanceof EntityItem)) {return;}
		ItemStack stack = ((EntityItem)entity).getItem();
		if(stack.isEmpty()) {return;}
		
		if(masterTe == null) {return;}
		if(masterTe.recipe == null) {return;}
		if(masterTe.ticksPassed != 0) {return;}
		
		final ItemStack itemData = mBpos[0]==0?masterTe.recipe.inputComponent.get():masterTe.recipe.inputGlass.get();
		if(stack.getItem() != itemData.getItem()){return;}

		if(masterTe.inventory.get(mBpos[0]).getCount() >= itemData.getCount()) {return;}
		stack.shrink(1);
		if(stack.getCount() <= 0) { entity.setDead(); }
		masterTe.inventory.set(mBpos[0], new ItemStack(itemData.getItem(), inventory.get(mBpos[0])==ItemStack.EMPTY?1:inventory.get(mBpos[0]).getCount()+1, itemData.getMetadata()));
	}
	
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

/*if( //fuelTank.getFluid().getFluid() == recipe.fuel.get().getFluid() && //internalTank.getFluid().getFluid() == recipe.internal.get().getFluid() &&
		    inventory.get(0) == recipe.inputGlass.get() &&
			inventory.get(1) == recipe.inputComponent.get() &&
			energyStorage.getEnergyStored() >= recipe.energy &&
			fuelTank.getFluidAmount() >= recipe.fuel.get().amount &&
			( recipe.internal == null
			    ?internalTank.getFluidAmount() >= recipe.internal.get().amount
				:true
			)
		) {
		    ticksPassed = 1; //Should be the first.
		    inventory.set(0, ItemStack.EMPTY);
			inventory.set(1, ItemStack.EMPTY);
			fuelTank.drain(recipe.fuel.get().amount, true);
			if(recipe.internal != null) {
			    internalTank.drain(recipe.internal.get().amount, true);
			}
			energyStorage.modifyEnergyStored(-recipe.energy);
			atualizar(); //Update to render.
		}*/