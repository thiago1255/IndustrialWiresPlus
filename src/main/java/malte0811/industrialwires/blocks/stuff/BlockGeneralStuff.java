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
import malte0811.industrialwires.blocks.BlockIWBase;
import malte0811.industrialwires.blocks.IMetaEnum;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockGeneralStuff extends BlockIWBase implements IMetaEnum {
	public static final PropertyEnum<BlockTypes_GeneralStuff> type = PropertyEnum.create("type",BlockTypes_GeneralStuff.class);
	public static final String NAME = "general_stuff";

	public BlockGeneralStuff() {
		super(Material.IRON, NAME);
                setHardness(3.0F);
		setResistance(15.0F);
	}
      	
        @Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		BlockTypes_GeneralStuff[] values = BlockTypes_GeneralStuff.values();
		for (int i = 0; i < values.length; i++) {
		    list.add(new ItemStack(this, 1, i));
		}
	}
  
        @Override
	protected IProperty<?>[] getProperties() {
		return new IProperty[] {IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL, type};
	}

	@Override
	public boolean isTopSolid(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		switch (state.getValue(type)) {
			case CONTROL_TRANSFORMER_RS:
				return new ();
                        case CONTROL_TRANSFORMER_NORMAL:
				return new TileEntityControlTransformer();
                        case VARISTOR:
				return new TileEntityVaristor();
		}
		return null;
	}

	@Override
	public BlockTypes_GeneralStuff[] getValues() {
		return BlockTypes_GeneralStuff.values();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(type).ordinal();
	}

	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return super.getStateFromMeta(meta).withProperty(type, BlockTypes_GeneralStuff.values()[meta]);
	}
	
	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
		return state.getValue(type)==BlockTypes_GeneralStuff.CONTROL_TRANSFORMER;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
}
