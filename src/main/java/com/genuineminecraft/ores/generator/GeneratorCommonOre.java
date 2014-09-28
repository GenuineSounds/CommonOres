package com.genuineminecraft.ores.generator;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;

import com.genuineminecraft.ores.metals.Metal;
import com.genuineminecraft.ores.registry.MetalRegistry;

import cpw.mods.fml.common.IWorldGenerator;

public class GeneratorCommonOre implements IWorldGenerator {

	private void genEnd(World world, Random random, int chunkX, int chunkZ) {}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		long time = System.currentTimeMillis();
		switch (world.provider.dimensionId) {
			case -1:
				this.genNether(world, random, chunkX, chunkZ);
				break;
			case 0:
				this.genOverworld(world, random, chunkX, chunkZ);
				break;
			case 1:
				this.genEnd(world, random, chunkX, chunkZ);
				break;
			default:
				break;
		}
		long newTime = System.currentTimeMillis() - time;
		if (newTime > 1)
			System.out.println("Standard generation: " + newTime + " milliseconds");
	}

	private void genNether(World world, Random random, int chunkX, int chunkZ) {}

	private void genOverworld(World world, Random random, int chunkX, int chunkZ) {
		Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
		int yMax = Utility.findHighestBlock(chunk);
		for (Metal metal : MetalRegistry.getInstance().metals) {
			if (metal.isAlloy())
				continue;
			WorldGenMinable gen = new WorldGenMinable(metal.ore, metal.getNodeSize());
			for (int i = 0; i < (yMax / 64D) * metal.getNodesPerChunk(); i++) {
				if (metal.getChunkRarity() < random.nextDouble())
					continue;
				int x = chunkX * 16 + random.nextInt(16);
				int z = chunkZ * 16 + random.nextInt(16);
				int y = (int) (((random.nextGaussian() - 0.5) * metal.getSpread() * yMax) + metal.getDepth() * yMax);
				if (y < 0)
					continue;
				gen.generate(world, random, x, y, z);
			}
		}
	}
}