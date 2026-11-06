package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DelegatedEnum<T> implements Supplier<T>, Consumer<T> {

    private Supplier<T> getter;
    private Consumer<T> setter;

    public DelegatedEnum(Supplier<T> getter, Consumer<T> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public void setDelegated(Supplier<T> getter, Consumer<T> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public T get() {
        return getter.get();
    }

    @Override
    public void accept(T value) {
        setter.accept(value);
    }
}
