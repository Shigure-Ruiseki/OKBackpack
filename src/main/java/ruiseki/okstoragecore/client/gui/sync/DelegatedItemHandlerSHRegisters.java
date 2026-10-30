package ruiseki.okstoragecore.client.gui.sync;

import net.minecraft.util.ResourceLocation;

import ruiseki.okstoragecore.Reference;
import ruiseki.okstoragecore.upgrades.IStorageUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;

public class DelegatedItemHandlerSHRegisters {

    public static final StorageSHRegistry.Entry<DelegatedItemHandlerSH> REGISTRY = StorageSHRegistry
        .get(DelegatedItemHandlerSH.class);

    public static final ResourceLocation STORAGE = new ResourceLocation(Reference.MOD_ID, "storage_storage");

    public static void load() {
        REGISTRY.registerServer(STORAGE, (handler, buf) -> {
            IUpgradeWrapper wrapper = handler.getUpgradeWrapper();
            if (!(wrapper instanceof IStorageUpgrade upgrade)) return;
            handler.setItemHandler(upgrade::getStorage);
        });
    }
}
