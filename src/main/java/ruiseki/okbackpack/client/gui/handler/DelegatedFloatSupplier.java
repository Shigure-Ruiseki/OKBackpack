package ruiseki.okbackpack.client.gui.handler;

import java.util.function.Supplier;

import com.cleanroommc.modularui.utils.FloatSupplier;

public class DelegatedFloatSupplier implements FloatSupplier {

    private Supplier<FloatSupplier> delegated;

    public DelegatedFloatSupplier(Supplier<FloatSupplier> delegated) {
        this.delegated = delegated;
    }

    public void setDelegated(Supplier<FloatSupplier> delegated) {
        this.delegated = delegated;
    }

    @Override
    public float getAsFloat() {
        return get().getAsFloat();
    }

    public FloatSupplier get() {
        return delegated.get();
    }
}
