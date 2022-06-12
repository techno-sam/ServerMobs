package com.slimeist.server_mobs.server_rendering.model.elements;

import com.slimeist.server_mobs.ServerMobsMod;
import com.slimeist.server_mobs.server_rendering.model.ModelTransform;
import eu.pb4.polymer.api.resourcepack.PolymerModelData;
import net.minecraft.util.math.Vec3f;

import java.util.ArrayList;
import java.util.Arrays;

public class ModelGroup implements IBakedModelPart {
    public final String name;
    public final ModelTransform transform;
    public final ModelGroup[] childGroups;
    public final ModelBox[] boxes;
    public final String uuid;
    protected PolymerModelData displayData = null;

    public ModelGroup copy() {
        return new ModelGroup(name, transform, childGroups, boxes, uuid, displayData);
    }

    protected ModelGroup(String name, ModelTransform transform, ModelGroup[] childGroups, ModelBox[] boxes, String uuid, PolymerModelData displayData) {
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
                ServerMobsMod.LOGGER.warn("Duplicate name: "+modelGroup.name);
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
        return displayData;
    }

    public void setDisplayData(PolymerModelData displayData) {
        this.displayData = displayData;
    }
}
