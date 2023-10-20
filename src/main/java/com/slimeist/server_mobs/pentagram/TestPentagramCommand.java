package com.slimeist.server_mobs.pentagram;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class TestPentagramCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("test_pentagram")
            .requires(cs -> cs.hasPermissionLevel(2))
            .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes((ctx) -> {
                BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(ctx, "pos");
                boolean success = new StructureTester(ctx.getSource().getWorld(), pos).test();
                ctx.getSource().sendFeedback(Text.literal("Pentagram"+(success ? "" : " not")+" found at " + pos.toShortString()), true);
                return success ? 1 : 0;
            }));
    }
}
