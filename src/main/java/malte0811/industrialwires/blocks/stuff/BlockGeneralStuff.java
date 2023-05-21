/*
|| UNDER 'GNU General Public License v3.0'
|| File made by thiago based (copied a lot) of files of mods 'Industrial Wires', and 'Immersive Engineering'.
||
|| (check github for credits of this mods:)
|| IW: https://github.com/malte0811/IndustrialWires
|| IE: https://github.com/BluSunrize/ImmersiveEngineering
*/

package malte0811.industrialwires.blocks.stuff;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.common.util.Utils;
import malte0811.industrialwires.blocks.BlockIWBase;
import malte0811.industrialwires.blocks.IMetaEnum;
import malte0811.industrialwires.blocks.IPlacementCheck;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static malte0811.industrialwires.util.MiscUtils.offset;

public class BlockGeneralStuff extends BlockIWBase implements IMetaEnum, IPlacementCheck{
	public static final PropertyEnum<BlockTypes_GeneralStuff> type = PropertyEnum.create("type", BlockTypes_GeneralStuff.class);
	public static final String NAME = "general_stuff";

	public BlockGeneralStuff() {
		super(Material.IRON, NAME);
        setHardness(3.0F);
		setResistance(15.0F);
		lightOpacity = 0;
	}
      	
    @Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		BlockTypes_GeneralStuff[] values = BlockTypes_GeneralStuff.values();
		for (int i = 0; i < values.length; i++) {
			if(i == 1) { continue; }
		    list.add(new ItemStack(this, 1, i));
		}
	}
	
	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.TRANSLUCENT || layer == BlockRenderLayer.SOLID;
	}
		
    @Override
	protected IProperty<?>[] getProperties() {
		return new IProperty[] {IEProperties.MULTIBLOCKSLAVE, IEProperties.BOOLEANS[0], IEProperties.DYNAMICRENDER, IEProperties.FACING_HORIZONTAL, type};
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
	
	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
	    switch (state.getValue(type)) {
	        case CONTROL_TRANSFORMER_RS: return new TileEntityControlTransformerRs();
            case CONTROL_TRANSFORMER_NORMAL: return new TileEntityControlTransformerNormal();
            case VARISTOR: return new TileEntityVaristor();
		    case POTENTIOMETER: return new TileEntityPotentiometer();
		    case CURRENT_TRANSFORMER: return new TileEntityCurrentTransformer();
			case RETIFIER_VALVE: return new TileEntityRetifierValve();
		    default: return null;
	    }
	}
	
    @Override
	public boolean canPlaceBlockAt(World w, BlockPos pos, ItemStack stack, EntityPlayer p) {
        switch (stack.getItemDamage()) {
			case 0:
				if (!w.isAirBlock(pos.offset(EnumFacing.fromAngle(p.rotationYaw).rotateY(), 1))) {
					return false;
				}
				break;
		    case 1:
				if (!w.isAirBlock(pos.offset(EnumFacing.fromAngle(p.rotationYaw).rotateY(), -1))) {
					return false;
				}
				break;
            case 3: 
                if (!w.isAirBlock(pos.up(1))) {
				    return false;
			    }
                break;
			case 4:
				if (!w.isAirBlock(pos.down(1))) {
				    return false;
			    }
				break;
			case 5:
				for (int i = 1; i <= 1; i++) {
			        if (!w.isAirBlock(pos.down(i))) {
				        return false;
			        }
				}
				break;
		}
		return true;
	}

	@Override
	public BlockTypes_GeneralStuff[] getValues() {
		return BlockTypes_GeneralStuff.values();
	}
    
	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		state = super.getActualState(state, worldIn, pos);
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityRetifierValve) {
			state = state.withProperty(IEProperties.BOOLEANS[0], ((TileEntityRetifierValve) te).active);
		}
		return state;
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
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
}
