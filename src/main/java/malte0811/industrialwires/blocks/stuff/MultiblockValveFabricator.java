/*
|| UNDER 'GNU General Public License v3.0'
|| File made by thiago based (copied a lot) of files of mods 'Industrial Wires', and 'Immersive Engineering'.
||
|| (check github for credits of this mods:)
|| IW: https://github.com/malte0811/IndustrialWires
|| IE: https://github.com/BluSunrize/ImmersiveEngineering
*/

package malte0811.industrialwires.blocks.stuff;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.client.ClientUtils;

import malte0811.industrialwires.IEObjects;
import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.IWProperties;
import malte0811.industrialwires.client.ClientUtilsIW;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static blusunrize.immersiveengineering.api.IEProperties.*;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.LIGHT_ENGINEERING;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.HEAVY_ENGINEERING;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.RS_ENGINEERING;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice0.FLUID_PUMP;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1.FLUID_PIPE;
import static blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll.STEEL;
import static blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll.IRON;
import static blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_TreatedWood.HORIZONTAL;
import static malte0811.industrialwires.IEObjects.*;
import static malte0811.industrialwires.util.MiscUtils.offset;

public class MultiblockValveFabricator implements IMultiblock {
	public static MultiblockValveFabricator INSTANCE;
	private static final ItemStack[][][] structure = new ItemStack[3][3][3];
	
