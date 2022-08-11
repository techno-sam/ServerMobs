package com.slimeist.server_mobs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServerMobsConfig {
    private static final ServerMobsConfig DEFAULT = new ServerMobsConfig();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    protected float missileExplosionPower = 2f;

    protected int fluteCrocodileSurvivalTicks = 45 * 20;
    protected float fluteCrocodileSpeedMultiplier = 1.45f;
    protected int fluteTargetExpirationTicks = 90 * 20;

    public float getMissileExplosionPower() {
        return missileExplosionPower;
    }

    public void setMissileExplosionPower(float missileExplosionPower) {
        this.missileExplosionPower = missileExplosionPower;
    }

    public int getFluteCrocodileSurvivalTicks() {
        return fluteCrocodileSurvivalTicks;
    }

    public void setFluteCrocodileSurvivalTicks(int fluteCrocodileSurvivalTicks) {
        this.fluteCrocodileSurvivalTicks = fluteCrocodileSurvivalTicks;
    }

    public float getFluteCrocodileSpeedMultiplier() {
        return fluteCrocodileSpeedMultiplier;
    }

    public void setFluteCrocodileSpeedMultiplier(float fluteCrocodileSpeedMultiplier) {
        this.fluteCrocodileSpeedMultiplier = fluteCrocodileSpeedMultiplier;
    }

    public int getFluteTargetExpirationTicks() {
        return fluteTargetExpirationTicks;
    }

    public void setFluteTargetExpirationTicks(int fluteTargetExpirationTicks) {
        this.fluteTargetExpirationTicks = fluteTargetExpirationTicks;
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("server_mobs.json");
    }

    public void resetConfig() {
        setMissileExplosionPower(DEFAULT.getMissileExplosionPower());

        setFluteCrocodileSurvivalTicks(DEFAULT.getFluteCrocodileSurvivalTicks());
        setFluteCrocodileSpeedMultiplier(DEFAULT.getFluteCrocodileSpeedMultiplier());
        setFluteTargetExpirationTicks(DEFAULT.getFluteTargetExpirationTicks());
    }

    public void loadConfig() {
        resetConfig();

        try {
            JsonObject conf = GSON.fromJson(new String(Files.readAllBytes(getConfigPath())), JsonObject.class);
            if (conf.has("missileExplosionPower"))
                setMissileExplosionPower(conf.get("missileExplosionPower").getAsFloat());

            if (conf.has("fluteCrocodileSurvivalTicks"))
                setFluteCrocodileSurvivalTicks(conf.get("fluteCrocodileSurvivalTicks").getAsInt());

            if (conf.has("fluteCrocodileSpeedMultiplier"))
                setFluteCrocodileSpeedMultiplier(conf.get("fluteCrocodileSpeedMultiplier").getAsFloat());

            if (conf.has("fluteTargetExpirationTicks"))
                setFluteTargetExpirationTicks(conf.get("fluteTargetExpirationTicks").getAsInt());
        } catch (IOException | ClassCastException | IllegalStateException | JsonSyntaxException e) {
            ServerMobsMod.LOGGER.error("Failed to load config. If this is the first time running this mod, disregard this message.");
        }

        saveConfig();
    }

    public void saveConfig() {
        JsonObject conf = new JsonObject();
        conf.addProperty("__comment1", "To reset a value, simply delete the entry for it.");
        conf.addProperty("__comment2", "It will be recreated on next server startup.");

        conf.addProperty("missileExplosionPower", getMissileExplosionPower());
        conf.addProperty("fluteCrocodileSurvivalTicks", getFluteCrocodileSurvivalTicks());
        conf.addProperty("fluteCrocodileSpeedMultiplier", getFluteCrocodileSpeedMultiplier());
        conf.addProperty("fluteTargetExpirationTicks", getFluteTargetExpirationTicks());

        try {
            Files.write(getConfigPath(), GSON.toJson(conf).getBytes());
        } catch (IOException ex) {
            ServerMobsMod.LOGGER.error("Failed to save config.");
        }
    }
}
