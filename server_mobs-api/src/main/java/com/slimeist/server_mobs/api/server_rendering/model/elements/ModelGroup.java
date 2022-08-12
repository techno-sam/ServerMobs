package com.slimeist.server_mobs.api.server_rendering.model.elements;

import com.slimeist.server_mobs.api.ServerMobsApiMod;
import com.slimeist.server_mobs.api.server_rendering.model.ModelTransform;
import com.slimeist.server_mobs.api.server_rendering.model.ScaleUtils;
import eu.pb4.polymer.api.resourcepack.PolymerModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

public class ModelGroup implements IBakedModelPart {
    public final String name;
    public final ModelTransform transform;
    public final ModelGroup[] childGroups;
    public final ModelBox[] boxes;
    public final String uuid;
    protected ScaleUtils.Scale standScale = ScaleUtils.Scale.SMALL;
    protected PolymerModelData displayData = null;
    protected PolymerModelData tintDisplayData = null;

    public ModelGroup copy() {
        return new ModelGroup(name, transform, childGroups, boxes, uuid, displayData, tintDisplayData);
    }

    protected ModelGroup(String name, ModelTransform transform, ModelGroup[] childGroups, ModelBox[] boxes, String uuid, PolymerModelData displayData, @Nullable PolymerModelData tintDisplayData) {
        this.name = name;
        this.transform = transform;
        this.childGroups = childGroups;
        this.boxes = boxes;
        this.uuid = uuid;
        this.displayData = displayData;
    }

    public ModelGroup(String name, ModelTransform transform, ModelGroup[] childGroups, ModelBox[] boxes, String uuid) {
        this.name = name;
        this.transform = transform;//ModelTransform.rotation(transform.pitch, transform.yaw, transform.roll);
        this.childGroups = childGroups;
        /*ModelBox[] wipBoxes = new ModelBox[boxes.length];
        for (int i=0; i<boxes.length; i++) {
            ModelBox box = boxes[i];
            ModelBox.Builder b = new ModelBox.Builder(box);
            Vec3f origin = new Vec3f(transform.pivotX, transform.pivotY, transform.pivotZ);
            b.from.subtract(origin);
            b.to.subtract(origin);
            wipBoxes[i] = b.build();
        }*/
        ArrayList<String> names = new ArrayList<>();
        Arrays.stream(this.childGroups).forEach((modelGroup -> {
            if (names.contains(modelGroup.name)) {
                ServerMobsApiMod.LOGGER.warn("Duplicate name: " + modelGroup.name);
            }
            names.add(modelGroup.name);
        }));
        this.boxes = boxes;
        this.uuid = uuid;
    }

    public ModelGroup getChild(String name) {
        for (ModelGroup group : this.childGroups) {
            if (group.name.equals(name)) {
                return group;
            }
        }
        return null;
    }

    public String[] getChildNames() {
        ArrayList<String> names = new ArrayList<>();
        Arrays.stream(this.childGroups).forEach((modelGroup -> names.add(modelGroup.name)));
        return names.toArray(new String[0]);
    }

    @Override
    public String getName() {
        return name;
    }

    public PolymerModelData getDisplayData() {
        return getDisplayData(false);
    }

    public PolymerModelData getDisplayData(boolean damageTint) {
        return (doesDamageTint() && damageTint) ? tintDisplayData : displayData;
    }

    public boolean doesDamageTint() {
        return tintDisplayData != null;
    }

    public PolymerModelData getTintDisplayData() {
        return tintDisplayData;
    }

    public void setDisplayData(PolymerModelData displayData) {
        this.displayData = displayData;
    }

    public void setTintDisplayData(@Nullable PolymerModelData tintDisplayData) {
        this.tintDisplayData = tintDisplayData;
    }

    public ScaleUtils.Scale getArmorStandScale() {
        return standScale;
    }

    public void setArmorStandScale(ScaleUtils.Scale standScale) {
        this.standScale = standScale;
    }
}
