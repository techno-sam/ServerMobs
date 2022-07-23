package com.slimeist.server_mobs;

import com.slimeist.server_mobs.entities.GoldGolemEntity;
import com.slimeist.server_mobs.entities.GustEntity;
import com.slimeist.server_mobs.entities.MissileEntity;
import com.slimeist.server_mobs.entities.TestEntity;
import com.slimeist.server_mobs.items.MissileItem;
import com.slimeist.server_mobs.server_rendering.model.ServerEntityModelLoader;
import eu.pb4.polymer.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.api.resourcepack.PolymerModelData;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
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

	public static final boolean SMALL_STANDS = false;

	//ITEMS
	public static final Item MISSILE_ITEM = new MissileItem(
			new FabricItemSettings()
					.group(ItemGroup.COMBAT),
			Items.BLAZE_ROD);

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

	public static EntityType<GustEntity> GUST = Registry.register(
			Registry.ENTITY_TYPE,
			id("gust"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, GustEntity::new).dimensions(EntityDimensions.fixed(0.875f, 1.5f)).trackRangeChunks(8).build()
	);
	public static ServerEntityModelLoader GUST_LOADER = new ServerEntityModelLoader(GUST);
	static {
		GustEntity.setBakedModelSupplier(() -> GUST_LOADER.getBakedModel());
	}

	public static EntityType<MissileEntity> MISSILE = Registry.register(
			Registry.ENTITY_TYPE,
			id("missile"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, MissileEntity::new).dimensions(EntityDimensions.fixed(0.3125f, 0.3125f)).trackRangeChunks(8).build()
	);
	public static ServerEntityModelLoader MISSILE_LOADER = new ServerEntityModelLoader(MISSILE, "missile_entity.bbmodel");
	static  {
		MissileEntity.setBakedModelSupplier(() -> MISSILE_LOADER.getBakedModel());
	}

	public static EntityType<TestEntity> TEST = Registry.register(
			Registry.ENTITY_TYPE,
			id("test"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, TestEntity::new).dimensions(EntityDimensions.fixed(0.7f, 1.9f)).trackRangeChunks(8).build()
	);
	public static ServerEntityModelLoader TEST_LOADER = new ServerEntityModelLoader(TEST);
	static {
		TestEntity.setBakedModelSupplier(() -> TEST_LOADER.getBakedModel());
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
		//Items
		PolymerModelData missileData = PolymerRPUtils.requestModel(Items.BLAZE_ROD, id("item/missile_item"));
		((MissileItem) MISSILE_ITEM).setCustomModelData(missileData.value());
		Registry.register(Registry.ITEM, id("missile"), MISSILE_ITEM);

		//Entities
		FabricDefaultAttributeRegistry.register(GOLD_GOLEM, GoldGolemEntity.createGoldGolemAttributes());
		FabricDefaultAttributeRegistry.register(GUST, GustEntity.createGustAttributes());
		FabricDefaultAttributeRegistry.register(TEST, TestEntity.createTestAttributes());
		//No attributes for missile, it is not a LivingEntity
		PolymerEntityUtils.registerType(GOLD_GOLEM);
		PolymerEntityUtils.registerType(GUST);
		PolymerEntityUtils.registerType(MISSILE);
		PolymerEntityUtils.registerType(TEST);
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
