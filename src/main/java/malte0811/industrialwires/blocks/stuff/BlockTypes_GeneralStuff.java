/*
|| (do what u want with this, but give credits to:)
|| File made by thiago1255 based (copied a lot) of files of mods 'Industrial Wires', and 'Immersive Engineering'.
||
|| (check github for credits of this mods:)
|| IW: https://github.com/malte0811/IndustrialWires
|| IE: https://github.com/BluSunrize/ImmersiveEngineering
*/

package malte0811.industrialwires.blocks.stuff;

import malte0811.industrialwires.compat.Compat;
import net.minecraft.util.IStringSerializable;

public enum BlockTypes_GeneralStuff implements IStringSerializable{
	CONTROL_TRANSFORMER,
        VARISTOR;

	@Override
	public String getName() {
		return name().toLowerCase();
	}
}
