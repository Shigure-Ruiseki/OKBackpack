package ruiseki.okbackpack.client.gui.syncHandler.value;

import java.util.function.DoubleSupplier;

public class DelegatedDoubleSupplier implements DoubleSupplier {

    private DoubleSupplier delegated;

    public DelegatedDoubleSupplier(DoubleSupplier delegated) {
        this.delegated = delegated;
    }

    public void setDelegated(DoubleSupplier delegated) {
        this.delegated = delegated;
    }

    public DoubleSupplier get() {
        return delegated;
    }

    @Override
    public double getAsDouble() {
        return get().getAsDouble();
    }
}