	public MultiblockValveFabricator() {
       //structure [height] [length] [width] //up forward right
	   //1st layer
	   structure[0][0][0] = new ItemStack(IEContent.blockMetalDecoration0, 1, LIGHT_ENGINEERING.getMeta());//le
	   structure[0][0][1] = new ItemStack(IEContent.blockMetalDecoration0, 1, LIGHT_ENGINEERING.getMeta());//le
	   structure[0][0][2] = new ItemStack(IEContent.blockMetalDevice0, 1, FLUID_PUMP.getMeta());//pump
		   
	   structure[0][1][0] = new ItemStack(IEContent.blockTreatedWood, 1, HORIZONTAL.getMeta());//wood
	   structure[0][1][1] = new ItemStack(IEContent.blockMetalDecoration0, 1, HEAVY_ENGINEERING.getMeta());//he
	   structure[0][1][2] = new ItemStack(IEContent.blockTreatedWood, 1, HORIZONTAL.getMeta());//wood
		   
	   structure[0][2][1] = new ItemStack(IEContent.blockMetalDecoration0, 1, LIGHT_ENGINEERING.getMeta());//le
	   structure[0][2][2] = new ItemStack(IEContent.blockMetalDecoration0, 1, RS_ENGINEERING.getMeta());//rs

	   //2nd layer
	   structure[1][0][0] = new ItemStack(IEContent.blockSheetmetal, 1, STEEL.getMeta());//steel
	   structure[1][0][1] = new ItemStack(IEContent.blockMetalDevice1, 1, FLUID_PIPE.getMeta());//pipe
	   structure[1][0][2] = new ItemStack(IEContent.blockMetalDevice0, 1, FLUID_PUMP.getMeta());//pump
		   
	   structure[1][1][0] = ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID+":conveyor");//conv
	   structure[1][1][1] = new ItemStack(IEContent.blockMetalDecoration0, 1, HEAVY_ENGINEERING.getMeta());//he
	   structure[1][1][2] = ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID+":conveyor");//conv
		   
	   structure[1][2][1] = ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID+":conveyor");//conv
	   structure[1][2][2] = new ItemStack(IEContent.blockMetalDecoration0, 1, LIGHT_ENGINEERING.getMeta());//le
		   
       //3rd layer
	   structure[2][1][1] = new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.IRON.getMeta());//iron
	}
	
	@Override
	public ItemStack[][][] getStructureManual() { return structure; }

	@Override
	@SideOnly(Side.CLIENT)
	public boolean overwriteBlockRender(ItemStack stack, int iterator) {
		if (stack == structure[1][0][2]) {
			ImmersiveEngineering.proxy.drawFluidPumpTop();
			return true;
		}
		if (stack == ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID+":conveyor")) {
		    ImmersiveEngineering.proxy.drawConveyorInGui("immersiveengineering:conveyor", EnumFacing.EAST);
		}
			return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure() { return true; }

	static ItemStack renderStack = ItemStack.EMPTY;

	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack.isEmpty())
			renderStack = new ItemStack(IndustrialWires.mBstuff, 1, BlockTypes_StuffMultiblocks.VALVE_FABRICATOR.getMeta());
		GlStateManager.disableCull();
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}

	@Override
	public float getManualScale() { return 12; }

	@Override
	public String getUniqueName() { return "iw:valve_fabricator"; }

	@SuppressWarnings("unchecked")
	@Override
	public boolean isBlockTrigger(IBlockState state) {
		return state.getBlock()==IEContent.blockMetalDecoration0&&(state.getBlock().getMetaFromState(state)==HEAVY_ENGINEERING.getMeta());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		if(side.getAxis()==Axis.Y) {
			side = EnumFacing.fromAngle(player.rotationYaw);
		} else {
			side = side.getOpposite();
		}
		/*
		 *BlockPos position = pos.offset(dir, l).offset(dir.rotateY(), ww).add(0, h, 0); "[h] [l] [w] | [up] [forward] [right]"
		 *if(!Utils.isBlockAt(world, position, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())) {return false;}
		 *if(!ConveyorHandler.isConveyor(world, position, ImmersiveEngineering.MODID+":conveyor", null)) {return false;}
         */
		//^from MultiblockAutoWorkbench.java^\\
		//layer 1
		BlockPos position = pos.offset(side, -1).offset(side.rotateY(), 1).add(0, -1, 0);
		if(!Utils.isBlockAt(world, position, IEContent.blockMetalDecoration0, LIGHT_ENGINEERING.getMeta())) {return false;}
		position = pos.offset(side, -1).offset(side.rotateY(), 0).add(0, -1, 0);
		if(!Utils.isBlockAt(world, position, IEContent.blockMetalDecoration0, LIGHT_ENGINEERING.getMeta())) {return false;}
		position = pos.offset(side, -1).offset(side.rotateY(), -1).add(0, -1, 0);
		if(!Utils.isBlockAt(world, position, IEContent.blockMetalDevice0, FLUID_PUMP.getMeta())) {return false;}
		
		position = pos.offset(side, 0).offset(side.rotateY(), 1).add(0, -1, 0);
		if(!Utils.isBlockAt(world, position, IEContent.blockTreatedWood, HORIZONTAL.getMeta())) {return false;}
		position = pos.offset(side, 0).offset(side.rotateY(), 0).add(0, -1, 0);
		if(!Utils.isBlockAt(world, position, IEContent.blockMetalDecoration0, HEAVY_ENGINEERING.getMeta())) {return false;}
		position = pos.offset(side, 0).offset(side.rotateY(), -1).add(0, -1, 0);
		if(!Utils.isBlockAt(world, position, IEContent.blockTreatedWood, HORIZONTAL.getMeta())) {return false;}
		
		position = pos.offset(side, 1).offset(side.rotateY(), 0).add(0, -1, 0);
		if(!Utils.isBlockAt(world, position, IEContent.blockMetalDecoration0, LIGHT_ENGINEERING.getMeta())) {return false;}
		position = pos.offset(side, 1).offset(side.rotateY(), -1).add(0, -1, 0);
		if(!Utils.isBlockAt(world, position, IEContent.blockMetalDecoration0, RS_ENGINEERING.getMeta())) {return false;}
		
		//layer 2
		position = pos.offset(side, -1).offset(side.rotateY(), 1);
		if(!Utils.isBlockAt(world, position, IEContent.blockSheetmetal, STEEL.getMeta())) {return false;}
		position = pos.offset(side, -1).offset(side.rotateY(), 0);
		if(!Utils.isBlockAt(world, position, IEContent.blockMetalDevice1, FLUID_PIPE.getMeta())) {return false;}
		position = pos.offset(side, -1).offset(side.rotateY(), -1);
		if(!Utils.isBlockAt(world, position, IEContent.blockMetalDevice0, FLUID_PUMP.getMeta())) {return false;}
		
		position = pos.offset(side, 0).offset(side.rotateY(), 1);
		if(!ConveyorHandler.isConveyor(world, position, ImmersiveEngineering.MODID+":conveyor", null)) {return false;}
		position = pos.offset(side, 0).offset(side.rotateY(), 0);
	    if(!Utils.isBlockAt(world, position, IEContent.blockMetalDecoration0, HEAVY_ENGINEERING.getMeta())) {return false;}
		position = pos.offset(side, 0).offset(side.rotateY(), -1);
		if(!ConveyorHandler.isConveyor(world, position, ImmersiveEngineering.MODID+":conveyor", null)) {return false;}
		
		position = pos.offset(side, 1).offset(side.rotateY(), 0);
		if(!ConveyorHandler.isConveyor(world, position, ImmersiveEngineering.MODID+":conveyor", null)) {return false;}
		position = pos.offset(side, 1).offset(side.rotateY(), -1);
		if(!Utils.isBlockAt(world, position, IEContent.blockMetalDecoration0, LIGHT_ENGINEERING.getMeta())) {return false;}
		
        //layer 3
		position = pos.offset(side, 0).offset(side.rotateY(), 0).add(0, 1, 0);
		if(!Utils.isBlockAt(world, position, IEContent.blockSheetmetal, IRON.getMeta())) {return false;}
		
		ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER)?player.getHeldItemMainhand(): player.getHeldItemOffhand();
		if(MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer).isCanceled()) { return false; }
		
		IBlockState state = IndustrialWires.mBstuff.getStateFromMeta(BlockTypes_StuffMultiblocks.VALVE_FABRICATOR.getMeta());
		state = state.withProperty(IEProperties.FACING_HORIZONTAL, side);
		for(int yy = -1; yy <= 1; yy++) {
			for(int zz = 1; zz >= -1; zz--) {
				for(int xx = -1; xx <= 1; xx++) {
					if((zz==1)&&(xx==1)) { continue; }
					if((yy==1)&&((xx!=0)||(zz!=0))) { continue; }
					position = pos.offset(side, xx).offset(side.rotateY(), zz).add(0, yy, 0);
					world.setBlockState(position, state);
					TileEntity te = world.getTileEntity(position);
					if(te instanceof TileEntityValveFabricator)
					{
						TileEntityValveFabricator tileV = (TileEntityValveFabricator)te;
						tileV.formed = true;
						tileV.mBpos[0] = xx;
						tileV.mBpos[1] = yy;
						tileV.mBpos[2] = zz;
						tileV.offset = position.subtract(pos);
						tileV.facing = side;
						tileV.markDirty();
					}
				}	
			}	
		}
			
		return true;
	}
	
	static final IngredientStack[] materials = new IngredientStack[]{
		    new IngredientStack(Utils.copyStackWithAmount(ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID+":conveyor"), 3)),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, RS_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 2, HEAVY_ENGINEERING.getMeta())),
		    new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 4, LIGHT_ENGINEERING.getMeta())),
		    new IngredientStack(new ItemStack(IEContent.blockTreatedWood, 2, HORIZONTAL.getMeta())),
		    new IngredientStack(new ItemStack(IEContent.blockMetalDevice0, 1, FLUID_PUMP.getMeta())),
		    new IngredientStack(new ItemStack(IEContent.blockMetalDevice1, 1, FLUID_PIPE.getMeta())),
		    new IngredientStack(new ItemStack(IEContent.blockSheetmetal, 1, STEEL.getMeta())),
		    new IngredientStack(new ItemStack(IEContent.blockSheetmetal, 1, IRON.getMeta()))
	};

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}
}