package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DelegatedString implements Supplier<String>, Consumer<String> {

    private Supplier<String> getter;
    private Consumer<String> setter;

    public DelegatedString(Supplier<String> getter, Consumer<String> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public void setDelegated(Supplier<String> getter, Consumer<String> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public String get() {
        return getter.get();
    }

    @Override
    public void accept(String value) {
        setter.accept(value);
    }

    public void set(String value) {
        accept(value);
    }
}
