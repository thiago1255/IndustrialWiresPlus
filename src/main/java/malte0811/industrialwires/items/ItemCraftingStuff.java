package malte0811.industrialwires.items;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import malte0811.industrialwires.IndustrialWires;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.*;

import javax.annotation.Nonnull;
import java.util.List;

import static malte0811.industrialwires.IndustrialWires.hasII;

public class ItemCraftingStuff extends Item {
	public final static String[] subNames = {
			"mercury_valve"       //0
			,"valve_circuit"      //1
			,"valve_glass"        //2
			,"valve_component"    //3
			,"neon_component"     //4
			,"advanced_component" //5 
			,"retifier_component" //6
	};
	
	public final static String NAME = "stuff_crafting";

	public ItemCraftingStuff() {
		setTranslationKey(IndustrialWires.MODID + "." + NAME);
		setHasSubtypes(true);
		this.setCreativeTab(IndustrialWires.creativeTab);
		setMaxStackSize(16);
		setRegistryName(new ResourceLocation(IndustrialWires.MODID, NAME));
		IndustrialWires.items.add(this);
	}

	@Override
	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> subItems) {
		if (isInCreativeTab(tab)) {
			for (int i = 0; i < subNames.length; i++) {
				if(!hasII && subNames[i] == "advanced_component") {continue;}
				subItems.add(new ItemStack(this, 1, i));
			}
		}
	}

	@Nonnull
	@Override
	public String getTranslationKey(ItemStack stack) {
		return this.getTranslationKey() + "." + subNames[stack.getMetadata()];
	}
}