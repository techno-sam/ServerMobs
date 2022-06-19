package com.slimeist.server_mobs.entities;

public record MissileColor(int[] colors, int[] fadeColors) {
    public static final MissileColor EMPTY = new MissileColor(new int[0], new int[0]);
}
