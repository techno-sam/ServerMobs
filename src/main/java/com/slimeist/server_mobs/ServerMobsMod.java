package com.slimeist.server_mobs;

import com.slimeist.server_mobs.entities.GoldGolemEntity;
import com.slimeist.server_mobs.server_rendering.model.ServerEntityModelLoader;
import eu.pb4.polymer.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMobsMod implements DedicatedServerModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "server_mobs";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	//ITEMS
	/*public static final Item JETPACK_BUTTON = new JetpackButtonItem(
			new FabricItemSettings()
					.maxDamage(25)
					.group(ItemGroup.TRANSPORTATION),
			Items.CARROT_ON_A_STICK);*/

	//ENTITIES
	public static EntityType<GoldGolemEntity> GOLD_GOLEM = Registry.register(
			Registry.ENTITY_TYPE,
			id("gold_golem"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, GoldGolemEntity::new).dimensions(EntityDimensions.fixed(0.7f, 1.9f)).trackRangeChunks(8).build()
	);
	public static ServerEntityModelLoader GOLD_GOLEM_LOADER = new ServerEntityModelLoader(GOLD_GOLEM);
	static {
		GoldGolemEntity.setBakedModelSupplier(() -> GOLD_GOLEM_LOADER.getBakedModel());
	}

	@Override
	public void onInitializeServer() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("ServerMobs loading");

		if (PolymerRPUtils.addAssetSource(MOD_ID)) {
			LOGGER.info("Successfully marked as asset source");
		} else {
			LOGGER.error("Failed to mark ServerMobs as asset source");
		}
		PolymerRPUtils.markAsRequired();
		FabricDefaultAttributeRegistry.register(GOLD_GOLEM, GoldGolemEntity.createGoldGolemAttributes());
		PolymerEntityUtils.registerType(GOLD_GOLEM);
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
