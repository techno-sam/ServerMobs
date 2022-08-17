package com.slimeist.server_mobs.api;

import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ServerMobsApiMod implements DedicatedServerModInitializer {

    public static final String MOD_ID = "server_mobs_api";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Path POLYMER_PACK_FILE = Path.of(FabricLoader.getInstance().getGameDir().toFile() + "/polymer-resourcepack.zip");

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (!FabricLoader.getInstance().isModLoaded("polypack_host")) {
                CompletableFuture.runAsync(() -> {
                    LOGGER.info("Building pack file because PolyPack Host is not loaded");
                    PolymerRPUtils.build(POLYMER_PACK_FILE);
                });
            }
        });
    }
}