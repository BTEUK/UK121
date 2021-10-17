package net.bteuk.uk121;

import net.bteuk.uk121.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Optional;

public class UK121 implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger("uk121");

	public static final String MOD_ID = "uk121";

	private static final GeneratorType EARTH = new GeneratorType("earth") {
		protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry,
				Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed) {
			FlatChunkGeneratorConfig config = new FlatChunkGeneratorConfig(
					new StructuresConfig(Optional.empty(), Collections.emptyMap()), biomeRegistry);
			config.updateLayerBlocks();
			return new FlatChunkGenerator(config);
		}
	};

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModItems.registerModItems();

		LOGGER.info("Hello Fabric world!");
	}
}
