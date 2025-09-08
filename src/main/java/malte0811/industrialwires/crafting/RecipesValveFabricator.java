package malte0811.industrialwires.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.IEContent;

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
import java.util.Iterator;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import static malte0811.industrialwires.IWConfig.replaceIeTubes;
import static malte0811.industrialwires.IndustrialWires.hasII;
import static malte0811.industrialwires.IndustrialWires.hasCT;

import static malte0811.industrialwires.IWFluids.fluidMercury;
import static pl.pabilo8.immersiveintelligence.common.IIContent.gasHydrogen;
import static pl.pabilo8.immersiveintelligence.common.IIContent.itemMaterial;

public class RecipesValveFabricator {
	
	public static List<RecipeData> allRecipes = new ArrayList<>();
	
	public static void init() { //this is for pre-added recipes.
		if(replaceIeTubes && hasCT) {
			//add vaccum (ID 1)
			put(new RecipeData(1, 
				() -> new ItemStack(IEContent.itemMaterial, 3, 26),
				() -> new ItemStack(IndustrialWires.craftingStuff, 1, 2),
				() -> new ItemStack(IndustrialWires.craftingStuff, 1, 3), 
				15000, 410, 17,
				null
			));
			if(hasII) {
				//add fluorescent (ID 2)
				put(new RecipeData(2,
					() -> new ItemStack(IEContent.itemFluorescentTube, 1, 1),
					() -> new ItemStack(IndustrialWires.craftingStuff, 1, 2),
					() -> new ItemStack(IndustrialWires.craftingStuff, 1, 4), 
					10000, 390, 10, 
					() -> new FluidStack(gasHydrogen, 25)
				));
				//add advanced (ID 3)
				put(new RecipeData(3,
					() -> new ItemStack(itemMaterial, 3, 0),
					() -> new ItemStack(IndustrialWires.craftingStuff, 2, 2),
					() -> new ItemStack(IndustrialWires.craftingStuff, 1, 5),
					30000, 490, 29,
					null
				));
			}
		}
		//add mercury (ID 4)
		put(new RecipeData(2,
			() -> new ItemStack(IndustrialWires.craftingStuff, 1, 0),
			() -> new ItemStack(IndustrialWires.craftingStuff, 1, 2),
			() -> new ItemStack(IndustrialWires.craftingStuff, 1, 6), 
			32000, 528, 32, 
			() -> new FluidStack(fluidMercury, 1000)
		));
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
		public final Supplier<ItemStack> inputGlass;
		public final Supplier<ItemStack> inputComponent;
		public final int energy;
		public final int time;
		public final int fuel;
		@Nullable
		public final Supplier<FluidStack> internal;
		
		public RecipeData(int recipeId, Supplier<ItemStack> output, Supplier<ItemStack> inputComponent, Supplier<ItemStack> inputGlass, int energy, int time, int fuel, @Nullable Supplier<FluidStack> internal) {
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
 
 /*
			//add component to wb
			BlueprintCraftingRecipe.addRecipe("components", new ItemStack(IIndustrialWires.craftingStuff, 1, 3), "plateNickel", "wireCopper", "dustRedstone");
			//remove vaccum from wb (from Blueprint.java)
			Iterator<BlueprintCraftingRecipe> it = BlueprintCraftingRecipe.recipeList.get("components").iterator();
			while(it.hasNext()) {
				BlueprintCraftingRecipe ir = it.next();
				final ItemStack vaccum = new ItemStack(IEContent.itemMaterial, 3, 26);
				if(OreDictionary.itemMatches(ir.output, vaccum, true) && ItemStack.areItemStackTagsEqual(ir.output, vaccum)) { it.remove(); break; }
			}
			//remove vaccum from pa
				
				//add component to pa
				PrecissionAssemblerRecipe.addRecipe(new ItemStack(IEContent.itemMaterial, 4, 26), new ItemStack(IEContent.itemMetal, 1, 20), new IngredientStack[]{new IngredientStack("plateIron"), new IngredientStack("wireCopper", 2), new IngredientStack("dustRedstone")}, new String[]{"inserter", "solderer", "drill"}, new String[]{"drill work main", "solderer work first", "inserter pick first", "inserter drop main", "solderer work main", "drill work second", "inserter pick second", "inserter drop main"}, 12000, 1.0f);
				//add adv component to pa
				PrecissionAssemblerRecipe.addRecipe(new ItemStack(IndustrialWires.craftingStuff, 1, 5), ItemStack.EMPTY, new IngredientStack[]{new IngredientStack("plateSteel", 3), new IngredientStack("wireTungsten", 6), new ItemStack(IndustrialWires.craftingStuff, 2, 3)}, new String[]{"inserter", "solderer", "drill"}, new String[]{"drill work main", "inserter pick second", "inserter drop main", "inserter pick first", "inserter drop main", "solderer work main"}, 24000, 1.25f);
				//remove fluorescent
				//remove advanced from pa
*/