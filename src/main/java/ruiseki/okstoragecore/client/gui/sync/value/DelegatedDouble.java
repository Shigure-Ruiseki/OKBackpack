package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class DelegatedDouble implements DoubleSupplier, DoubleConsumer {

    private DoubleSupplier getter;
    private DoubleConsumer setter;

    public DelegatedDouble(DoubleSupplier getter, DoubleConsumer setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public void setDelegated(DoubleSupplier getter, DoubleConsumer setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public DoubleSupplier get() {
        return getter;
    }

    @Override
    public double getAsDouble() {
        return getter.getAsDouble();
    }

    @Override
    public void accept(double value) {
        setter.accept(value);
    }
}
