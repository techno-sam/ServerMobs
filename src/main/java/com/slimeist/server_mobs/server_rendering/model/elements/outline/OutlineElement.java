package com.slimeist.server_mobs.server_rendering.model.elements.outline;

import com.slimeist.server_mobs.server_rendering.model.UnbakedServerEntityModel;
import com.slimeist.server_mobs.server_rendering.model.elements.IBakedModelPart;

public interface OutlineElement {
    String getUUID();

    IBakedModelPart bake(UnbakedServerEntityModel unbakedServerEntityModel);
}
