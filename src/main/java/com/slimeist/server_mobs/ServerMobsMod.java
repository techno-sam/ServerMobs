package com.slimeist.server_mobs;

import com.slimeist.server_mobs.api.ServerMobsApiMod;
import com.slimeist.server_mobs.api.server_rendering.model.ServerEntityModelLoader;
import com.slimeist.server_mobs.blocks.CrocodileFluteBlock;
import com.slimeist.server_mobs.entities.CrocodileEntity;
import com.slimeist.server_mobs.entities.GustEntity;
import com.slimeist.server_mobs.entities.MissileEntity;
import com.slimeist.server_mobs.items.*;
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
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.*;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.tag.BiomeTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

public class ServerMobsMod implements DedicatedServerModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final String MOD_ID = "server_mobs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static ServerMobsConfig config;

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
                    .group(ItemGroup.COMBAT)
                    .maxDamage(25),
            Items.CARROT_ON_A_STICK);

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

    public static final EntityType<GustEntity> GUST = Registry.register(
            Registry.ENTITY_TYPE,
            id("gust"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, GustEntity::new).dimensions(EntityDimensions.fixed(0.875f, 1.5f)).trackRangeChunks(8).build()
    );
    public static final ServerEntityModelLoader GUST_LOADER = new ServerEntityModelLoader(GUST);

    static {
        GustEntity.setBakedModelSupplier(GUST_LOADER::getBakedModel);
    }

    public static final EntityType<MissileEntity> MISSILE = Registry.register(
            Registry.ENTITY_TYPE,
            id("missile"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, MissileEntity::new).dimensions(EntityDimensions.fixed(0.3125f, 0.3125f)).trackRangeChunks(8).build()
    );
    public static final ServerEntityModelLoader MISSILE_LOADER = new ServerEntityModelLoader(MISSILE, "missile_entity.bbmodel", false);

    static {
        MissileEntity.setBakedModelSupplier(MISSILE_LOADER::getBakedModel);
    }

    public static final EntityType<CrocodileEntity> CROCODILE = Registry.register(
            Registry.ENTITY_TYPE,
            id("crocodile"),
            FabricEntityTypeBuilder.<CrocodileEntity>create(SpawnGroup.MONSTER, CrocodileEntity::new).dimensions(EntityDimensions.fixed(1.1875f, 0.75f)).trackRangeChunks(8).build()
    );
    public static final ServerEntityModelLoader CROCODILE_LOADER = new ServerEntityModelLoader(CROCODILE);

    static {
        CrocodileEntity.setBakedModelSupplier(CROCODILE_LOADER::getBakedModel);
    }

    public static final CustomPolymerSpawnEggItem CROCODILE_SPAWN_EGG = new CustomPolymerSpawnEggItem(CROCODILE, Items.GHAST_SPAWN_EGG, new FabricItemSettings().group(ItemGroup.MISC));

    private static <I extends Item & PolymerItem & CustomModelItem> void registerCustomModelItem(I item, String name) {
        PolymerModelData data = PolymerRPUtils.requestModel(item.getPolymerItem(new ItemStack(item, 1), null), id("item/" + name));
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

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("server_mobs.json");
    }

    public static ServerMobsConfig getConfig() {
        return config;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onInitializeServer() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        ServerMobsApiMod.sayHello();

        LOGGER.info("ServerMobs loading");
        config = ServerMobsConfig.loadConfig(new File(getConfigPath().toString()));

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
        FabricDefaultAttributeRegistry.register(GUST, GustEntity.createGustAttributes());
        FabricDefaultAttributeRegistry.register(CROCODILE, CrocodileEntity.createCrocodileAttributes());
        //No attributes for missile, it is not a LivingEntity
        PolymerEntityUtils.registerType(GUST);
        PolymerEntityUtils.registerType(MISSILE);
        PolymerEntityUtils.registerType(CROCODILE);

        CrocodileEntity.registerSpawnRestrictions(CROCODILE);
        BiomeModifications.addSpawn(
                biomeSelectionContext -> biomeSelectionContext.getBiomeRegistryEntry().isIn(BiomeTags.ALLOWS_SURFACE_SLIME_SPAWNS),
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
