package com.slimeist.server_mobs.server_rendering.model;

import com.google.gson.*;
import com.slimeist.server_mobs.server_rendering.model.elements.ModelBox;
import com.slimeist.server_mobs.server_rendering.model.elements.ModelGroup;
import com.slimeist.server_mobs.server_rendering.model.elements.outline.GroupElement;
import com.slimeist.server_mobs.server_rendering.model.elements.outline.OutlineElement;
import com.slimeist.server_mobs.server_rendering.model.elements.outline.SingleElement;
import net.minecraft.util.JsonHelper;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class UnbakedServerEntityModel {
    static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(UnbakedServerEntityModel.class, new Deserializer())
            .registerTypeAdapter(ModelBox.class, new ModelBox.Deserializer())
            .registerTypeAdapter(GroupElement.class, new GroupElement.Deserializer())
            .registerTypeAdapter(SingleElement.class, new SingleElement.Deserializer())
            .create();
    public final int texWidth;
    public final int texHeight;
    public final ModelBox[] modelBoxes;
    public final OutlineElement[] outlineElements;
    private BakedServerEntityModel baked;

    public UnbakedServerEntityModel(int texWidth, int texHeight, ModelBox[] modelBoxes, OutlineElement[] outlineElements) {
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.modelBoxes = modelBoxes;
        this.outlineElements = outlineElements;
        this.baked = null;
    }

    public ModelBox getBox(String uuid) {
        for (ModelBox box : modelBoxes) {
            if (box.uuid().equals(uuid)) {
                return box;
            }
        }
        return null;
    }

    public BakedServerEntityModel bake(boolean forceMarker) {
        if (this.baked==null) {
            GroupElement baseElement = new GroupElement("base", ModelTransform.NONE, "base-uuid", outlineElements);
            ModelGroup base = baseElement.bake(this);
            this.baked = new BakedServerEntityModel(this.texWidth, this.texHeight, base, forceMarker);
        }
        return this.baked;
    }

    public static UnbakedServerEntityModel deserialize(Reader input) {
        return JsonHelper.deserialize(GSON, input, UnbakedServerEntityModel.class);
    }

    public static UnbakedServerEntityModel deserialize(String json) {
        return deserialize(new StringReader(json));
    }

    public static class Deserializer implements JsonDeserializer<UnbakedServerEntityModel> {

        @Override
        public UnbakedServerEntityModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            int texWidth = 16;
            int texHeight = 16;
            if (jsonObject.has("resolution")) {
                JsonObject resolution = jsonObject.getAsJsonObject("resolution");
                if (resolution.has("width")) {
                    texWidth = resolution.get("width").getAsInt();
                }
                if (resolution.has("height")) {
                    texHeight = resolution.get("height").getAsInt();
                }
            }
            ArrayList<ModelBox> elements = new ArrayList<>();
            if (jsonObject.has("elements")) {
                JsonArray array = jsonObject.getAsJsonArray("elements");
                for (JsonElement value : array) {
                    JsonObject element = value.getAsJsonObject();
                    elements.add(jsonDeserializationContext.deserialize(element, ModelBox.class));
                }
            }
            OutlineElement[] outlineElements = new OutlineElement[0];
            if (jsonObject.has("outliner")) {
                JsonObject outlineElementsJson = new JsonObject();
                outlineElementsJson.add("children", jsonObject.getAsJsonArray("outliner"));
                GroupElement baseGroupElement = jsonDeserializationContext.deserialize(outlineElementsJson, GroupElement.class);
                outlineElements = baseGroupElement.children();
            }

            return new UnbakedServerEntityModel(texWidth, texHeight, elements.toArray(new ModelBox[0]), outlineElements);
        }
    }
}
