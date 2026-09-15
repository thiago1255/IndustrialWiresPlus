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
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.Utils;
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
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1.FLUID_PIPE;
import static blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll.IRON;
import static blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll.STEEL;
import static blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDecoration.ALLOYBRICK;
import static malte0811.industrialwires.IEObjects.*;
import static malte0811.industrialwires.util.MiscUtils.offset;

public class MultiblockMelter implements IMultiblock {
	public static MultiblockValveFabricator INSTANCE;
	private static final ItemStack[][][] structure = new ItemStack[3][2][2];
	
	public MultiblockValveFabricator() {
		ItemStack iron = new ItemStack(IEContent.blockSheetmetal, 1, IRON.getMeta());
		ItemStack pipe = new ItemStack(IEContent.blockMetalDevice1, 1, FLUID_PIPE.getMeta());
		ItemStack brick = new ItemStack(IEContent.blockStoneDecoration, 1, ALLOYBRICK.getMeta());
		//structure [height] [length] [width] //up forward right
		//1st layer
		structure[0][0][0] = iron;
		structure[0][0][1] = iron;

		structure[0][1][0] = iron;
		structure[0][1][1] = iron;
		//2nd layer
		structure[1][0][0] = pipe;
		structure[1][0][1] = iron;

		structure[1][1][0] = iron;
		structure[1][1][1] = iron;
		//3rd layer
		structure[2][0][0] = brick;
		structure[2][0][1] = brick;

		structure[2][1][0] = brick;
		structure[2][1][1] = brick;
	}
	
	@Override
	public ItemStack[][][] getStructureManual() { return structure; }

	@Override
	@SideOnly(Side.CLIENT)
	public boolean overwriteBlockRender(ItemStack stack, int iterator) { return false; }

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure() { return true; }

	static ItemStack renderStack = ItemStack.EMPTY;

	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack.isEmpty())
			renderStack = new ItemStack(IndustrialWires.mBstuff, 1, BlockTypes_StuffMultiblocks.MELTER.getMeta());
		GlStateManager.disableCull();
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}

	@Override
	public float getManualScale() { return 12; }

	@Override
	public String getUniqueName() { return "iw:melter"; }

	@SuppressWarnings("unchecked")
	@Override
	public boolean isBlockTrigger(IBlockState state) {
		return state.getBlock()==IEContent.blockMetalDevice1&&(state.getBlock().getMetaFromState(state)==FLUID_PIPE.getMeta());
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
		if(!Utils.isBlockAt(world,
			pos.offset(side, 1).add(0, -1, 0), 
			IEContent.blockStoneDecoration, ALLOYBRICK.getMeta())
		) {return false;}
		
		if(!Utils.isBlockAt(world,
			pos.offset(side, 1).offset(side.rotateY(), 1).add(0, -1, 0), 
			IEContent.blockStoneDecoration, ALLOYBRICK.getMeta())
		) {return false;}
		
		if(!Utils.isBlockAt(world,
			pos.add(0, -1, 0), 
			IEContent.blockStoneDecoration, ALLOYBRICK.getMeta())
		) {return false;}
		
		if(!Utils.isBlockAt(world,
			pos.offset(side.rotateY(), 1).add(0, -1, 0), 
			IEContent.blockStoneDecoration, ALLOYBRICK.getMeta())
		) {return false;}
		//layer 2
		if(!Utils.isBlockAt(world,
			pos.offset(side, 1), 
			IEContent.blockSheetmetal, IRON.getMeta())
		) {return false;}
		
		if(!Utils.isBlockAt(world,
			pos.offset(side, 1).offset(side.rotateY(), 1), 
			IEContent.blockSheetmetal, IRON.getMeta())
		) {return false;}
		
		if(!Utils.isBlockAt(world,
			pos, 
			IEContent.blockMetalDevice1, FLUID_PIPE.getMeta())
		) {return false;}
		
		if(!Utils.isBlockAt(world,
			pos.offset(side.rotateY(), 1), 
			IEContent.blockSheetmetal, IRON.getMeta())
		) {return false;}
		//layer 3
		if(!Utils.isBlockAt(world,
			pos.offset(side, 1).offset(side.rotateY(), 1).add(0, 1, 0), 
			IEContent.blockSheetmetal, IRON.getMeta())
		) {return false;}
		
		if(!Utils.isBlockAt(world,
			pos.offset(side, 1).add(0, 1, 0), 
			IEContent.blockSheetmetal, IRON.getMeta())
		) {return false;}
		
		if(!Utils.isBlockAt(world,
			pos.add(0, 1, 0), 
			IEContent.blockSheetmetal, IRON.getMeta())
		) {return false;}
		
		if(!Utils.isBlockAt(world,
			pos.offset(side.rotateY(), 1).add(0, 1, 0), 
			IEContent.blockSheetmetal, IRON.getMeta())
		) {return false;}
		
		ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER)?player.getHeldItemMainhand(): player.getHeldItemOffhand();
		if(MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer).isCanceled()) { return false; }
		
		IBlockState state = IndustrialWires.mBstuff.getStateFromMeta(BlockTypes_StuffMultiblocks.MELTER.getMeta());
		state = state.withProperty(IEProperties.FACING_HORIZONTAL, side);
		for(int yy = -1; yy <= 1; yy++) {
			for(int zz = 1; zz >= 0; zz--) {
				for(int xx = 0; xx <= 1; xx++) {
					BlockPos position = pos.offset(side, xx).offset(side.rotateY(), zz).add(0, yy, 0);
					world.setBlockState(position, state);
					TileEntity te = world.getTileEntity(position);
					if(te instanceof TileEntityMelter)
					{
						TileEntityMelter tileM = (TileEntityMelter)te;
						tileM.mBpos[0] = xx;
						tileM.mBpos[1] = yy;
						tileM.mBpos[2] = zz;
						tileM.offset = position.subtract(pos);
						tileM.facing = side;
						tileM.formed = true;
						tileM.markDirty();
					}
				}	
			}	
		}
			
		return true;
	}

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return new IngredientStack[]{
			new IngredientStack(new ItemStack(IEContent.blockMetalDevice1, 1, FLUID_PIPE.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockSheetmetal, 7, STEEL.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockStoneDecoration, 4, ALLOYBRICK.getMeta()))
		};
	}
}