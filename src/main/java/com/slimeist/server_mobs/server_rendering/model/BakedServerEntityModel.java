package com.slimeist.server_mobs.server_rendering.model;

import com.slimeist.server_mobs.ServerMobsMod;
import com.slimeist.server_mobs.mixin.ArmorStandEntityAccessor;
import com.slimeist.server_mobs.server_rendering.hologram.ArmorStandHologramElement;
import com.slimeist.server_mobs.server_rendering.model.elements.ModelGroup;
import eu.pb4.holograms.api.holograms.EntityHologram;
import eu.pb4.polymer.api.resourcepack.PolymerModelData;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public record BakedServerEntityModel(int texWidth, int texHeight,
                                     ModelGroup base) {

    public Instance createInstance(LivingEntity entity) {
        return new Instance(this, entity);
    }

    public static class Instance {
        private BakedServerEntityModel parent;
        private LivingEntity entity;
        private EntityHologram hologram;
        private HashMap<ModelGroup, ModelDisplayPiece> displayPieces;

        protected Instance(BakedServerEntityModel parent, LivingEntity entity) {
            this.parent = parent;
            this.entity = entity;
            this.hologram = new EntityHologram(this.entity, Vec3d.ZERO);
            this.initializeHologram();
        }

        @Nullable
        private ModelGroup getModelGroup(String modelPath) {
            String[] pathParts = modelPath.split("\\.");
            ModelGroup modelGroup = parent.base().getChild("base");
            for (int i = pathParts[0].equals("base") ? 1 : 0; i< pathParts.length; i++) {
                if (modelGroup == null) {
                    return null;
                }
                modelGroup = modelGroup.getChild(pathParts[i]);
            }
            return modelGroup;
        }

        private void setupArmorStand(String modelPath) {
            ModelGroup group = getModelGroup(modelPath);
            if (group == null) {
                ServerMobsMod.LOGGER.warn("Failed to create armor stand for path "+modelPath+"because that path does not exist.");
                return;
            }
            PolymerModelData data = group.getDisplayData();
            ItemStack displayStack = new ItemStack(data.item(), 1);
            NbtCompound nbt = displayStack.getOrCreateNbt();
            nbt.putInt("CustomModelData", data.value());
            displayStack.setNbt(nbt);

            ArmorStandEntity armorStand = new ArmorStandEntity(this.entity.getWorld(), this.entity.getX(), this.entity.getY(), this.entity.getZ());
            armorStand.equipStack(EquipmentSlot.HEAD, displayStack);
            armorStand.setYaw(0.0f);
            armorStand.setBodyYaw(0.0f);
            armorStand.setHeadYaw(0.0f);
            armorStand.setBodyRotation(ArmorStandEntityAccessor.getDEFAULT_BODY_ROTATION());
            armorStand.setHeadRotation(ArmorStandEntityAccessor.getDEFAULT_HEAD_ROTATION());
            ((ArmorStandEntityAccessor) armorStand).invokeSetMarker(true);
            int id = this.hologram.addElement(new ArmorStandHologramElement(armorStand, true));
            ModelDisplayPiece displayPiece = new ModelDisplayPiece(armorStand, id);
            this.displayPieces.put(group, displayPiece);
        }

        protected void initializeHologram() {
            this.hologram.hide();
            this.hologram.clearElements();
            PolymerModelData data = this.parent.base().getChild("base").getChild("body_bottom").getDisplayData();
            this.itemStack = new ItemStack(data.item(), 1);
            NbtCompound stackNbt = this.itemStack.getOrCreateNbt();
            stackNbt.putInt("CustomModelData", data.value());
            this.itemStack.setNbt(stackNbt);

            this.armorStand = new ArmorStandEntity(this.entity.getWorld(), this.entity.getX(), this.entity.getY(), this.entity.getZ());
            this.armorStand.equipStack(EquipmentSlot.HEAD, this.itemStack);
            this.armorStand.setYaw(0.0f);
            this.armorStand.setBodyYaw(0.0f);
            this.armorStand.setHeadYaw(0.0f);
            this.armorStand.setBodyRotation(ArmorStandEntityAccessor.getDEFAULT_BODY_ROTATION());
            this.armorStand.setHeadRotation(ArmorStandEntityAccessor.getDEFAULT_HEAD_ROTATION());
            ((ArmorStandEntityAccessor) this.armorStand).invokeSetMarker(true);

            this.hologram.addElement(new ArmorStandHologramElement(this.armorStand, true));
            this.hologram.setOffset(new Vec3d(0, -0.4475*2, 0));
            this.hologram.show();
        }

        public void updateHologram() {
            //this.hologram.addItemStack(this.itemStack, false);
            //this.hologram.setOffset(new Vec3d(0, -1.1865, 0));
            this.armorStand.setHeadRotation(new EulerAngle(0.0f, (float) (this.entity.bodyYaw), 0.0f));
            this.armorStand.setInvisible(false);
            ((ArmorStandEntityAccessor) this.armorStand).invokeSetSmall(true);
            Vec3d offset = new Vec3d(0, -0.4475*2, 0);
            Vec3f pivot = this.parent.base().getChild("base").getChild("body_bottom").transform.pivotVec();
            offset.add(pivot.getX(), pivot.getY(), pivot.getZ());
            this.hologram.setOffset(offset);
            this.hologram.syncPositionWithEntity();
        }

        public static class ModelDisplayPiece {
            public final ArmorStandEntity armorStand;
            public final int elementID;
            public EulerAngle rotation;
            public Vec3d offset;

            public ModelDisplayPiece(ArmorStandEntity armorStand, int elementID) {
                this.armorStand = armorStand;
                this.elementID = elementID;
                this.rotation = new EulerAngle(0.0f, 0.0f, 0.0f);
                this.offset = Vec3d.ZERO;
            }
        }
    }
}
