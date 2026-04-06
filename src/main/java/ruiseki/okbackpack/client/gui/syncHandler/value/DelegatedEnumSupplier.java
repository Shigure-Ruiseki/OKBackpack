package ruiseki.okbackpack.client.gui.syncHandler.value;

import java.util.function.Supplier;

public class DelegatedEnumSupplier<T> implements Supplier<T> {

    private Supplier<T> delegated;

    public DelegatedEnumSupplier(Supplier<T> delegated) {
        this.delegated = delegated;
    }

    public void setDelegated(Supplier<T> delegated) {
        this.delegated = delegated;
    }

    @Override
    public T get() {
        return delegated.get();
    }
}
