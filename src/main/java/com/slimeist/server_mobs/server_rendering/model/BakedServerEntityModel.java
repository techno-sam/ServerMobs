package com.slimeist.server_mobs.server_rendering.model;

import com.slimeist.server_mobs.ServerMobsMod;
import com.slimeist.server_mobs.mixin.ArmorStandEntityAccessor;
import com.slimeist.server_mobs.server_rendering.entity.IServerRenderedEntity;
import com.slimeist.server_mobs.server_rendering.hologram.ArmorStandHologramElement;
import com.slimeist.server_mobs.server_rendering.model.elements.ModelGroup;
import com.slimeist.server_mobs.util.VectorUtil;
import eu.pb4.holograms.api.holograms.EntityHologram;
import eu.pb4.polymer.api.resourcepack.PolymerModelData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.*;

//TODO (Includes GoldGolemEntity), make transforms propogate down to child elements (THIS IS GOING TO BE A LOT OF WORK)
public record BakedServerEntityModel(int texWidth, int texHeight,
                                     ModelGroup base) {

    public <T extends Entity & IServerRenderedEntity> Instance createInstance(T entity) {
        return new Instance(this, entity);
    }

    public static class Instance {
        private BakedServerEntityModel parent;
        private Entity entity;
        private EntityHologram hologram;
        private HashMap<ModelGroup, ModelDisplayPiece> displayPieces;
        private boolean hologramDirty;

        protected <T extends Entity & IServerRenderedEntity> Instance(BakedServerEntityModel parent, T entity) {
            this.parent = parent;
            this.entity = entity;
            this.hologram = new EntityHologram(this.entity, Vec3d.ZERO);
            this.displayPieces = new HashMap<>();
            this.hologramDirty = true;
            this.initializeHologram();
        }

        @Nullable
        public ModelGroup getModelPart(String modelPath) {
            ModelGroup part = this.getModelGroup(modelPath);
            return part == null ? null : part.copy();
        }

        @Nullable
        private ModelGroup getModelGroup(String modelPath) {
            String[] pathParts = modelPath.split("\\.");
            ModelGroup modelGroup = parent.base().getChild("base");
            for (int i = pathParts[0].equals("base") ? 1 : 0; i < pathParts.length; i++) {
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
                ServerMobsMod.LOGGER.warn("Failed to create armor stand for path "+modelPath+" because that path does not exist.");
                return;
            } else {
                ServerMobsMod.LOGGER.info("Creating armor stand for path ["+modelPath+"].");
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
            ((ArmorStandEntityAccessor) armorStand).invokeSetSmall(true);
            armorStand.setInvisible(true);
            ArmorStandHologramElement element = new ArmorStandHologramElement(armorStand, true);
            Vec3d pivot = VectorUtil.toVec3d(group.transform.pivotVec());
            element.setOffset(pivot.multiply(1/16.0d).multiply(-1, 1, 1));
            int id = this.hologram.addElement(element);
            ModelDisplayPiece displayPiece = new ModelDisplayPiece(armorStand, id, VectorUtil.toVec3d(group.transform.pivotVec()));
            this.displayPieces.put(group, displayPiece);
        }

        protected void clear() {
            this.hologram.hide();
            this.hologram.clearElements();
            this.displayPieces.forEach(((modelGroup, modelDisplayPiece) -> modelDisplayPiece.armorStand.remove(Entity.RemovalReason.DISCARDED)));
            this.displayPieces.clear();
        }

        private ArrayList<String> getChildPaths(ModelGroup group) {
            return getChildPaths(new ArrayList<>(Collections.singletonList(group)));
        }

        private ArrayList<String> getChildPaths(ArrayList<ModelGroup> building) {
            ArrayList<String> out = new ArrayList<>();
            for (ModelGroup group : building) {
                for (String name : group.getChildNames()) {
                    out.add(name);
                    ArrayList<String> childPaths = getChildPaths(group.getChild(name));
                    childPaths.forEach((childName) -> out.add(name+"."+childName));
                }
            }
            return out;
        }

        protected void initializeHologram() {
            this.clear();

            ArrayList<String> childPaths = getChildPaths(this.parent.base());
            childPaths.forEach(this::setupArmorStand);

            this.hologram.setOffset(new Vec3d(0, -0.4475*2, 0));
            this.hologram.show();
        }

        public void updateHologram() {
            //this.hologram.addItemStack(this.itemStack, false);
            //this.hologram.setOffset(new Vec3d(0, -1.1865, 0));
            if (this.entity instanceof IServerRenderedEntity serverRenderedEntity) {
                serverRenderedEntity.setupAngles();
            }
            /*this.displayPieces.forEach(((modelGroup, modelDisplayPiece) -> {
                modelDisplayPiece.armorStand.setInvisible(false);
            }));*/
            if (this.hologramDirty) {
                this.hologram.syncPositionWithEntity();
                this.hologramDirty = false;
            }
            /*ArmorStandEntity armorStand = this.displayPieces.get(getModelGroup("base.body_bottom")).armorStand;
            armorStand.setHeadRotation(new EulerAngle(0.0f, (float) (this.entity.bodyYaw), 0.0f));
            armorStand.setInvisible(false);*/
        }

        /**
         * Marks hologram as dirty, will call position sync
         */
        private void markDirty() {
            this.hologramDirty = true;
        }

        public boolean setPartDebug(String path, boolean debug) {
            ModelGroup part = this.getModelGroup(path);
            if (part == null) {
                return false;
            }
            if (getPartDebug(path)!=debug) {
                this.markDirty();
            }
            this.displayPieces.get(part).armorStand.setInvisible(debug);
            return true;
        }

        public boolean getPartDebug(String path) {
            ModelGroup part = this.getModelGroup(path);
            if (part == null) {
                return false;
            }
            return this.displayPieces.get(part).armorStand.isInvisible();
        }

        public boolean setPartRotation(String path, EulerAngle angle) {
            ModelGroup part = this.getModelGroup(path);
            if (part == null) {
                return false;
            }
            if (!getPartRotation(path).equals(angle)) {
                this.markDirty();
            }
            this.displayPieces.get(part).rotation = angle;
            this.displayPieces.get(part).armorStand.setHeadRotation(angle);
            return true;
        }

        public EulerAngle getPartRotation(String path) {
            ModelGroup part = this.getModelGroup(path);
            if (part == null) {
                return new EulerAngle(0, 0, 0);
            }
            return this.displayPieces.get(part).rotation;
        }

        public boolean setPartPivot(String path, Vec3d pivot) {
            ModelGroup part = this.getModelGroup(path);
            if (part == null) {
                return false;
            }
            if (!getPartPivot(path).equals(pivot)) {
                this.markDirty();
            }
            this.displayPieces.get(part).pivot = pivot;
            this.displayPieces.get(part).applyOffset(this.hologram);
            return true;
        }

        public Vec3d getPartPivot(String path) {
            ModelGroup part = this.getModelGroup(path);
            if (part == null) {
                return Vec3d.ZERO;
            }
            return this.displayPieces.get(part).pivot;
        }

        public Vec3d getPartDefaultPivot(String path) {
            ModelGroup part = this.getModelGroup(path);
            if (part == null) {
                return Vec3d.ZERO;
            }
            return VectorUtil.toVec3d(part.transform.pivotVec());
        }

        public static class ModelDisplayPiece {
            public final ArmorStandEntity armorStand;
            public final int elementID;
            public EulerAngle rotation;
            public Vec3d pivot;

            public ModelDisplayPiece(ArmorStandEntity armorStand, int elementID, Vec3d pivot) {
                this.armorStand = armorStand;
                this.elementID = elementID;
                this.rotation = new EulerAngle(0.0f, 0.0f, 0.0f);
                this.pivot = pivot;
            }

            public void applyOffset(EntityHologram hologram) {
                if (hologram.getElement(this.elementID) instanceof ArmorStandHologramElement armorStandHologramElement) {
                    armorStandHologramElement.setOffset(this.pivot.multiply(1/16.0d).multiply(-1, 1, 1));
                }
            }
        }
    }
}
