package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.BooleanSupplier;

import com.cleanroommc.modularui.utils.BooleanConsumer;

public class DelegatedBoolean implements BooleanSupplier, BooleanConsumer {

    private BooleanSupplier getter;
    private BooleanConsumer setter;

    public DelegatedBoolean(BooleanSupplier getter, BooleanConsumer setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public void setDelegated(BooleanSupplier getter, BooleanConsumer setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public BooleanSupplier get() {
        return getter;
    }

    @Override
    public boolean getAsBoolean() {
        return getter.getAsBoolean();
    }

    @Override
    public void accept(boolean value) {
        setter.accept(value);
    }
}
