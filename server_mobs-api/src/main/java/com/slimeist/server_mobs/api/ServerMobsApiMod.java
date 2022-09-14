package com.slimeist.server_mobs.api;

import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ServerMobsApiMod implements DedicatedServerModInitializer {

    public static final String MOD_ID = "server_mobs_api";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Path POLYMER_PACK_FILE = Path.of(FabricLoader.getInstance().getGameDir().toFile() + "/polymer-resourcepack.zip");
    private static ServerMobsApiConfig config;

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("server_mobs_api.json");
    }

    public static ServerMobsApiConfig getConfig() {
        return config;
    }

    @Override
    public void onInitializeServer() {
        config = ServerMobsApiConfig.loadConfig(new File(getConfigPath().toString()));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (config.force_rp_build || (!FabricLoader.getInstance().isModLoaded("polypack_host") && !FabricLoader.getInstance().isModLoaded("polymer-autohost"))) {
                CompletableFuture.runAsync(() -> {
                    LOGGER.info("Building pack file because neither polypack_host nor polymer-autohost are loaded, or config set to force RP build");
                    PolymerRPUtils.build(POLYMER_PACK_FILE);
                });
            }
        });
    }
}