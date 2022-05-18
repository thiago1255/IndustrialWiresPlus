/*
Made using other files of this mod and files of immersive engineering mod as example/model.
*/

package malte0811.industrialwires.blocks.stuff;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_GeneralStuff implements IStringSerializable, BlockIEBase.IBlockEnum{
	CONTROL_TRANSFORMER;

	@Override
	public String getName() 
        {
		return name().toLowerCase();
	}

	@Override
	public int getMeta()
        {
		return ordinal();
	}

	@Override
	public boolean listForCreative() 
        {
		return true;
	}
}
