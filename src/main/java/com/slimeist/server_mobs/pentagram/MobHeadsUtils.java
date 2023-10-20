package com.slimeist.server_mobs.pentagram;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.slimeist.server_mobs.ServerMobsMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Locale;

public class MobHeadsUtils {
    private static final String[] llamaNames = new String[]{"creamy", "white", "brown", "gray"};
    private static final String[] rabbitNames = new String[]{"brown", "white", "black", "black_and_white", "gold", "salt_and_pepper"};
    public static ItemStack getHead(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        if (type == EntityType.ZOMBIE) return Items.ZOMBIE_HEAD.getDefaultStack();
        else if (entity instanceof CreeperEntity creeper) {
            if (creeper.shouldRenderOverlay()) return makeFromId("creeper");
            else return Items.CREEPER_HEAD.getDefaultStack();
        }
        else if (type == EntityType.SKELETON) return Items.SKELETON_SKULL.getDefaultStack();
        else if (type == EntityType.WITHER_SKELETON) return Items.WITHER_SKELETON_SKULL.getDefaultStack();
        else if (entity instanceof AxolotlEntity axolotl) return makeFromId("axolotl_"+axolotl.getVariant().getName());
        else if (entity instanceof CatEntity cat) {
            Identifier id = Registry.CAT_VARIANT.getId(cat.getVariant());
            String variantName = switch (id.getPath()) {
                case "black" -> "tuxedo";
                case "all_black" -> "black";
                case "red" -> "ginger";
                default -> id.getPath();
            };
            return makeFromId("cat_"+variantName);
        } else if (entity instanceof VillagerEntity villager) {
            String id = villager.getVillagerData().getProfession().id();
            if (id.equals("none")) id = "unemployed";
            return makeFromId("villager_"+id);
        } else if (entity instanceof MooshroomEntity mooshroom) {
            return makeFromId("mooshroom_"+mooshroom.getMooshroomType().name().toLowerCase(Locale.ROOT));
        } else if (entity instanceof TraderLlamaEntity traderLlama) {
            return makeFromId("trader_llama_"+llamaNames[traderLlama.getVariant()]);
        } else if (entity instanceof PandaEntity panda) {
            String personality = panda.getProductGene().getName();
            if (personality.equals("normal")) personality = "";
            return makeFromId("panda_"+personality);
        } else if (entity instanceof HorseEntity horse) {
            String color = horse.getColor().name().toLowerCase(Locale.ROOT);
            if (color.equals("darkbrown")) color = "dark_brown";
            return makeFromId("horse_"+color);
        } else if (entity instanceof FrogEntity frog) {
            return makeFromId("frog_"+Registry.FROG_VARIANT.getId(frog.getVariant()).getPath());
        } else if (entity instanceof ZombieVillagerEntity zombieVillager) {
            String id = zombieVillager.getVillagerData().getProfession().id();
            if (id.equals("none")) return makeFromId("zombie_villager_");
            return makeFromId("zombie_villager_zombie_"+id);
        } else if (entity instanceof RabbitEntity rabbit) {
            if (rabbit.getRabbitType() == RabbitEntity.KILLER_BUNNY_TYPE) return makeFromId("rabbit_the_killer_bunny");
            String string = Formatting.strip(rabbit.getName().getString());
            if ("Toast".equals(string)) return makeFromId("rabbit_toast");
            return makeFromId("rabbit_"+rabbitNames[rabbit.getRabbitType()]);
        } else if (entity instanceof LlamaEntity llama) {
            return makeFromId("llama_"+llamaNames[llama.getVariant()]);
        } else if (type == ServerMobsMod.CROCODILE) return ServerMobsMod.CROCODILE_HEAD.getDefaultStack();
        else if (type == EntityType.WOLF) return ServerMobsMod.WOLF_HEAD.getDefaultStack();
        else if (entity instanceof PlayerEntity player) {
            ItemStack skullStack = new ItemStack(Items.PLAYER_HEAD);
            NbtCompound owner = new NbtCompound();
            NbtHelper.writeGameProfile(owner, player.getGameProfile());
            skullStack.getOrCreateNbt().put("SkullOwner", owner);
            return skullStack;
        } else if (entity instanceof SheepEntity sheep) {
            if (sheep.hasCustomName() && "jeb_".equals(sheep.getName().getString())) return makeFromId("sheep_jeb_sheep");
            return makeFromId("sheep_"+sheep.getColor().getName());
        }

        return makeFromId(Registry.ENTITY_TYPE.getId(type).getPath());
    }

    private static ItemStack makeFromId(String id) {
        if (MobHeadsData.MOB_HEADS.containsKey(id)) {
            ItemStack stack = Items.PLAYER_HEAD.getDefaultStack();
            NbtCompound toMerge;
            try {
                toMerge = new StringNbtReader(new StringReader(MobHeadsData.MOB_HEADS.get(id))).parseCompound();
            } catch (CommandSyntaxException e) {
                return ItemStack.EMPTY;
            }
            NbtCompound stackCompound = stack.getOrCreateNbt();
            stackCompound.copyFrom(toMerge);
            return stack;
        }
        return ItemStack.EMPTY;
    }
}
