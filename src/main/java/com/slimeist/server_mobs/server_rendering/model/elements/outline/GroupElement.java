package com.slimeist.server_mobs.server_rendering.model.elements.outline;

import com.google.gson.*;
import com.slimeist.server_mobs.ServerMobsMod;
import com.slimeist.server_mobs.server_rendering.model.ModelTransform;
import com.slimeist.server_mobs.server_rendering.model.UnbakedServerEntityModel;
import com.slimeist.server_mobs.server_rendering.model.elements.ModelBox;
import com.slimeist.server_mobs.server_rendering.model.elements.ModelGroup;
import net.minecraft.util.math.Vec3f;

import java.lang.reflect.Type;
import java.util.ArrayList;

public record GroupElement(String name,
                           ModelTransform transform,
                           String uuid,
                           OutlineElement[] children) implements OutlineElement {

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public ModelGroup bake(UnbakedServerEntityModel unbakedServerEntityModel) {
        ArrayList<ModelGroup> childGroups = new ArrayList<>();
        ArrayList<ModelBox> boxes = new ArrayList<>();
        for (OutlineElement childElement : this.children) {
            if (childElement instanceof SingleElement) {
                boxes.add(unbakedServerEntityModel.getBox(childElement.getUUID()));
            } else if (childElement instanceof GroupElement groupElement) {
                childGroups.add(groupElement.bake(unbakedServerEntityModel));
            }
        }
        return new ModelGroup(this.name, this.transform, childGroups.toArray(new ModelGroup[0]), boxes.toArray(new ModelBox[0]), this.uuid);
    }

    public static class Deserializer implements JsonDeserializer<GroupElement> {

        @Override
        public GroupElement deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();
            String name = "";
            Vec3f origin = Vec3f.ZERO;
            Vec3f rotation = Vec3f.ZERO;
            String uuid = "";
            ArrayList<OutlineElement> childElements = new ArrayList<>();
            if (object.has("name")) {
                name = object.get("name").getAsString();
            }
            if (object.has("origin")) {
                JsonArray array = object.getAsJsonArray("origin");
                if (array.size() == 3) {
                    origin = new Vec3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
                }
                //ServerMobsMod.LOGGER.info("Origin of element "+name+" is: "+origin);
            }
            if (object.has("rotation")) {
                JsonArray array = object.getAsJsonArray("rotation");
                if (array.size() == 3) {
                    rotation = new Vec3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
                }
            }
            if (object.has("uuid")) {
                uuid = object.get("uuid").getAsString();
            }
            if (object.has("children")) {
                JsonArray jsonChildren = object.getAsJsonArray("children");
                for (JsonElement child : jsonChildren) {
                    if (child.isJsonObject()) {
                        childElements.add(jsonDeserializationContext.deserialize(child, GroupElement.class));
                    } else {
                        childElements.add(jsonDeserializationContext.deserialize(child, SingleElement.class));
                    }
                }
            }
            return new GroupElement(name,
                    ModelTransform.of(origin.getX(), origin.getY(), origin.getZ(), rotation.getX(), rotation.getY(), rotation.getZ()),
                    uuid,
                    childElements.toArray(new OutlineElement[0]));
        }
    }
}
