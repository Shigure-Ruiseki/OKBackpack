package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.Supplier;

public class DelegatedString implements Supplier<String> {

    private Supplier<String> delegated;

    public DelegatedString(Supplier<String> delegated) {
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
