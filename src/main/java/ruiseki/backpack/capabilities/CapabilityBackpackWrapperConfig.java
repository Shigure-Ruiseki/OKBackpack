package ruiseki.backpack.capabilities;

import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.okcore.capabilities.Capability;
import ruiseki.okcore.capabilities.CapabilityInject;
import ruiseki.okcore.config.extendedconfig.CapabilityConfig;
import ruiseki.okstoragecore.OKStorageCore;

public class CapabilityBackpackWrapperConfig extends CapabilityConfig<IBackpackWrapper> {

    /**
     * The unique instance.
     */
    public static CapabilityBackpackWrapperConfig _instance;

    @CapabilityInject(IBackpackWrapper.class)
    public static Capability<IBackpackWrapper> CAPABILITY = null;

    public CapabilityBackpackWrapperConfig() {
        super(OKStorageCore._instance, true, "backpackWrapper", null, IBackpackWrapper.class);
    }
}
