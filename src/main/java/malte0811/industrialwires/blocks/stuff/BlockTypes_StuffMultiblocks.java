/*
|| UNDER 'GNU General Public License v3.0'
|| File made by thiago based (copied a lot) of files of mods 'Industrial Wires', and 'Immersive Engineering'.
||
|| (check github for credits of this mods:)
|| IW: https://github.com/malte0811/IndustrialWires
|| IE: https://github.com/BluSunrize/ImmersiveEngineering
*/

package malte0811.industrialwires.blocks.stuff;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;

public enum BlockTypes_StuffMultiblocks implements BlockIEBase.IBlockEnum {
	VALVE_FABRICATOR,
	GLASS_MELTER,
	MELTER_HEATER;

	@Override
	public int getMeta() {
		return ordinal();
	}

	@Override
	public boolean listForCreative() {
		return false;
	}


	@Override
	public String getName() {
		return name().toLowerCase();
	}
}