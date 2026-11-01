package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.Supplier;

public class DelegatedEnum<T> implements Supplier<T> {

    private Supplier<T> delegated;

    public DelegatedEnum(Supplier<T> delegated) {
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
