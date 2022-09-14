package com.slimeist.server_mobs.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("CanBeFinal")
public class ServerMobsApiConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean force_rp_build = false;

    public static ServerMobsApiConfig loadConfig(File file) {
        ServerMobsApiConfig config;

        if (file.exists() && file.isFile()) {
            try (
                    FileInputStream fileInputStream = new FileInputStream(file);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
            ) {
                config = GSON.fromJson(bufferedReader, ServerMobsApiConfig.class);
            } catch (IOException e) {
                ServerMobsApiMod.LOGGER.error("Failed to load config");
                config = new ServerMobsApiConfig();
            }
        } else {
            config = new ServerMobsApiConfig();
        }

        config.saveConfig(file);
        return config;
    }

    public void saveConfig(File config) {
        try (
                FileOutputStream stream = new FileOutputStream(config);
                Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)
        ) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            ServerMobsApiMod.LOGGER.error("Failed to save config");
        }
    }
}
