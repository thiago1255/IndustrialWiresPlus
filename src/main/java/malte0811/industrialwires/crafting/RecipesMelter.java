package malte0811.industrialwires.crafting;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.util.Utils;

import malte0811.industrialwires.IndustrialWrites;
import malte0811.industrialwires.blocks.stuff.BlockTypes_GeneralStuff;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static malte0811.industrialwires.IndustrialWires.hasIC2;
import static malte0811.industrialwires.IWFluids.fluidMercury;
import static malte0811.industrialwires.IWFluids.fluidGlassInsulator;
import static malte0811.industrialwires.IWFluids.fluidGlassValve;
import static malte0811.industrialwires.IWFluids.fluidGlassFiber;
import static ic2.api.item.IC2Items.getItem;

public class RecipesMelter {
	public static List<RecipeData> allRecipes = new ArrayList<>();

	public static void init() {
		put(new RecipeData(200,
			() -> new FluidStack(fluidMercury, 200),
			() -> new IngredientStack[] {
				new IngredientStack(new ItemStack(IndustrialWires.generalStuff, 1, BlockTypes_GeneralStuff.MERCURY_ORE.ordinal()))
			}
		)); // Mercury
		put(new RecipeData(4000,
			() -> new FluidStack(fluidGlassInsulator, 4000),
			() -> new IngredientStack[] {
				new IngredientStack(new ItemStack(Blocks.GLASS, 4)),
				new IngredientStack(new ItemStack(Items.DYE, 1, 2)),
				new IngredientStack("dustIron")
			}
		)); // Glass Insulator
		put(new RecipeData(2000,
			() -> new FluidStack(fluidGlassValve, 2000),
			() -> new IngredientStack[] {
				new IngredientStack(new ItemStack(Blocks.GLASS, 2)),
				new IngredientStack(new ItemStack(Items.QUARTZ))
			}
		)); // Valve Glass
		put(new RecipeData(20000,
			() -> new FluidStack(fluidGlassFiber, 20000),
			() -> new IngredientStack[] {
				new IngredientStack(new ItemStack(Blocks.GLASS, 20)),
				new IngredientStack(new ItemStack(Items.REDSTONE, 2)),
				new IngredientStack(new ItemStack(Items.GLOWSTONE_DUST, 2)),
				new IngredientStack(Utils.copyStackWIthAmount(getItem("dust", "diamond"), 2)),
				new IngredientStack(Utils.copyStackWIthAmount(getItem("dust", "silver"), 2))
			}
		)); // Glass Fiber
	}

	public static void put(RecipesMelter.RecipeData recipe) { // also used by CT
		allRecipes.add(recipe);
	}

	public static RecipeData recipeByInputs(List<ItemStack> items) {
		for(RecipeData re : allRecipes) {
			if(re.matches(items)) {
				return re;
			}
		}
		return null;
	}

	public static class RecipeData {
		public final int time; //ticks
		public final Supplier<FLuidStack> output;
		public final Supplier<IngredientStack[]> inputs;

		public RecipeData(int time, Supplier<FluidStack> output, Supplier<IngredientStack[]> inputs) {
			this.time = time;
			this.output = output;
			this.inputs = inputs;
		}

		public boolean matches(List<ItemStack> items) {
			for(IngredientStack ing : inputs.get()) {
				int found = 0;
				for(ItemStack s : items) {
					if(ing.matchesItemStackIgnoringSize(s)) {
						found += s.getCount();
					}
				}
				if(found < ing.inputSize) {
					return false;
				}
			}
			return true;
		}
	}
}