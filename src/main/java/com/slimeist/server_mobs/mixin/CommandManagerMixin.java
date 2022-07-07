/*package com.slimeist.server_mobs.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Inject(method = "argument", at = @At("TAIL"), cancellable = true)
    private static void polymer_makeSuggestionsServerSide(String name, ArgumentType<?> type, CallbackInfoReturnable<RequiredArgumentBuilder<ServerCommandSource, ?>> cir) {
        if (type instanceof EntityArgumentType) {
            cir.setReturnValue(cir.getReturnValue().suggests(type::listSuggestions));
        }
    }
}
*/