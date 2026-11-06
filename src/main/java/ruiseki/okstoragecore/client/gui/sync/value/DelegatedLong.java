package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

public class DelegatedLong implements LongSupplier, LongConsumer {

    private LongSupplier getter;
    private LongConsumer setter;

    public DelegatedLong(LongSupplier getter, LongConsumer setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public void setDelegated(LongSupplier getter, LongConsumer setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public LongSupplier get() {
        return getter;
    }

    @Override
    public long getAsLong() {
        return getter.getAsLong();
    }

    @Override
    public void accept(long value) {
        setter.accept(value);
    }
}
