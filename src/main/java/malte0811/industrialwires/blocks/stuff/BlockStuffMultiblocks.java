/*
|| UNDER 'GNU General Public License v3.0'
|| File made by thiago1255 based (copied a lot) of files of mods 'Industrial Wires', and 'Immersive Engineering'.
||
|| (check github for credits of this mods:)
|| IW: https://github.com/malte0811/IndustrialWires
|| IE: https://github.com/BluSunrize/ImmersiveEngineering
*/

package malte0811.industrialwires.blocks.stuff;

import blusunrize.immersiveengineering.api.IEProperties;
import malte0811.industrialwires.blocks.BlockIWMultiblock;
import malte0811.industrialwires.blocks.IMetaEnum;
import malte0811.industrialwires.blocks.IWProperties;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockStuffMultiblocks extends BlockIWMultiblock implements IMetaEnum {
	public static final PropertyEnum<BlockTypes_StuffMultiblocks> type = PropertyEnum.create("type", BlockTypes_StuffMultiblocks.class);
	public static final String NAME = "stuff_multiblock";
	public BlockStuffMultiblocks() {
		super(Material.IRON, NAME);
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {}

	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		    return super.canRenderInLayer(state, layer);
			//return layer==BlockRenderLayer.SOLID;
	}

	@Override
	protected IProperty[] getProperties() {
		return new IProperty[]{type, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL};
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		switch (state.getValue(type)) {
		case VALVE_FABRICATOR:
			return new TileEntityValveFabricator(state.getValue(IEProperties.FACING_HORIZONTAL));
		}
		return null;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(type).getMeta();
	}

	@Override
	public Object[] getValues() {
		return BlockTypes_StuffMultiblocks.values();
	}
}