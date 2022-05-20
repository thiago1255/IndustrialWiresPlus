/*
Made using other files of this mod as example/model.
*/

package malte0811.industrialwires.blocks.stuff;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase.IBlockEnum;

public enum BlockTypes_GeneralStuff implements IBlockEnum{
	CONTROL_TRANSFORMER,
        VARISTOR;

	@Override
	public String getName() {
		return name().toLowerCase();
	}
        
        @Override
	public int getMeta() {
		return ordinal();
	}

	@Override
	public boolean listForCreative() {
		return true;
	}
}
