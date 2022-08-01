package com.slimeist.server_mobs.server_rendering.entity;

import com.slimeist.server_mobs.server_rendering.model.BakedServerEntityModel;
import com.slimeist.server_mobs.server_rendering.model.UnbakedServerEntityModel;

public interface IServerRenderedEntity {
    BakedServerEntityModel.Instance createModelInstance();

    BakedServerEntityModel.Instance getModelInstance();

    BakedServerEntityModel getBakedModel();

    void updateAngles();

    default void initAngles() {};

    default String getNametagPath() {
        return "base";
    }
}
