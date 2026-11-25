package ruiseki.okbackpack.client.gui.syncHandler.value;

import java.util.function.IntSupplier;

public class DelegatedIntSupplier implements IntSupplier {

    private IntSupplier delegated;

    public DelegatedIntSupplier(IntSupplier delegated) {
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
