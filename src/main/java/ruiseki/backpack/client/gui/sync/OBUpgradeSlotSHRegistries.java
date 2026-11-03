package ruiseki.backpack.client.gui.sync;

import net.minecraft.util.ResourceLocation;

import ruiseki.backpack.upgrades.inception.InceptionUpgradeWrapper;
import ruiseki.backpack.upgrades.inception.InventoryOrder;
import ruiseki.okbackpack.Reference;
import ruiseki.okstoragecore.client.gui.sync.StorageSHRegistry;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSH;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;

public class OBUpgradeSlotSHRegistries {

    public static final StorageSHRegistry.Entry<UpgradeSlotSH> REGISTRY = StorageSHRegistry.get(UpgradeSlotSH.class);

    public static final ResourceLocation INVENTORY_ORDER = new ResourceLocation(
        Reference.MOD_ID,
        "backpack_inventory_order");

    public static void load() {
        REGISTRY.registerServer(INVENTORY_ORDER, (slot, buf) -> {
            IUpgradeWrapper wrapper = slot.getUpgradeWrapper();
            if (!(wrapper instanceof InceptionUpgradeWrapper upgradeWrapper)) return;
            upgradeWrapper.setInventoryOrder(InventoryOrder.fromOrdinal(buf.readInt()));
        });
    }
}
