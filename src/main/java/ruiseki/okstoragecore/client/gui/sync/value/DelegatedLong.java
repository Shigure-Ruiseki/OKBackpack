package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.LongSupplier;

public class DelegatedLong implements LongSupplier {

    private LongSupplier delegated;

    public DelegatedLong(LongSupplier delegated) {
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
