package com.slimeist.server_mobs.api.server_rendering.entity;

import com.slimeist.server_mobs.api.server_rendering.model.BakedServerEntityModel;

public interface IServerRenderedEntity {
    BakedServerEntityModel.Instance createModelInstance();

    BakedServerEntityModel.Instance getModelInstance();

    BakedServerEntityModel getBakedModel();

    void updateAngles();

    default void initAngles() {
    }

    @SuppressWarnings("SameReturnValue")
    default String getNametagPath() {
        return "base";
    }
}
