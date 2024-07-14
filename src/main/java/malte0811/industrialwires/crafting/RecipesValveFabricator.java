package malte0811.industrialwires.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.util.Utils;

import malte0811.industrialwires.IndustrialWires;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import blusunrize.immersiveengineering.common.IEContent;
import static malte0811.industrialwires.IWConfig.replaceIeTubes;

import javax.annotation.Nullable;

public class RecipesValveFabricator {
	
	public static List<RecipeData> allRecipes = new ArrayList<>();
	
	public static void preInit() {
		//Pre-added recipes.
		if(replaceIeTubes) {
			//remove vaccum
			//remove fluorescent
			//add vaccum (ID 1)
			put(new RecipeData(1, () -> new ItemStack(IEContent.itemMaterial, 3, 26), () -> new ItemStack(IndustrialWires.craftingStuff, 1, 3), () -> new ItemStack(IndustrialWires.craftingStuff, 1, 2), 23040, 90, () -> new FluidStack(IEContent.fluidBiodiesel, 10), null));
			//add fluorescent (ID 2)
		}
		//add mercury (ID 3)
	}

	public static void put(RecipesValveFabricator.RecipeData recipe) { //used by CT too
		allRecipes.add(recipe);
	}
	
	public static RecipeData getRecipeById(int recipeId) {
        for (RecipeData recipe : allRecipes) {
            if (recipe.recipeId == recipeId) {
                return recipe;
            }
        }
        return null;
    }
	
	public static int getMaxId() {
		int[] allIds = new int[allRecipes.size()];
		for (int i = 0; i < allRecipes.size(); i++) {
			allIds[i] = allRecipes.get(i).recipeId;
		}
		int maxId = allIds[0];
		for (int i = 1; i < allIds.length; i++) {
			if (allIds[i] > maxId) {
				maxId = allIds[i];
			}
		}
		return maxId;
	}
	
	public static class RecipeData {
		public final int recipeId;
		public final Supplier<ItemStack> output;
		public final Supplier<ItemStack> inputComponent;
		public final Supplier<ItemStack> inputGlass;
		public final int energy;
		public final int time;
		public final Supplier<FluidStack> fuel;
		@Nullable
		public final Supplier<FluidStack> internal;
		
		public RecipeData(int recipeId, Supplier<ItemStack> output, Supplier<ItemStack> inputComponent, Supplier<ItemStack> inputGlass, int energy, int time, Supplier<FluidStack> fuel, @Nullable Supplier<FluidStack> internal) {
			this.recipeId = recipeId;
			this.output = output;
			this.inputComponent = inputComponent;
			this.inputGlass = inputGlass;
			this.energy = energy;
			this.time = time;
			this.fuel = fuel;
			this.internal = internal;
		}
	}
}

/*---------------------
 * Recipe values:
 * - int recipeId 
 *   - If 0, null; 1, 2 and 3 is for mod recipes.
 * - ItemStack output
 * - ItemStack inputComponent
 * - ItemStack inputGlass
 * - int energy
 *   - In IF.
 * - int time
 *   - In ticks.
 * - FluidStack fuel
 * - FluidStack internal
 * Saving examples:
 * - new FluidStack(fluidBiodiesel, 10)
 ---------------------*/