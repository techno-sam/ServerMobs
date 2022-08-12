package com.slimeist.server_mobs.api.server_rendering.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.slimeist.server_mobs.api.ServerMobsApiMod;
import com.slimeist.server_mobs.api.server_rendering.model.elements.ModelBox;
import com.slimeist.server_mobs.api.server_rendering.model.elements.ModelGroup;
import com.slimeist.server_mobs.api.server_rendering.model.elements.ModelUV;
import com.slimeist.server_mobs.api.util.JsonUtil;
import eu.pb4.polymer.api.resourcepack.PolymerModelData;
import eu.pb4.polymer.api.resourcepack.PolymerRPBuilder;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

/*
Strategy:
    All cubes inside a group are considered a part (will be given item model)
    All groups inside a group are separately controlled
    .bbmodel file will be assets/<NAMESPACE>/server_entities/<PATH>/<PATH>.bbmodel
    .png tex will be assets/<NAMESPACE>/server_entities/<PATH>/<PATH>.png
    tex will go to assets/<NAMESPACE>/textures/entity/<PATH>.png
    entity item models will go to assets/<NAMESPACE>/models/entity/<PATH>/[CHILD_NAMES]/model.json
 */
public class ServerEntityModelLoader {
    public final EntityType<?> entityType;
    public final boolean doDamageTint;
    protected UnbakedServerEntityModel unbakedModel = null;
    protected BakedServerEntityModel bakedModel = null;
    private boolean forceMarker = false;
    private final String model_name_override;

    public ServerEntityModelLoader(EntityType<?> entityType) {
        this(entityType, null);
    }

    public ServerEntityModelLoader(EntityType<?> entityType, boolean doDamageTint) {
        this(entityType, null, doDamageTint);
    }

    public ServerEntityModelLoader(EntityType<?> entityType, String model_name_override) {
        this(entityType, model_name_override, true);
    }

    public ServerEntityModelLoader(EntityType<?> entityType, String model_name_override, boolean doDamageTint) {
        this.entityType = entityType;
        this.doDamageTint = doDamageTint;
        if (model_name_override != null) {
            model_name_override = model_name_override.replace("..", "");
        }
        this.model_name_override = model_name_override;
        PolymerRPUtils.RESOURCE_PACK_CREATION_EVENT.register(this::loadRP);
    }

    public ServerEntityModelLoader forceMarker() {
        this.forceMarker = true;
        return this;
    }

    public ServerEntityModelLoader unforceMarker() {
        this.forceMarker = false;
        return this;
    }

    private void loadRP(PolymerRPBuilder builder) {
        Identifier id = Registry.ENTITY_TYPE.getId(entityType);
        String texture_loc_in = "assets/" + id.getNamespace() + "/server_entities/" + id.getPath() + "/" + id.getPath() + ".png";
        String texture_tint_loc_in = "assets/" + id.getNamespace() + "/server_entities/" + id.getPath() + "/" + id.getPath() + "_tint.png";
        String model_loc_in = "assets/" + id.getNamespace() + "/server_entities/" + id.getPath() + "/" + (this.model_name_override == null ? id.getPath() + ".bbmodel" : this.model_name_override);
        String texture_loc_out = "assets/" + id.getNamespace() + "/textures/entity/" + id.getPath() + ".png";
        String texture_tint_loc_out = "assets/" + id.getNamespace() + "/textures/entity/" + id.getPath() + "_tint.png";
        String model_loc_out = "assets/" + id.getNamespace() + "/models/entity/" + id.getPath() + "/";

        builder.copyModAssets(id.getNamespace());

        //copy texture
        builder.addData(texture_loc_out, builder.getData(texture_loc_in));
        if (this.doDamageTint) {
            builder.addData(texture_tint_loc_out, builder.getData(texture_tint_loc_in));
        }
        //load model
        String model_contents = new String(builder.getData(model_loc_in));
        this.unbakedModel = UnbakedServerEntityModel.deserialize(model_contents);
        this.bakedModel = this.unbakedModel.bake(forceMarker);
        ModelBox hitbox = this.bakedModel.base().getChild("hitbox").boxes[0];
        Vec3f hitboxSize = hitbox.to().copy();
        hitboxSize.subtract(hitbox.from());
        ServerMobsApiMod.LOGGER.info("Entity " + id + " hitbox:\n\tWidth: " + ((hitboxSize.getX() + hitboxSize.getZ()) / 32) + "\n\tHeight: " + hitboxSize.getY() / 16);
        createItemModel(builder, model_loc_out, this.bakedModel.base().getChild("base"));
    }

