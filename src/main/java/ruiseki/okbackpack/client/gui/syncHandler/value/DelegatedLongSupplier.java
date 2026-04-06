package ruiseki.okbackpack.client.gui.syncHandler.value;

import java.util.function.LongSupplier;

public class DelegatedLongSupplier implements LongSupplier {

    private LongSupplier delegated;

    public DelegatedLongSupplier(LongSupplier delegated) {
        this.delegated = delegated;
    }

    public void setDelegated(LongSupplier delegated) {
        this.delegated = delegated;
    }

    public LongSupplier get() {
        return delegated;
    }

    @Override
    public long getAsLong() {
        return get().getAsLong();
    }
}
