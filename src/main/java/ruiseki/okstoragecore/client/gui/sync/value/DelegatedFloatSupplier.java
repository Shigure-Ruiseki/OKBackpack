package ruiseki.okstoragecore.client.gui.sync.value;

import com.cleanroommc.modularui.utils.FloatSupplier;

public class DelegatedFloatSupplier implements FloatSupplier {

    private FloatSupplier delegated;

    public DelegatedFloatSupplier(FloatSupplier delegated) {
        this.delegated = delegated;
    }

    public void setDelegated(FloatSupplier delegated) {
        this.delegated = delegated;
    }

    @Override
    public float getAsFloat() {
        return get().getAsFloat();
    }

    public FloatSupplier get() {
        return delegated;
    }
}
