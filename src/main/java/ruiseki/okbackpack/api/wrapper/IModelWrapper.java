package ruiseki.okbackpack.api.wrapper;

import com.gtnewhorizon.gtnhlib.client.model.BakedModelQuadContext;
import com.gtnewhorizon.gtnhlib.client.model.loading.ResourceLoc;

import ruiseki.okbackpack.api.BackpackPart;

public interface IModelWrapper {

    ResourceLoc.ModelLoc getModelLoc(BakedModelQuadContext context);

    BackpackPart getBackpackPart(BakedModelQuadContext context);
}
