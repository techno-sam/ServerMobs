package com.slimeist.server_mobs;

import com.slimeist.server_mobs.blocks.CrocodileFluteBlock;
import com.slimeist.server_mobs.entities.*;
import com.slimeist.server_mobs.items.*;
import com.slimeist.server_mobs.server_rendering.model.ServerEntityModelLoader;
import eu.pb4.polymer.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.api.item.PolymerItem;
import eu.pb4.polymer.api.resourcepack.PolymerArmorModel;
import eu.pb4.polymer.api.resourcepack.PolymerModelData;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.*;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMobsMod implements DedicatedServerModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "server_mobs";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	//BLOCKS
	public static final CrocodileFluteBlock CROCODILE_FLUTE_BLOCK = new CrocodileFluteBlock(FabricBlockSettings.of(Material.BAMBOO, MapColor.DARK_GREEN)
			.breakInstantly()
			.strength(1.0f)
			.sounds(BlockSoundGroup.BAMBOO)
			.nonOpaque()
			.dynamicBounds()
	);
	public static final CrocodileFluteItem CROCODILE_FLUTE_ITEM = new CrocodileFluteItem(
			CROCODILE_FLUTE_BLOCK,
			new FabricItemSettings()
					.group(ItemGroup.COMBAT),
			Items.BAMBOO);

	//ITEMS
	public static final MissileItem MISSILE_ITEM = new MissileItem(
			new FabricItemSettings()
					.group(ItemGroup.COMBAT),
			Items.BLAZE_ROD);

	public static final SimpleCustomModelItem CROCODILE_HIDE_ITEM = new SimpleCustomModelItem(
			new FabricItemSettings()
					.group(ItemGroup.MATERIALS),
			Items.LEATHER);

	public static final SimpleCustomModelItem CROCODILE_TOOTH_ITEM = new SimpleCustomModelItem(
			new FabricItemSettings()
					.group(ItemGroup.MATERIALS),
			Items.BONE);

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
	public static ServerEntityModelLoader MISSILE_LOADER = new ServerEntityModelLoader(MISSILE, "missile_entity.bbmodel", false);

	static {
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

	public static EntityType<CrocodileEntity> CROCODILE = Registry.register(
			Registry.ENTITY_TYPE,
			id("crocodile"),
			FabricEntityTypeBuilder.<CrocodileEntity>create(SpawnGroup.MONSTER, CrocodileEntity::new).dimensions(EntityDimensions.fixed(1.1875f, 0.75f)).trackRangeChunks(8).build()
	);
	public static ServerEntityModelLoader CROCODILE_LOADER = new ServerEntityModelLoader(CROCODILE);

	static {
		CrocodileEntity.setBakedModelSupplier(() -> CROCODILE_LOADER.getBakedModel());
	}

	public static final CustomPolymerSpawnEggItem CROCODILE_SPAWN_EGG = new CustomPolymerSpawnEggItem(CROCODILE, Items.GHAST_SPAWN_EGG, new FabricItemSettings().group(ItemGroup.MISC));

	private static <I extends Item & PolymerItem & CustomModelItem> void registerCustomModelItem(I item, String name) {
		PolymerModelData data = PolymerRPUtils.requestModel(item.getPolymerItem(new ItemStack(item, 1), null), id("item/"+name));
		item.setCustomModelData(data.value());
		Registry.register(Registry.ITEM, id(name), item);
	}

	//ARMOR
	private static CustomArmorItem crocodileArmor(Item armorBase) {
		return new CustomArmorItem((ArmorItem) armorBase, ArmorMaterials.CHAIN, new FabricItemSettings().group(ItemGroup.COMBAT));
	}

	public static final CustomArmorItem CROCODILE_HEAD = new CustomArmorItem((ArmorItem) Items.LEATHER_HELMET, Items.SLIME_BALL, ArmorMaterials.CHAIN, new FabricItemSettings().group(ItemGroup.COMBAT));
	public static final CustomArmorItem CROCODILE_HIDE_HELMET = crocodileArmor(Items.LEATHER_HELMET);
	public static final CustomArmorItem CROCODILE_HIDE_CHESTPLATE = crocodileArmor(Items.LEATHER_CHESTPLATE);
	public static final CustomArmorItem CROCODILE_HIDE_LEGGINGS = crocodileArmor(Items.LEATHER_LEGGINGS);
	public static final CustomArmorItem CROCODILE_HIDE_BOOTS = crocodileArmor(Items.LEATHER_BOOTS);

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
		registerCustomModelItem(MISSILE_ITEM, "missile");
		registerCustomModelItem(CROCODILE_HIDE_ITEM, "crocodile_hide");
		registerCustomModelItem(CROCODILE_TOOTH_ITEM, "crocodile_tooth");
		registerCustomModelItem(CROCODILE_SPAWN_EGG, "crocodile_spawn_egg");
		registerCustomModelItem(CROCODILE_FLUTE_ITEM, "crocodile_flute");

		registerCustomModelItem(CROCODILE_HEAD, "crocodile_head");
		registerCustomModelItem(CROCODILE_HIDE_HELMET, "crocodile_hide_helmet");
		registerCustomModelItem(CROCODILE_HIDE_CHESTPLATE, "crocodile_hide_chestplate");
		registerCustomModelItem(CROCODILE_HIDE_LEGGINGS, "crocodile_hide_leggings");
		registerCustomModelItem(CROCODILE_HIDE_BOOTS, "crocodile_hide_boots");

		Registry.register(Registry.BLOCK, id("crocodile_flute"), CROCODILE_FLUTE_BLOCK);
		CROCODILE_FLUTE_BLOCK.registerModel();

		PolymerArmorModel crocodileArmorModel = PolymerRPUtils.requestArmor(id("crocodile_hide"));
		CROCODILE_HIDE_HELMET.setCustomArmorColor(crocodileArmorModel.value());
		CROCODILE_HIDE_CHESTPLATE.setCustomArmorColor(crocodileArmorModel.value());
		CROCODILE_HIDE_LEGGINGS.setCustomArmorColor(crocodileArmorModel.value());
		CROCODILE_HIDE_BOOTS.setCustomArmorColor(crocodileArmorModel.value());

		//Entities
		FabricDefaultAttributeRegistry.register(GOLD_GOLEM, GoldGolemEntity.createGoldGolemAttributes());
		FabricDefaultAttributeRegistry.register(GUST, GustEntity.createGustAttributes());
		FabricDefaultAttributeRegistry.register(TEST, TestEntity.createTestAttributes());
		FabricDefaultAttributeRegistry.register(CROCODILE, CrocodileEntity.createCrocodileAttributes());
		//No attributes for missile, it is not a LivingEntity
		PolymerEntityUtils.registerType(GOLD_GOLEM);
		PolymerEntityUtils.registerType(GUST);
		PolymerEntityUtils.registerType(MISSILE);
		PolymerEntityUtils.registerType(TEST);
		PolymerEntityUtils.registerType(CROCODILE);

		CrocodileEntity.registerSpawnRestrictions(CROCODILE);
		BiomeModifications.addSpawn(
				biomeSelectionContext -> Biome.getCategory(biomeSelectionContext.getBiomeRegistryEntry()).equals(Biome.Category.SWAMP),
				CROCODILE.getSpawnGroup(),
				CROCODILE,
				12,
				2,
				4
		);
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
