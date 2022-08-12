package com.slimeist.server_mobs.server_rendering.model.elements;

import com.google.gson.*;
import com.slimeist.server_mobs.ServerMobsMod;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;

import java.lang.reflect.Type;

public record ModelBox(String name, Vec3f from, Vec3f to,
                       ModelUV northUV,
                       ModelUV eastUV,
                       ModelUV southUV,
                       ModelUV westUV,
                       ModelUV upUV,
                       ModelUV downUV,
                       String uuid,
                       Vec3f rotation_origin,
                       float rotation_amt,
                       Direction.Axis rotation_axis) implements IBakedModelPart {

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

        protected Vec3f rotation_origin = Vec3f.ZERO;
        protected float rotation_amt = 0;
        protected Direction.Axis rotation_axis = Direction.Axis.X;

        public Builder() {
        }

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

        public String getName() {
            return this.name;
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

        public void setRotationOrigin(Vec3f origin) {
            this.rotation_origin = origin;
        }

        public void setRotationAmount(float amount) {
            this.rotation_amt = amount;
        }

        public void setRotationAxis(Direction.Axis axis) {
            this.rotation_axis = axis;
        }

        public ModelBox build() {
            Vec3f inflateVec = new Vec3f(inflate, inflate, inflate);
            from.subtract(inflateVec);
            to.add(inflateVec);
            return new ModelBox(name, from, to, northUV, eastUV, southUV, westUV, upUV, downUV, uuid, rotation_origin, rotation_amt, rotation_axis);
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
            //Rotation
            if (object.has("origin")) {
                JsonArray origin = object.get("origin").getAsJsonArray();
                if (origin.size() == 3) {
                    builder.setRotationOrigin(new Vec3f(origin.get(0).getAsFloat(), origin.get(1).getAsFloat(), origin.get(2).getAsFloat()));
                }
            }
            if (object.has("rotation")) {
                JsonArray rotation = object.get("rotation").getAsJsonArray();
                if (rotation.size() == 3) {
                    float x = rotation.get(0).getAsFloat();
                    float y = rotation.get(1).getAsFloat();
                    float z = rotation.get(2).getAsFloat();

                    int non_zero = 0;
                    int index = 0;
                    float rotation_amt = 0;
                    Direction.Axis axis = null;
                    for (float v : new float[]{x, y, z}) {
                        if (v != 0) {
                            non_zero++;
                            rotation_amt = v;
                            axis = Direction.Axis.values()[index];
                        }
                        index++;
                    }

                    if (non_zero == 1) {
                        if (rotation_amt == -45f || rotation_amt == -22.5f || rotation_amt == 0f || rotation_amt == 22.5f || rotation_amt == 45f) {
                            builder.setRotationAmount(rotation_amt);
                            builder.setRotationAxis(axis);
                        } else {
                            ServerMobsMod.LOGGER.error("Loading element with name " + builder.getName() + ", rotation " + rotation_amt + " invalid for axis " + axis.getName());
                        }
                    } else if (non_zero != 0) {
                        ServerMobsMod.LOGGER.error("Loading element with name " + builder.getName() + ", can only rotate in one axis");
                    }
                }
            }

            return builder.build();
        }

        private ModelUV deserializeFace(String name, JsonObject parent) {
            if (parent.has(name)) {
                JsonObject face = parent.getAsJsonObject(name);
                int rotation = 0;
                if (face.has("rotation")) {
                    rotation = face.get("rotation").getAsInt();
                }
                if (face.has("uv")) {
                    JsonArray uv = face.getAsJsonArray("uv");
                    if (uv.size() == 4) {
                        return new ModelUV(uv.get(0).getAsFloat(), uv.get(1).getAsFloat(), uv.get(2).getAsFloat(), uv.get(3).getAsFloat(), rotation);
                    }
                } else {
                    return new ModelUV(0, 0, 0, 0, rotation);
                }
            }
            return ModelUV.ZERO;
        }
    }
}
