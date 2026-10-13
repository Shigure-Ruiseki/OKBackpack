package ruiseki.backpack;

import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.okcore.capabilities.Capability;
import ruiseki.okcore.capabilities.CapabilityInject;
import ruiseki.okcore.capabilities.CapabilityManager;

public class Capabilities {

    @CapabilityInject(IBackpackWrapper.class)
    public static Capability<IBackpackWrapper> BACKPACK_WRAPPER = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(IBackpackWrapper.class);
    }
}
