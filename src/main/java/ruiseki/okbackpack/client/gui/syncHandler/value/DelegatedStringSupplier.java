package ruiseki.okbackpack.client.gui.syncHandler.value;

import java.util.function.Supplier;

public class DelegatedStringSupplier implements Supplier<String> {

    private Supplier<String> delegated;

    public DelegatedStringSupplier(Supplier<String> delegated) {
        this.delegated = delegated;
    }

    public void setDelegated(Supplier<String> delegated) {
        this.delegated = delegated;
    }

    @Override
    public String get() {
        return delegated.get();
    }
}
