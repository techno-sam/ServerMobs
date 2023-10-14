package com.slimeist.server_mobs.api.compat.auto_host;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.autohost.impl.WebServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ResendRPCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("resend_resourcepack")
            .requires(cs -> cs.hasPermissionLevel(2))
            .executes(ctx -> {
                boolean required = AutoHost.config.require || PolymerRPUtils.isRequired();;
                for (ServerPlayerEntity player : ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
                    player.sendResourcePackUrl(WebServer.fullAddress, WebServer.hash, required, AutoHost.message);
                }
                ctx.getSource().sendFeedback(Text.literal("Sent resourcepack to all players"), false);
                return 1;
            });
    }
}
