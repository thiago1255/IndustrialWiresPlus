package malte0811.industrialwires.blocks.stuff;

import blusunrize.immersiveengineering.api.IEProperties;
import malte0811.industrialwires.blocks.BlockIWBase;
import malte0811.industrialwires.blocks.IMetaEnum;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockGeneralStuff extends BlockIWBase implements IMetaEnum {
	public static final PropertyEnum<BlockTypes_GeneralStuff> PROPERTY = PropertyEnum.create("type",
			BlockTypes_GeneralStuff.class);
	public static final String NAME = "general_stuff";

	public BlockGeneralHV() {
		super(Material.IRON, NAME);
	}

	@Override
	protected IProperty[] getProperties() {
		return new IProperty[] {
				IEProperties.BOOLEANS[0], PROPERTY, IEProperties.FACING_HORIZONTAL
		};
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

	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		state = super.getActualState(state, worldIn, pos);
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityDischargeMeter)
			state = state.withProperty(IEProperties.BOOLEANS[0], ((TileEntityDischargeMeter) te).hasWire);
		return state;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		switch (state.getValue(PROPERTY)) {
			case CONTROL_TRANSFORMER;:
				return new TileEntityControlTransformer();
		}
		return null;
	}

	@Override
	public BlockTypes_GeneralHV[] getValues() {
		return BlockTypes_GeneralStuff.values();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(PROPERTY).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(PROPERTY, getValues()[meta]);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
