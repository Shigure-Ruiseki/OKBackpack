package ruiseki.storagecore.client.gui.sync;

import net.minecraft.util.ResourceLocation;

import ruiseki.okbackpack.Reference;
import ruiseki.storagecore.upgrades.IUpgradeWrapper;

public class UpgradeSlotSHRegistries {

    public static final StorageSHRegistry.Entry<UpgradeSlotSH> REGISTRY = StorageSHRegistry.get(UpgradeSlotSH.class);

    public static final ResourceLocation ENABLE = new ResourceLocation(Reference.MOD_ID, "storage_enable");

    public static void load() {

        REGISTRY.registerServer(ENABLE, (slot, buf) -> {
            boolean enable = buf.readBoolean();
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            wrapper.setEnabled(enable);
        });
    }
}
