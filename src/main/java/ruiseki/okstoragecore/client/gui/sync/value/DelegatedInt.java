package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class DelegatedInt implements IntSupplier, IntConsumer {

    private IntSupplier getter;
    private IntConsumer setter;

    public DelegatedInt(IntSupplier getter, IntConsumer setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public void setDelegated(IntSupplier getter, IntConsumer setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public IntSupplier get() {
        return getter;
    }

    @Override
    public int getAsInt() {
        return getter.getAsInt();
    }

    @Override
    public void accept(int value) {
        setter.accept(value);
    }
}
