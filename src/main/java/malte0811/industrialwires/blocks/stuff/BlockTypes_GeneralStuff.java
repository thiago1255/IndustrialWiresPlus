/*
Made using other files of this mod as example/model.
*/

package malte0811.industrialwires.blocks.stuff;

import net.minecraft.util.IStringSerializable;

public enum BlockTypes_GeneralStuff implements IStringSerializable{
	CONTROL_TRANSFORMER,
        VARISTOR;

	@Override
	public String getName() {
		return name().toLowerCase();
	}
}
