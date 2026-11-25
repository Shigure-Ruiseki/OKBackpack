package ruiseki.okbackpack.api.wrapper;

import java.util.List;
import java.util.Map;

import com.gtnewhorizon.gtnhlib.client.model.BakedModelQuadContext;
import com.gtnewhorizon.gtnhlib.client.model.loading.ResourceLoc;

import ruiseki.okbackpack.api.BackpackPart;

public interface IModelUpgrade {

    Map<BackpackPart, List<ResourceLoc.ModelLoc>> geModels(BakedModelQuadContext context);
}
