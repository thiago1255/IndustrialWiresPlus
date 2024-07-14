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

import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.LIGHT_ENGINEERING;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.HEAVY_ENGINEERING;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.RS_ENGINEERING;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice0.FLUID_PUMP;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1.FLUID_PIPE;
import static blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll.STEEL;
import static blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll.IRON;
import static blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_TreatedWood.HORIZONTAL;
import static malte0811.industrialwires.util.MiscUtils.offset;

public class TileEntityValveFabricator extends TileEntityIWMultiblock implements ITickable, IBlockBoundsIW, IPlayerInteraction, IHasDummyBlocksIW, IIEInternalFluxHandler, IIEInventory, IFluidHandler
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
	
	public boolean isPartConveyorBool = isPartConveyorVoid();
	
	public boolean active = false;
	
	public int tempId = 0;
	public boolean confirmedId = false;
	
	public NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY); //0-glass 1-component
	
	public FluidTank fuelTank = new FluidTank(10000);
	
	public FluidTank internalTank = new FluidTank(10000);
	
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
		facing = EnumFacing.byHorizontalIndex(in.getInteger(FACING));
		mBpos = in.getIntArray("offsetMb");
		isPartConveyorBool = isPartConveyorVoid();
		if(!isDummy()) {
		    ticksPassed = in.getInteger("ticksPassed");
			recipeId = in.getInteger("recipeId");
		    if (recipeId != 0) {
		        recipe = RecipesValveFabricator.getRecipeById(recipeId);
		    }
			ticksNeeded = recipe.time;
		    energyStorage.readFromNBT(in);
			fuelTank.readFromNBT(in.getCompoundTag("fuelTank"));
		    internalTank.readFromNBT(in.getCompoundTag("internalTank"));
			inventory = Utils.readInventory(in.getTagList("inventory", 10), 2);
			inverted = in.getBoolean("inverted");
		}
	}

	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		super.writeNBT(out, updatePacket);	
		out.setIntArray("offsetMb", mBpos);
		isPartConveyorBool = isPartConveyorVoid();
		if(!isDummy()) {
		    out.setInteger("ticksPassed", ticksPassed);
		    out.setInteger("recipeId", recipeId);
		    energyStorage.writeToNBT(out);
			out.setTag("fuelTank", fuelTank.writeToNBT(new NBTTagCompound()));
		    out.setTag("internalTank", internalTank.writeToNBT(new NBTTagCompound()));
			out.setTag("inventory", Utils.writeInventory(inventory));
			out.setBoolean("inverted", inverted);
		}
	}

	@Override
	public void update() {
	    if(world.isRemote) {return;}
        if(isDummy()) {return;}
		if(recipe == null) {return;}
		
		if((world.getRedstonePowerFromNeighbors(pos.offset(facing, 1).offset(facing.rotateY(), -1).add(0, -1, 0)) != 0) != inverted) {return;}
		
		if(ticksPassed != 0 && active) {
		    ticksPassed += 1;
		    if(ticksPassed >= ticksNeeded) {
			    ticksPassed = 0;
				Utils.dropStackAtPos(world, pos.offset(facing.rotateY(), -1), recipe.output.get(), facing.getOpposite().rotateY()); //conveyor -1
			}
			atualizar(); //Update to render.
			return;
		}
		if(
		    active &&
		    inventory.get(0) == recipe.inputGlass.get() &&
			inventory.get(1) == recipe.inputComponent.get() &&
			energyStorage.getEnergyStored() >= recipe.energy &&
			fuelTank.getFluid().getFluid() == recipe.fuel.get().getFluid() &&
			fuelTank.getFluidAmount() >= recipe.fuel.get().amount &&
			( recipe.internal.get() == null
			    ?internalTank.getFluid().getFluid() == recipe.internal.get().getFluid() && internalTank.getFluidAmount() >= recipe.internal.get().amount
				:true
			)
		) {
		    ticksPassed = 1; //Should be the first.
		    inventory.set(0, ItemStack.EMPTY);
			inventory.set(1, ItemStack.EMPTY);
			fuelTank.drain(recipe.fuel.get().amount, true);
			if(recipe.internal.get() != null) {
			    internalTank.drain(recipe.internal.get().amount, true);
			}
			energyStorage.modifyEnergyStored(-recipe.energy);
			atualizar(); //Update to render.
		}	
	}
	
	public void atualizar() {
	    this.markDirty();
	    IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 3);
		world.addBlockEvent(pos, state.getBlock(), 255, 0);
	}
	
	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
	    if(!isPartRedstonePort()) {return false;}
		if(world.isRemote) {return false;}
		if(!Utils.isHammer(heldItem)) {return false;}
		
		TileEntityValveFabricator masterTe = null;
		if(world.getTileEntity(getOrigin()) instanceof TileEntityValveFabricator) {
		    masterTe = (TileEntityValveFabricator)world.getTileEntity(getOrigin());
		}else{ return false; }
		
		if(masterTe.recipeId == 0) {
			if(player.isSneaking()) {
			    if(confirmedId) {
				    masterTe.recipeId = tempId;
					masterTe.recipe = RecipesValveFabricator.getRecipeById(tempId);
					masterTe.markDirty();
					player.sendMessage(new TextComponentTranslation(IndustrialWires.MODID + ".ValveFabricator.confirmedTempId", String.format("%s", tempId)));
				} else {
				    confirmedId = true;
					player.sendMessage(new TextComponentTranslation(IndustrialWires.MODID + ".ValveFabricator.askTempId"));
				}
			} else {
			    confirmedId = false;
				for(tempId++; tempId < RecipesValveFabricator.getMaxId()+1; tempId++) {
					if(RecipesValveFabricator.getRecipeById(tempId) != null) {continue;}
                }
				if(tempId > RecipesValveFabricator.getMaxId()) {tempId = 0;}
				player.sendMessage(new TextComponentTranslation(IndustrialWires.MODID + ".ValveFabricator.selectedTempId", String.format("%s", tempId)));
			}
			return true;
		}
		
		masterTe.inverted = !masterTe.inverted;
		masterTe.markDirty();
		ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(Lib.CHAT_INFO+"rsControl."+(masterTe.inverted?"invertedOn": "invertedOff")));
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
		return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	}
	
    @Override
	public Vec3i getSize() {
		return new Vec3i(3, 3, 3);
	}
	
	//Energy: ======================================================================
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
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing) {
		if(isPartEnergyInput()&&(facing==EnumFacing.UP))
			return wrapper;
		return null;
	}
    
    //Fluidhandler: ======================================================================== Based on: TileEntityWoodenBarrel.java
	
	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
		   && isPartFluidInput()
		   && facing.getOpposite() == this.facing) { return true; }
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && isPartFluidInput() && facing == this.facing) {
			TileEntityValveFabricator masterTe = null;
			if(world.getTileEntity(getOrigin()) instanceof TileEntityValveFabricator) {
		   		masterTe = (TileEntityValveFabricator)world.getTileEntity(getOrigin());
			} else {return super.getCapability(capability, facing);}
			if(mBpos[1]==1) {
				return (T)masterTe.fuelTank;
			} else {
				return (T)masterTe.internalTank;
			}
		}
		return super.getCapability(capability, facing);
	}
	
	@Override
	public int fill(FluidStack resource, boolean doFill) {
		TileEntityValveFabricator masterTe = null;
		if(world.getTileEntity(getOrigin()) instanceof TileEntityValveFabricator) {
		    masterTe = (TileEntityValveFabricator)world.getTileEntity(getOrigin());
		} else { return 0; }
		if( resource==null || !isPartFluidInput() || masterTe.recipe == null || 
			( mBpos[1] == 1
				?masterTe.recipe.fuel.get().getFluid() != resource.getFluid()
				:masterTe.recipe.internal.get().getFluid() != resource.getFluid()
			)
		) { return 0; }

		final int i = mBpos[1]==1 ?masterTe.fuelTank.fill(resource, doFill) :masterTe.internalTank.fill(resource, doFill);
		if(i > 0) {
			masterTe.markDirty();
			//masterTe.markContainingBlockForUpdate(null);
		}
		return i;
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {return null;}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {return null;}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		TileEntityValveFabricator masterTe = null;
		if(world.getTileEntity(getOrigin()) instanceof TileEntityValveFabricator) {
		    masterTe = (TileEntityValveFabricator)world.getTileEntity(getOrigin());
		} else {
			IFluidTankProperties[] array = new IFluidTankProperties[2];
			array[0] = new FluidTankProperties(fuelTank.getFluid(), fuelTank.getCapacity());
			array[1] = new FluidTankProperties(internalTank.getFluid(), internalTank.getCapacity());
			return array;
		}
		IFluidTankProperties[] array = new IFluidTankProperties[2];
		array[0] = new FluidTankProperties(masterTe.fuelTank.getFluid(), masterTe.fuelTank.getCapacity());
		array[1] = new FluidTankProperties(masterTe.internalTank.getFluid(), masterTe.internalTank.getCapacity());
		return array;
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
	
	public void onEntityCollision(World world, Entity entity) {
	    if(world.isRemote || entity==null || entity.isDead) {return;}
		if(!isPartConveyorVoid()) {return;}
		if(mBpos[2]==-1) {return;}
		
		if(!(entity instanceof EntityItem) || ((EntityItem)entity).getItem().isEmpty()) {return;}
		ItemStack stack = ((EntityItem)entity).getItem();
		if(stack.isEmpty()) {return;}
		
		TileEntityValveFabricator masterTe = null;
		if(world.getTileEntity(getOrigin()) instanceof TileEntityValveFabricator) {
		    masterTe = (TileEntityValveFabricator)world.getTileEntity(getOrigin());
		}else{ return; }
		if(masterTe.ticksPassed != 0) {return;}
		if(masterTe.recipe == null) {return;}
		
		final int itemsCount = mBpos[2]==1?masterTe.recipe.inputGlass.get().getCount():masterTe.recipe.inputComponent.get().getCount(); //if mBpos[2]==1, glass, else component
		final Item itemData = mBpos[2]==1?masterTe.recipe.inputGlass.get().getItem():masterTe.recipe.inputComponent.get().getItem();
		if(stack.getItem() != itemData){return;}

		if(masterTe.inventory.get(mBpos[2]==1?0:1).getCount() >= itemsCount) {return;}
		stack.shrink(1);
		if(stack.getCount() <= 0) { entity.setDead(); }
		masterTe.inventory.set(mBpos[2]==1?0:1, new ItemStack(itemData, inventory.get(mBpos[2]==1?0:1)==ItemStack.EMPTY?1:inventory.get(mBpos[2]==1?0:1).getCount()+1));
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
