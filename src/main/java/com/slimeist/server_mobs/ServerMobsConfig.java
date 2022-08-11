package com.slimeist.server_mobs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ServerMobsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public float missileExplosionPower = 2f;

    public int fluteCrocodileSurvivalTicks = 45 * 20;
    public float fluteCrocodileSpeedMultiplier = 1.45f;
    public int fluteTargetExpirationTicks = 90 * 20;

    public static ServerMobsConfig loadConfig(File file) {
        ServerMobsConfig config;

        if (file.exists() && file.isFile()) {
            try (
                    FileInputStream fileInputStream = new FileInputStream(file);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    ) {
                config = GSON.fromJson(bufferedReader, ServerMobsConfig.class);
            } catch (IOException e) {
                ServerMobsMod.LOGGER.error("Failed to load config");
                config = new ServerMobsConfig();
            }
        } else {
            config = new ServerMobsConfig();
        }

        config.saveConfig(file);
        return config;
    }

    public void saveConfig(File config) {
        try (
                FileOutputStream stream = new FileOutputStream(config);
                Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
                ) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            ServerMobsMod.LOGGER.error("Failed to save config");
        }
    }
}
