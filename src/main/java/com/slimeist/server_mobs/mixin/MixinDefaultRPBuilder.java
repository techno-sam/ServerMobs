package com.slimeist.server_mobs.mixin;

import eu.pb4.polymer.impl.resourcepack.DefaultRPBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.file.Path;

@Mixin(DefaultRPBuilder.class)
public class MixinDefaultRPBuilder {
    @Redirect(remap=false,method="lambda$buildResourcePack$6",at=@At(value="INVOKE",ordinal=0,target="Leu/pb4/polymer/impl/resourcepack/DefaultRPBuilder;getPolymerPath(Ljava/lang/String;)Ljava/nio/file/Path;"))
    private Path redirectGetPolymerPath(String path) {
        if (path.equals("base-armor/rendertype_armor_cutout_no_cull.fsh")) {
            return FabricLoader.getInstance().getModContainer("server_mobs").get().getPath(path);
        }
        return FabricLoader.getInstance().getModContainer("polymer").get().getPath(path);
    }
}
