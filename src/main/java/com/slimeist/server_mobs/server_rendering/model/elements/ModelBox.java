package com.slimeist.server_mobs.server_rendering.model.elements;

import com.google.gson.*;
import net.minecraft.util.math.Vec3f;

import java.lang.reflect.Type;

public record ModelBox(String name, Vec3f from, Vec3f to,
                       ModelUV northUV,
                       ModelUV eastUV,
                       ModelUV southUV,
                       ModelUV westUV,
                       ModelUV upUV,
                       ModelUV downUV,
                       String uuid) implements IBakedModelPart {

    @Override
    public String getName() {
        return this.name();
    }

    static class Builder {
        protected String name = "";
        protected Vec3f from = Vec3f.ZERO;
        protected Vec3f to = Vec3f.ZERO;
        protected ModelUV northUV = ModelUV.ZERO;
        protected ModelUV eastUV = ModelUV.ZERO;
        protected ModelUV southUV = ModelUV.ZERO;
        protected ModelUV westUV = ModelUV.ZERO;
        protected ModelUV upUV = ModelUV.ZERO;
        protected ModelUV downUV = ModelUV.ZERO;
        protected String uuid = "";
        protected float inflate = 0;

        public Builder() {}

        public Builder(ModelBox box) {
            name = box.name;
            from = box.from;
            to = box.to;
            northUV = box.northUV;
            eastUV = box.eastUV;
            southUV = box.southUV;
            westUV = box.westUV;
            upUV = box.upUV;
            downUV = box.downUV;
            uuid = box.uuid;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setFrom(Vec3f from) {
            this.from = from;
        }

        public void setTo(Vec3f to) {
            this.to = to;
        }

        public void setNorthUV(ModelUV northUV) {
            this.northUV = northUV;
        }

        public void setEastUV(ModelUV eastUV) {
            this.eastUV = eastUV;
        }

        public void setSouthUV(ModelUV southUV) {
            this.southUV = southUV;
        }

        public void setWestUV(ModelUV westUV) {
            this.westUV = westUV;
        }

        public void setUpUV(ModelUV upUV) {
            this.upUV = upUV;
        }

        public void setDownUV(ModelUV downUV) {
            this.downUV = downUV;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public void setInflate(float inflate) {
            this.inflate = inflate;
        }

        public ModelBox build() {
            Vec3f inflateVec = new Vec3f(inflate, inflate, inflate);
            from.subtract(inflateVec);
            to.add(inflateVec);
            return new ModelBox(name, from, to, northUV, eastUV, southUV, westUV, upUV, downUV, uuid);
        }
    }

    public static class Deserializer implements JsonDeserializer<ModelBox> {

        @Override
        public ModelBox deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();
            Builder builder = new Builder();
            if (object.has("name")) {
                builder.setName(object.get("name").getAsString());
            }
            if (object.has("from")) {
                JsonArray from = object.get("from").getAsJsonArray();
                if (from.size() == 3) {
                    builder.setFrom(new Vec3f(from.get(0).getAsFloat(), from.get(1).getAsFloat(), from.get(2).getAsFloat()));
                }
            }
            if (object.has("to")) {
                JsonArray to = object.get("to").getAsJsonArray();
                if (to.size() == 3) {
                    builder.setTo(new Vec3f(to.get(0).getAsFloat(), to.get(1).getAsFloat(), to.get(2).getAsFloat()));
                }
            }
            if (object.has("uuid")) {
                builder.setUuid(object.get("uuid").getAsString());
            }
            if (object.has("inflate")) {
                builder.setInflate(object.get("inflate").getAsFloat());
            }
            //UVs
            if (object.has("faces")) {
                JsonObject faces = object.getAsJsonObject("faces");
                builder.setNorthUV(deserializeFace("north", faces));
                builder.setEastUV(deserializeFace("east", faces));
                builder.setSouthUV(deserializeFace("south", faces));
                builder.setWestUV(deserializeFace("west", faces));
                builder.setUpUV(deserializeFace("up", faces));
                builder.setDownUV(deserializeFace("down", faces));
            }
            return builder.build();
        }

        private ModelUV deserializeFace(String name, JsonObject parent) {
            if (parent.has(name)) {
                JsonObject face = parent.getAsJsonObject(name);
                if (face.has("uv")) {
                    JsonArray uv = face.getAsJsonArray("uv");
                    if (uv.size() == 4) {
                        return new ModelUV(uv.get(0).getAsFloat(), uv.get(1).getAsFloat(), uv.get(2).getAsFloat(), uv.get(3).getAsFloat());
                    }
                }
            }
            return ModelUV.ZERO;
        }
    }
}