    private Vec3f sub(Vec3f a, Vec3f minus_b) {
        Vec3f tmp = a.copy();
        tmp.subtract(minus_b);
        return tmp;
    }

    private Vec3f mul(Vec3f a, int multiplier) {
        Vec3f tmp = a.copy();
        tmp.scale(multiplier);
        return tmp;
    }

    private Vec3f div(Vec3f a, double divisor) {
        Vec3f tmp = a.copy();
        tmp.scale((float) (1.0f / divisor));
        return tmp;
    }

    private Vec3f max(@Nullable Vec3f a, Vec3f b) {
        if (a == null)
            a = b;
        return new Vec3f(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
    }

    private Vec3f min(@Nullable Vec3f a, Vec3f b) {
        if (a == null)
            a = b;
        return new Vec3f(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
    }

    private void createItemModel(PolymerRPBuilder builder, String baseLoc, ModelGroup group) {
        Identifier id = Registry.ENTITY_TYPE.getId(entityType);
        baseLoc += group.name + "/";
        for (ModelGroup child : group.childGroups) {
            createItemModel(builder, baseLoc, child);
        }
        String model_file_name = baseLoc + "model.json";
        String tint_model_file_name = baseLoc + "model_tint.json";
        JsonObject model = new JsonObject();
        //parent
        // model.addProperty("parent", "minecraft:block/block");
        //credit
        model.addProperty("credit", "Autogenerated [" + model_file_name + "] by ServerMobs");
        //texture_size
        {
            JsonArray size_array = new JsonArray();
            size_array.add(this.bakedModel.texWidth());
            size_array.add(this.bakedModel.texHeight());
            model.add("texture_size", size_array);
        }
        //Center model around pivot, to disable, set to Vec3f.ZERO
        Vec3f origin = new Vec3f(group.transform.pivotX, group.transform.pivotY, group.transform.pivotZ);

        Vec3f minCoord = null;
        Vec3f maxCoord = null;

        //ServerMobsMod.LOGGER.info(id+", "+model_file_name+" boxes: ");
        for (ModelBox box : group.boxes) {
            Vec3f from = sub(box.from(), origin);
            Vec3f to = sub(box.to(), origin);
            minCoord = min(minCoord, min(from, to));
            maxCoord = max(maxCoord, max(from, to));
            //ServerMobsMod.LOGGER.info("    "+box.name()+", from: "+from+", to: "+to+", minCoord: "+minCoord+", maxCoord: "+maxCoord);


            if (!ScaleUtils.standSize(ScaleUtils.scaleAmount(minCoord, maxCoord)).valid) {
                ServerMobsApiMod.LOGGER.error("Item model for " + baseLoc + " is too big, try moving pivot point. (The model must not extend beyond -40 or 80 in all axes)");
            }
        }

        double extra_scaling;
        ScaleUtils.Scale stand_scale;
        // If there were no boxes
        if (minCoord == null || maxCoord == null) {
            extra_scaling = 1;
            stand_scale = ScaleUtils.Scale.SMALL;
            //ServerMobsMod.LOGGER.warn("Defaulting to small because no boxes were found for "+id+", "+model_file_name+", minCoord: "+minCoord+", maxCoord: "+maxCoord);
        } else {
            extra_scaling = ScaleUtils.scaleAmount(minCoord, maxCoord);
            stand_scale = ScaleUtils.standSize(extra_scaling);
        }
        //stand_scale = ScaleUtils.Scale.BIG; //debug for something?
        group.setArmorStandScale(stand_scale);

        /*if (divide != 1) {
            ServerMobsMod.LOGGER.warn("Shrank item model for "+baseLoc+" by "+divide);
        }*/

        //elements
        int numElements;
        {
            JsonArray elements = new JsonArray();
            for (ModelBox box : group.boxes) {
                JsonObject element = new JsonObject();
                element.addProperty("name", box.name());
                element.add("from", JsonUtil.toJsonArray(div(sub(box.from(), origin), extra_scaling)));
                element.add("to", JsonUtil.toJsonArray(div(sub(box.to(), origin), extra_scaling)));
                {
                    JsonObject faces = new JsonObject();
                    faces.add("north", createFace(box.northUV()));
                    faces.add("east", createFace(box.eastUV()));
                    faces.add("south", createFace(box.southUV()));
                    faces.add("west", createFace(box.westUV()));
                    faces.add("up", createFace(box.upUV()));
                    faces.add("down", createFace(box.downUV()));
                    element.add("faces", faces);
                }
                {
                    if (box.rotation_amt() != 0f) {
                        JsonObject rotation = new JsonObject();
                        rotation.add("origin", JsonUtil.toJsonArray(div(sub(box.rotation_origin(), origin), extra_scaling)));
                        rotation.addProperty("axis", box.rotation_axis().getName());
                        rotation.addProperty("angle", box.rotation_amt());
                        element.add("rotation", rotation);
                    }
                }
                elements.add(element);
            }
            model.add("elements", elements);
            numElements = elements.size();
        }
        //groups don't seem to do anything
        //groups
        {
            JsonArray groups = new JsonArray();
            {
                JsonObject mainGroup = new JsonObject();
                mainGroup.addProperty("name", "main");
                if (origin.equals(Vec3f.ZERO)) {
                    mainGroup.add("origin", JsonUtil.toJsonArray(Vec3f.ZERO));
                } else {
                    mainGroup.add("origin", JsonUtil.toJsonArray(div(origin, extra_scaling)));
                }
                {
                    JsonArray childrenArray = new JsonArray();
                    for (int i = 0; i < numElements; i++) {
                        childrenArray.add(i);
                    }
                    mainGroup.add("children", childrenArray);
                }
                groups.add(mainGroup);
            }
            model.add("groups", groups);
        }
        //display
        {
            JsonObject display = new JsonObject();
            //ServerMobsMod.LOGGER.warn("Loading display");
            {
                double s = ScaleUtils.getScaling(stand_scale) * extra_scaling;//(1.6/0.7); //the 0.7 is if using baby armor stands
                JsonObject head = new JsonObject();
                {
                    JsonArray translation = new JsonArray();
                    translation.add(8 * s);
                    translation.add((8 * s) - (4 * ScaleUtils.getScaling(stand_scale)));// - (4*s));
                    translation.add(8 * s);
                    head.add("translation", translation);
                }
                {
                    JsonArray scale = new JsonArray();
                    scale.add(s);//1.6 is inverse of 0.625 (see net.minecraft.client.render.entity.feature.HeadFeatureRenderer)
                    scale.add(s);
                    scale.add(s);
                    head.add("scale", scale);
                }
                display.add("head", head);
            }
            model.add("display", display);
        }
        JsonObject tint_model = model.deepCopy();
        //textures
        {
            JsonObject textures = new JsonObject();
            textures.addProperty("0", id.getNamespace() + ":entity/" + id.getPath());
            model.add("textures", textures);
        }
        if (this.doDamageTint) {
            JsonObject textures = new JsonObject();
            textures.addProperty("0", id.getNamespace() + ":entity/" + id.getPath() + "_tint");
            tint_model.add("textures", textures);
        }

        ///////////
        // BUILD //
        ///////////
        Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
        String model_string = GSON.toJson(model);
        builder.addData(model_file_name, model_string.getBytes(StandardCharsets.UTF_8));
        PolymerModelData polymerModelData = PolymerRPUtils.requestModel(
                Items.CARVED_PUMPKIN,
                new Identifier(
                        id.getNamespace(),
                        model_file_name
                                .replace("assets/" + id.getNamespace() + "/models/", "")
                                .replace(".json", "")
                )
        );
        group.setDisplayData(polymerModelData);

        if (this.doDamageTint) {
            String tint_model_string = GSON.toJson(tint_model);
            builder.addData(tint_model_file_name, tint_model_string.getBytes(StandardCharsets.UTF_8));
            PolymerModelData tintPolymerModelData = PolymerRPUtils.requestModel(
                    Items.CARVED_PUMPKIN,
                    new Identifier(
                            id.getNamespace(),
                            tint_model_file_name
                                    .replace("assets/" + id.getNamespace() + "/models/", "")
                                    .replace(".json", "")
                    )
            );
            group.setTintDisplayData(tintPolymerModelData);
        }
    }

    private JsonObject createFace(ModelUV uv) {
        JsonObject face = new JsonObject();
        face.add("uv", uv.toJsonArray(this.bakedModel.texWidth(), this.bakedModel.texHeight()));
        face.addProperty("rotation", uv.rotation());
        face.addProperty("texture", "#0");
        face.addProperty("tintindex", 0);
        return face;
    }

    private void logInstructions() {
        Identifier id = Registry.ENTITY_TYPE.getId(entityType);
        ServerMobsApiMod.LOGGER.warn("Missing proper datapack entry for entity " + id + ".");
        ServerMobsApiMod.LOGGER.warn("In your mod data, create the files:\n\t" + id.getPath() + ".bbmodel\n\t" + id.getPath() + ".png\nin the folder:\n\tdata/" + id.getNamespace() + "/entities/" + id.getPath() + "/");
    }

    public BakedServerEntityModel getBakedModel() {
        return bakedModel;
    }
}
