/*
|| (do what u want with this, but give credits to:)
|| File made by thiago1255 based (copied a lot) of files of mods 'Industrial Wires', and 'Immersive Engineering'.
||
|| (check github for credits of this mods:)
|| IW: https://github.com/malte0811/IndustrialWires
|| IE: https://github.com/BluSunrize/ImmersiveEngineering
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
