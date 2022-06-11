package com.slimeist.server_mobs.server_rendering.model.elements.outline;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.slimeist.server_mobs.server_rendering.model.UnbakedServerEntityModel;
import com.slimeist.server_mobs.server_rendering.model.elements.IBakedModelPart;

import java.lang.reflect.Type;

public record SingleElement(String uuid) implements OutlineElement {

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public IBakedModelPart bake(UnbakedServerEntityModel unbakedServerEntityModel) {
        return unbakedServerEntityModel.getBox(uuid);
    }

    public static class Deserializer implements JsonDeserializer<SingleElement> {

        @Override
        public SingleElement deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return new SingleElement(jsonElement.getAsString());
        }
    }
}
