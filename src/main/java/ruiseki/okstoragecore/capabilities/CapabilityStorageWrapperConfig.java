package ruiseki.okstoragecore.capabilities;

import ruiseki.okcore.capabilities.Capability;
import ruiseki.okcore.capabilities.CapabilityInject;
import ruiseki.okcore.config.extendedconfig.CapabilityConfig;
import ruiseki.okstoragecore.OKStorageCore;
import ruiseki.okstoragecore.api.IStorageWrapper;

public class CapabilityStorageWrapperConfig extends CapabilityConfig<IStorageWrapper> {

    /**
     * The unique instance.
     */
    public static CapabilityStorageWrapperConfig _instance;

    @CapabilityInject(IStorageWrapper.class)
    public static Capability<IStorageWrapper> CAPABILITY = null;

    public CapabilityStorageWrapperConfig() {
        super(OKStorageCore._instance, true, "storageWrapper", null, IStorageWrapper.class);
    }
}
