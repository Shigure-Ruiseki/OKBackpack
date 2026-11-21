package ruiseki.okstoragecore.client.gui.sync.value;

import com.cleanroommc.modularui.utils.FloatConsumer;
import com.cleanroommc.modularui.utils.FloatSupplier;

public class DelegatedFloat implements FloatSupplier, FloatConsumer {

    private FloatSupplier getter;
    private FloatConsumer setter;

    public DelegatedFloat(FloatSupplier getter, FloatConsumer setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public void setDelegated(FloatSupplier getter, FloatConsumer setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public FloatSupplier get() {
        return getter;
    }

    @Override
    public float getAsFloat() {
        return getter.getAsFloat();
    }

    @Override
    public void accept(float value) {
        setter.accept(value);
    }
}
