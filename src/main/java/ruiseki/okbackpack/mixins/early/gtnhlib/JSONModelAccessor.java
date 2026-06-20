package ruiseki.okbackpack.mixins.early.gtnhlib;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.gtnewhorizon.gtnhlib.client.model.loading.ModelDeserializer.ModelElement;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelDeserializer.Position;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelDeserializer.Position.ModelDisplay;
import com.gtnewhorizon.gtnhlib.client.model.loading.ResourceLoc;
import com.gtnewhorizon.gtnhlib.client.model.unbaked.JSONModel;

@Mixin(JSONModel.class)
public interface JSONModelAccessor {

    @Accessor(value = "parentId", remap = false)
    ResourceLoc.ModelLoc getParentId();

    @Accessor(value = "useAO", remap = false)
    boolean isUseAO();

    @Accessor(value = "display", remap = false)
    Map<Position, ModelDisplay> getDisplay();

    @Accessor(value = "elements", remap = false)
    List<ModelElement> getElements();
}
