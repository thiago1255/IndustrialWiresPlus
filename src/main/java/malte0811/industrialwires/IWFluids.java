package malte0811.industrialwires;

import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.BlockIWFluid;
import net.minecraft.block.Block;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

import static net.minecraft.init.MobEffects.WITHER;
import static net.minecraft.block.material.Material.WATER;
import static net.minecraft.block.material.Material.LAVA;

//IEContent.java
@Mod.EventBusSubscriber
public class IWFluids {
	public static ArrayList<Block> fluidsArray = new ArrayList<Block>();
	
	public static BlockIWFluid blockFluidMercury;
	public static Fluid fluidMercury;
	
	public static BlockIWFluid blockFluidGlassInsulator;
	public static Fluid fluidGlassInsulator;
	
	public static BlockIWFluid blockFluidGlassValve;
	public static Fluid fluidGlassValve;
	
	public static BlockIWFluid blockFluidGlassFiber;
	public static Fluid fluidGlassFiber;
	
	static {
		fluidMercury = setupFluid(new Fluid("mercury", new ResourceLocation("industrialwires:blocks/fluid/mercury_still"), new ResourceLocation("industrialwires:blocks/fluid/mercury_flow")).setDensity(13600).setViscosity(17000));
		blockFluidMercury = new BlockIWFluid("fluidMercury", fluidMercury, WATER);
		
		fluidGlassInsulator = setupFluid(new Fluid("glassinsulator", new ResourceLocation("industrialwires:blocks/fluid/glassinsulator_still"), new ResourceLocation("industrialwires:blocks/fluid/glassinsulator_flow")).setDensity(4000).setViscosity(4000));
		blockFluidGlassInsulator = new BlockIWFluid("fluidGlassInsulator", fluidGlassInsulator, LAVA);
		
		fluidGlassValve = setupFluid(new Fluid("glassvalve", new ResourceLocation("industrialwires:blocks/fluid/glassvalve_still"), new ResourceLocation("industrialwires:blocks/fluid/glassvalve_flow")).setDensity(4000).setViscosity(4000));
		blockFluidGlassValve = new BlockIWFluid("fluidGlassValve", fluidGlassValve, LAVA);
		
		fluidGlassFiber = setupFluid(new Fluid("glassfiber", new ResourceLocation("industrialwires:blocks/fluid/glassfiber_still"), new ResourceLocation("industrialwires:blocks/fluid/glassfiber_flow")).setDensity(4000).setViscosity(4000));
		blockFluidGlassFiber = new BlockIWFluid("fluidGlassFiber", fluidGlassFiber, LAVA);
	}
	
	public static void fluidsInit() {
		blockFluidMercury.setPotionEffects(new PotionEffect(WITHER, 40, 0));
	}
	
	public static void refreshFluidReferences() {
		fluidMercury = FluidRegistry.getFluid("mercury");
		fluidGlassInsulator = FluidRegistry.getFluid("glassinsulator");
		fluidGlassValve = FluidRegistry.getFluid("glassvalve");
		fluidGlassFiber = FluidRegistry.getFluid("glassfiber");
	}
	
	public static Fluid setupFluid(Fluid fluid) {
		FluidRegistry.addBucketForFluid(fluid);
		if(FluidRegistry.registerFluid(fluid)) { return fluid; }
		return FluidRegistry.getFluid(fluid.getName());
	}
}