package ruiseki.okstoragecore.client.gui.sync;

import net.minecraft.util.ResourceLocation;

import ruiseki.okstoragecore.Reference;
import ruiseki.okstoragecore.upgrades.IFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;

public class DelegatedFilterLogicSHRegisters {

    public static final StorageSHRegistry.Entry<DelegatedFilterLogicSH> REGISTRY = StorageSHRegistry
        .get(DelegatedFilterLogicSH.class);

    public static final ResourceLocation FILTER_LOGIC = new ResourceLocation(Reference.MOD_ID, "storage_filter_logic");

    public static void load() {
        REGISTRY.registerServer(FILTER_LOGIC, (handler, buf) -> {
            IUpgradeWrapper wrapper = handler.getUpgradeWrapper();
            if (!(wrapper instanceof IFilteredUpgrade upgrade)) return;
            handler.setFilterLogic(upgrade::getFilterLogic);
        });
    }
}
