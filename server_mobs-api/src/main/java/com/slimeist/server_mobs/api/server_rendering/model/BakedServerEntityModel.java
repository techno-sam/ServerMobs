package com.slimeist.server_mobs.api.server_rendering.model;

import com.slimeist.server_mobs.api.ServerMobsApiMod;
import com.slimeist.server_mobs.api.mixin.ArmorStandEntityAccessor;
import com.slimeist.server_mobs.api.server_rendering.entity.IServerRenderedEntity;
import com.slimeist.server_mobs.api.server_rendering.hologram.ArmorStandHologramElement;
import com.slimeist.server_mobs.api.server_rendering.model.elements.ModelGroup;
import com.slimeist.server_mobs.api.util.VectorUtil;
import eu.pb4.holograms.api.elements.HologramElement;
import eu.pb4.holograms.api.holograms.EntityHologram;
import eu.pb4.polymer.api.resourcepack.PolymerModelData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

public record BakedServerEntityModel(int texWidth, int texHeight,
                                     ModelGroup base, boolean forceMarker) {

    public <T extends Entity & IServerRenderedEntity> Instance createInstance(T entity) {
        return new Instance(this, entity);
    }

    public static class Instance {
        private final BakedServerEntityModel parent;
        private final Entity entity;
        private final EntityHologram hologram;
        private final HashMap<ModelGroup, ModelDisplayPiece> displayPieces;
        private boolean hologramDirty;
        private boolean initializedAngles = false;

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

        @Nullable
        private String getPath(ModelGroup group) {
            ModelDisplayPiece piece = this.displayPieces.get(group);
            return piece == null ? null : piece.path;
        }

        private ItemStack createDisplayStack(ModelGroup group, boolean damageFlash) {
            PolymerModelData data = group.getDisplayData(damageFlash);
            ItemStack displayStack = new ItemStack(data.item(), 1);
            NbtCompound nbt = displayStack.getOrCreateNbt();

            nbt.putInt("CustomModelData", data.value());

            /*{
                NbtCompound display = new NbtCompound();
                display.putInt("color", damageFlash ? 0xFF9595 : -1); //-1 is white
                nbt.put("display", display);
            }*/

            displayStack.setNbt(nbt);
            return displayStack;
        }

        private void setupArmorStand(String modelPath) {
            ModelGroup group = getModelGroup(modelPath);
            if (modelPath.equals("hitbox")) {
                return;
            }
            if (group == null) {
                ServerMobsApiMod.LOGGER.warn("Failed to create armor stand for path " + modelPath + " because that path does not exist.");
                return;
            }  // ServerMobsMod.LOGGER.info("Creating armor stand for path ["+modelPath+"].");

            ItemStack displayStack = createDisplayStack(group, false);

            ArmorStandEntity armorStand = new ArmorStandEntity(this.entity.getWorld(), this.entity.getX(), this.entity.getY(), this.entity.getZ());
            armorStand.equipStack(EquipmentSlot.HEAD, displayStack);
            armorStand.setYaw(0.0f);
            armorStand.setBodyYaw(0.0f);
            armorStand.setHeadYaw(0.0f);
            armorStand.setBodyRotation(ArmorStandEntityAccessor.getDEFAULT_BODY_ROTATION());
            armorStand.setHeadRotation(ArmorStandEntityAccessor.getDEFAULT_HEAD_ROTATION());
            ((ArmorStandEntityAccessor) armorStand).invokeSetMarker(group.getArmorStandScale().small || parent.forceMarker);
            ((ArmorStandEntityAccessor) armorStand).invokeSetSmall(group.getArmorStandScale().small);
            armorStand.setInvisible(true);
            ArmorStandHologramElement element = new ArmorStandHologramElement(armorStand, true);
            Vec3d pivot = VectorUtil.toVec3d(group.transform.pivotVec());
            Vec3d base_offset = new Vec3d(0, group.getArmorStandScale().small ? (-0.645) : (-1.4385), 0); //was +0.171875
            element.setOffset(base_offset.add(pivot.multiply(1 / 16.0d).multiply(-1, 1, -1))); //123456789
            int id = this.hologram.addElement(element);
            ModelDisplayPiece displayPiece = new ModelDisplayPiece(armorStand, id, VectorUtil.toVec3d(group.transform.pivotVec()), base_offset, modelPath);
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
                    childPaths.forEach((childName) -> out.add(name + "." + childName));
                }
            }
            return out;
        }

        protected void initializeHologram() {
            this.clear();

            ArrayList<String> childPaths = getChildPaths(this.parent.base());
            childPaths.forEach(this::setupArmorStand);

            //this.hologram.setOffset(new Vec3d(0, ServerMobsMod.SMALL_STANDS ? (-0.4475*2) : (-1.4385), 0)); //was -0.4475*2 with baby // do this in individual element to account for different sizes
            this.hologram.show();
        }

        public static Optional<String> parentPath(String path) {
            String[] pieces = path.split("\\.");
            if (pieces.length > 1) {
                StringBuilder parentPath = new StringBuilder("base");
                for (int i = 1; i < pieces.length - 1; i++) {
                    parentPath.append(".").append(pieces[i]);
                }
                return Optional.of(parentPath.toString());
            }
            return Optional.empty();
        }

        public void updateHologram() {
            //this.hologram.addItemStack(this.itemStack, false);
            //this.hologram.setOffset(new Vec3d(0, -1.1865, 0));
            if (this.entity instanceof IServerRenderedEntity serverRenderedEntity) {
                if (!this.initializedAngles) {
                    serverRenderedEntity.initAngles();
                    this.initializedAngles = true;
                }
                serverRenderedEntity.updateAngles();
                updateParenting(this.parent.base());

                String nametagPath = serverRenderedEntity.getNametagPath();
                this.displayPieces.forEach((group, display) -> {
                    if (display.path.equals(nametagPath)) {
                        display.armorStand.setCustomName(this.entity.getDisplayName());
                        display.armorStand.setCustomNameVisible(this.entity.hasCustomName());
                    } else {
                        display.armorStand.setCustomName(LiteralText.EMPTY);
                        display.armorStand.setCustomNameVisible(false);
                    }
                });
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

        private static EulerAngle add(EulerAngle a, EulerAngle b) {
            return new EulerAngle(a.getPitch() + b.getPitch(), a.getYaw() + b.getYaw(), a.getRoll() + b.getRoll());
        }

        protected void updateParenting(ModelGroup part) {
            updateParenting(part, null);
        }

        protected void updateParenting(ModelGroup part, @Nullable ModelGroup parent) {
            if (parent != null) {
                String partPath = getPath(part);
                String parentPath = getPath(parent);
                if (partPath != null && parentPath != null && getPartParentLocal(partPath)) {
                    ModelDisplayPiece piece = this.displayPieces.get(part);
                    ModelDisplayPiece parentPiece = this.displayPieces.get(parent);
                    if (piece.parentRelativePivot != null) {

                        EulerAngle parentRot = getPartRotation(parentPath);
                        double x_rad = Math.toRadians(parentRot.getWrappedPitch());
                        double y_rad = Math.toRadians(-parentRot.getWrappedYaw());
                        double z_rad = Math.toRadians(-parentRot.getWrappedRoll());

                        /*Vec3d rotatedRelativePivot = piece.parentRelativePivot
                                .rotateZ(z_rad)
                                .rotateX(x_rad)
                                .rotateY(y_rad);*/
                        Vec3d rotatedRelativePivot = ModelUtil.rotate(piece.parentRelativePivot, x_rad, y_rad, z_rad);

                        setPartPivot(partPath, getPartPivot(parentPath).add(rotatedRelativePivot));

                        setPartRotation(partPath, add(getPartRotation(parentPath), getPartRelativeRotation(partPath)));
                    }
                }
            }
            for (ModelGroup child : part.childGroups) {
                updateParenting(child, part);
            }
        }

        /**
         * Marks hologram as dirty, will call position sync
         */
        private void markDirty() {
            this.hologramDirty = true;
        }

        public void setDamageFlash(boolean damageFlash) {
            setDamageFlash(damageFlash, false);
        }

        public void setDamageFlash(boolean damageFlash, boolean invisible) {
            for (String path : getChildPaths(this.parent.base())) {
                ModelGroup group = getModelGroup(path);
                if (group != null) {
                    ModelDisplayPiece piece = this.displayPieces.get(group);
                    piece.armorStand.equipStack(EquipmentSlot.HEAD, invisible ? ItemStack.EMPTY : createDisplayStack(group, damageFlash));
                    if (hologram.getElement(piece.elementID) instanceof ArmorStandHologramElement armorStandHologramElement) {
                        armorStandHologramElement.markEquipmentDirty();
                    }
                }
            }
        }

        public boolean setPartDebug(String path, boolean debug) {
            ModelGroup part = this.getModelGroup(path);
            if (part == null) {
                return false;
            }
            if (getPartDebug(path) != debug) {
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

        public boolean setPartParentLocal(String path, boolean parentLocal) {
            ModelGroup part = this.getModelGroup(path);
            if (part == null) {
                return false;
            }
            if (getPartParentLocal(path) != parentLocal) {
                this.markDirty();
            }
            ModelDisplayPiece piece = this.displayPieces.get(part);
            piece.parentLocalMovement = parentLocal;
            if (parentLocal) {
                Optional<String> optionalParentPath = parentPath(path);
                if (optionalParentPath.isEmpty()) {
                    throw new IllegalArgumentException("Cannot set parent local to true for a path without a parent");
                } else {
//                    ModelGroup parentPart = this.getModelGroup(optionalParentPath.get());
//                    ModelDisplayPiece parentPiece = this.displayPieces.get(parentPart);
                    //parent relative pivot   =     my pivot              -                  parent pivot
                    piece.parentRelativePivot = getPartPivot(path).subtract(getPartPivot(optionalParentPath.get()));
                    //ServerMobsMod.LOGGER.info("set PartParentLocal to "+parentLocal+" for part "+path+", parent: "+optionalParentPath.get()+", pivot: "+getPartDefaultPivot(path)+", parent pivot: "+getPartDefaultPivot(optionalParentPath.get())+", relative pivot: "+piece.parentRelativePivot);
                }
            }
            return true;
        }

        public boolean getPartParentLocal(String path) {
            ModelGroup part = this.getModelGroup(path);
            if (part == null) {
                return false;
            }
            return this.displayPieces.get(part).parentLocalMovement;
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

        public boolean setPartRelativeRotation(String path, EulerAngle angle) {
            ModelGroup part = this.getModelGroup(path);
            if (part == null) {
                return false;
            }
            if (!getPartRelativeRotation(path).equals(angle)) {
                this.markDirty();
            }
            this.displayPieces.get(part).relativeRotation = angle;
            return true;
        }

        public EulerAngle getPartRelativeRotation(String path) {
            ModelGroup part = this.getModelGroup(path);
            if (part == null) {
                return new EulerAngle(0, 0, 0);
            }
            return this.displayPieces.get(part).relativeRotation;
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

        public <L extends LivingEntity> void handleDamageFlash(L livingEntity) {
            boolean invisible = livingEntity.isInvisible();
            if (!livingEntity.isAlive()) {
                this.setDamageFlash(true, invisible);
            } else {
                this.setDamageFlash(livingEntity.hurtTime > 0, invisible);
            }
        }

        public static class ModelDisplayPiece {
            public final ArmorStandEntity armorStand;
            public final int elementID;
            public final String path;
            public final Vec3d base_offset;
            public EulerAngle rotation;
            public Vec3d pivot;
            public boolean parentLocalMovement;
            public Vec3d parentRelativePivot;
            public EulerAngle relativeRotation;

            public ModelDisplayPiece(ArmorStandEntity armorStand, int elementID, Vec3d pivot, Vec3d base_offset, String path) {
                this(armorStand, elementID, pivot, base_offset, path, null, false);
            }

            public ModelDisplayPiece(ArmorStandEntity armorStand, int elementID, Vec3d pivot, Vec3d base_offset, String path, Vec3d parentRelativePivot, boolean parentLocalMovement) {
                this.armorStand = armorStand;
                this.elementID = elementID;
                this.rotation = new EulerAngle(0.0f, 0.0f, 0.0f);
                this.relativeRotation = new EulerAngle(0.0f, 0.0f, 0.0f);
                this.pivot = pivot;
                this.base_offset = base_offset;
                this.path = path;
                this.parentLocalMovement = parentLocalMovement;
                this.parentRelativePivot = parentRelativePivot;
            }

            public void applyOffset(EntityHologram hologram) {
                if (this.getElement(hologram) instanceof ArmorStandHologramElement armorStandHologramElement) {
                    armorStandHologramElement.setOffset(this.base_offset.add(this.pivot.multiply(1 / 16.0d).multiply(-1, 1, -1))); //123456789
                }
            }

            public HologramElement getElement(EntityHologram hologram) {
                return hologram.getElement(this.elementID);
            }
        }
    }
}
