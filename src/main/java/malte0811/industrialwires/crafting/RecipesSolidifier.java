package malte0811.industrialwires.crafting;

import malte0811.industrialwires.IndustrialWires;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.Fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static malte0811.industrialwires.IWConfig.replaceIeTubes;
import static malte0811.industrialwires.IndustrialWires.hasCT;
import static net.minecraftforge.fluids.FluidRegistry.LAVA;
import static net.minecraft.init.Blocks.MAGMA;
import static malte0811.industrialwires.IWFluids.fluidGlassInsulator;
import static malte0811.industrialwires.IWFluids.fluidGlassValve;
import static malte0811.industrialwires.IWFluids.fluidGlassFiber;
import static blusunrize.immersiveengineering.common.IEContent.blockStoneDecoration;
import static ic2.api.item.IC2Items.getItem;

public class RecipesSolidifier {
	
	public static List<RecipeData> allRecipes = new ArrayList<>();
	
	public static void init() { //this is for pre-added recipes.
		put(new RecipeData(4800,
			() -> new FluidStack(LAVA, 1000),
			() -> new ItemStack(MAGMA, 1)
		)); //Magma blocks
		put(new RecipeData(500,
			() -> new FluidStack(fluidGlassValve, 250),
			() -> new ItemStack(IndustrialWires.craftingStuff, 1, 2)
		)); //Valve glass
		put(new RecipeData(12000,
			() -> new FluidStack(fluidGlassFiber, 250),
			() -> getItem("cable", "type:glass,insulation:0")
		)); // Fiber
		if(replaceIeTubes && hasCT) {
			put(new RecipeData(800,
				() -> new FluidStack(fluidGlassInsulator, 1000),
				() -> new ItemStack(blockStoneDecoration, 1, 8)
			)); //Green Glass Thingh
		}
	}

	public static void put(RecipesSolidifier.RecipeData recipe) { //used by CT too
		allRecipes.add(recipe);
	}
	
	public static RecipeData recipeByInput(Fluid recipeInput) {
        for (RecipeData re : allRecipes) {
            if (re.input.get().getFluid() == recipeInput) {
                return re;
            }
        }
        return null;
    }
	
	public static class RecipeData {
		public final int time;
		public final Supplier<FluidStack> input;
		public final Supplier<ItemStack> output;
		
		public RecipeData(int time, Supplier<FluidStack> input, Supplier<ItemStack> output) {
			this.time = time;
			this.input = input;
			this.output = output;
		}
	}
}