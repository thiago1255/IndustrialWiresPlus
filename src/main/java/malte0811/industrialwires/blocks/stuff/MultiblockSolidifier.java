package malte0811.industrialwires.blocks.stuff;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.Utils;

import malte0811.industrialwires.IndustrialWires;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0.RADIATOR;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice0.BARREL;

public class MultiblockSolidifier implements IMultiblock {
	public static MultiblockSolidifier INSTANCE;
	private static final ItemStack[][][] structure = new ItemStack[2][1][1];
	
	public MultiblockSolidifier() {
		structure[0][0][0] = new ItemStack(IEContent.blockMetalDecoration0, 1, RADIATOR.getMeta());
		structure[1][0][0] = new ItemStack(IEContent.blockMetalDevice0, 1, BARREL.getMeta());
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
			renderStack = new ItemStack(IndustrialWires.mBstuff, 1, BlockTypes_StuffMultiblocks.SOLIDIFIER.getMeta());
		GlStateManager.disableCull();
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}

	@Override
	public float getManualScale() { return 12; }

	@Override
	public String getUniqueName() { return "iw:solidifier"; }

	@SuppressWarnings("unchecked")
	@Override
	public boolean isBlockTrigger(IBlockState state) {
		return state.getBlock() == IEContent.blockMetalDecoration0 && state.getBlock().getMetaFromState(state) == RADIATOR.getMeta();
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		side = side.getAxis() == Axis.Y ? EnumFacing.fromAngle(player.rotationYaw) : side.getOpposite();
		if(!Utils.isBlockAt(world, pos.up(), IEContent.blockMetalDevice0, BARREL.getMeta())) return false;

		ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER) ? player.getHeldItemMainhand(): player.getHeldItemOffhand();
		if(MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer).isCanceled()) return false;

		IBlockState state = IndustrialWires.mBstuff.getStateFromMeta(BlockTypes_StuffMultiblocks.SOLIDIFIER.getMeta()).withProperty(IEProperties.FACING_HORIZONTAL, side);
		for(int yy = 0; yy <= 1; yy++) {
			BlockPos position = pos.add(0, yy, 0);
			world.setBlockState(position, state);
			TileEntity te = world.getTileEntity(position);
			if(!(te instanceof TileEntitySolidifier)) continue;
			TileEntitySolidifier tileS = (TileEntitySolidifier)te;
			tileS.mBpos[0] = 0;
			tileS.mBpos[1] = yy;
			tileS.mBpos[2] = 0;
			tileS.offset = position.subtract(pos);
			tileS.facing = side;
			tileS.formed = true;
			tileS.markDirty();
		}
		return true;
	}

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return new IngredientStack[]{
			new IngredientStack( new ItemStack(IEContent.blockMetalDecoration0, 1, RADIATOR.getMeta()) ),
			new IngredientStack( new ItemStack(IEContent.blockMetalDevice0, 1, BARREL.getMeta()) )
		};
	}
}