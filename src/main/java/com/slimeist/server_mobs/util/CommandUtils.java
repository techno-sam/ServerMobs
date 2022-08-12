package com.slimeist.server_mobs.util;

import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;

public class CommandUtils {
    public static int playSound(Collection<ServerPlayerEntity> targets, Identifier sound, SoundCategory category, Vec3d pos, float volume, float pitch, float minVolume) {
        double d = Math.pow(volume > 1.0f ? (double) (volume * 16.0f) : 16.0, 2.0);
        int i = 0;
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            double e = pos.x - serverPlayerEntity.getX();
            double f = pos.y - serverPlayerEntity.getY();
            double g = pos.z - serverPlayerEntity.getZ();
            double h = e * e + f * f + g * g;
            Vec3d vec3d = pos;
            float j = volume;
            if (h > d) {
                if (minVolume <= 0.0f) continue;
                double k = Math.sqrt(h);
                vec3d = new Vec3d(serverPlayerEntity.getX() + e / k * 2.0, serverPlayerEntity.getY() + f / k * 2.0, serverPlayerEntity.getZ() + g / k * 2.0);
                j = minVolume;
            }
            serverPlayerEntity.networkHandler.sendPacket(new PlaySoundIdS2CPacket(sound, category, vec3d, j, pitch));
            ++i;
        }
        return i;
    }
}
