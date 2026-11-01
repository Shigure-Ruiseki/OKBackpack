package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.DoubleSupplier;

public class DelegatedDouble implements DoubleSupplier {

    private DoubleSupplier delegated;

    public DelegatedDouble(DoubleSupplier delegated) {
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
