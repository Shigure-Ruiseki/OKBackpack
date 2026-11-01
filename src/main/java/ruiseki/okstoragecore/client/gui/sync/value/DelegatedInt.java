package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.IntSupplier;

public class DelegatedInt implements IntSupplier {

    private IntSupplier delegated;

    public DelegatedInt(IntSupplier delegated) {
        this.delegated = delegated;
    }

    public void setDelegated(IntSupplier delegated) {
        this.delegated = delegated;
    }

    public IntSupplier get() {
        return delegated;
    }

    @Override
    public int getAsInt() {
        return get().getAsInt();
    }
}
