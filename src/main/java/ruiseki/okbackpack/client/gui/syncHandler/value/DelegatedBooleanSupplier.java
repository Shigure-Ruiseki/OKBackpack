package ruiseki.okbackpack.client.gui.syncHandler.value;

import java.util.function.BooleanSupplier;

public class DelegatedBooleanSupplier implements BooleanSupplier {

    private BooleanSupplier delegated;

    public DelegatedBooleanSupplier(BooleanSupplier delegated) {
        this.delegated = delegated;
    }

    public void setDelegated(BooleanSupplier delegated) {
        this.delegated = delegated;
    }

    public BooleanSupplier get() {
        return delegated;
    }

    @Override
    public boolean getAsBoolean() {
        return get().getAsBoolean();
    }
}
