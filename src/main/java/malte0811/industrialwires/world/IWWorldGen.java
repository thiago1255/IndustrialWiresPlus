//https://shadowfacts.net/tutorials/forge-modding-112/world-generation-ore/
package malte0811.industrialwires.world;

import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.stuff.BlockGeneralStuff;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class IWWorldGen implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		//IndustrialWires.logger.info("Checking for generation.");
		if (world.provider.getDimension() == -1) { //nether
			if (random.nextInt(100) < 7) {
				IndustrialWires.logger.warn("Generating mercury ore !");
				WorldGenMinable mercuryGenerator = new WorldGenMinable(IndustrialWires.generalStuff.getStateFromMeta(6), 6, BlockMatcher.forBlock(Blocks.NETHERRACK)); //State, max vein size, block to replace
				mercuryGenerator.generate(world, random, new BlockPos(chunkX*16 + random.nextInt(16), 8 + random.nextInt(30), chunkZ*16 + random.nextInt(16))); //world, rand, pos
			}		
		} else {
			//IndustrialWires.logger.warn("Not nether.");
		}
	}

}