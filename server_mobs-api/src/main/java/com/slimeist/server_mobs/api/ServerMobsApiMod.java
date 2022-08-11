package com.slimeist.server_mobs.api;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMobsApiMod implements DedicatedServerModInitializer {

    public static final String MOD_ID = "server_mobs_api";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void sayHello() {
        LOGGER.warn("Hello from server mobs api");
    }

    @Override
    public void onInitializeServer() {

    }
}