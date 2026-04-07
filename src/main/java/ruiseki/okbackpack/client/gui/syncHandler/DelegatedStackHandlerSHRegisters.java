package ruiseki.okbackpack.client.gui.syncHandler;

import java.io.IOException;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.utils.item.EmptyHandler;

import ruiseki.okbackpack.api.upgrade.DelegatedStackHandlerSHRegistry;
import ruiseki.okbackpack.api.wrapper.IAdvancedFilterable;
import ruiseki.okbackpack.api.wrapper.IBasicFilterable;
import ruiseki.okbackpack.api.wrapper.ISmeltingUpgrade;
import ruiseki.okbackpack.api.wrapper.IStorageUpgrade;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.handler.IndexedInventoryCraftingWrapper;
import ruiseki.okcore.init.IInitListener;

public class DelegatedStackHandlerSHRegisters implements IInitListener {

    public static final String UPDATE_FILTERABLE = "update_filter";
    public static final String UPDATE_ORE_DICT = "update_ore_dict";
    public static final String UPDATE_STORAGE = "update_storage";
    public static final String UPDATE_FUEL_FILTER = "update_fuel_filter";
    public static final String UPDATE_CRAFTING = "update_crafting";
    public static final String UPDATE_CRAFTING_CHANGES = "update_crafting_changes";

    @Override
    public void onInit(IInitListener.Step step) {
        if (step == IInitListener.Step.POSTINIT) {

            DelegatedStackHandlerSHRegistry.registerServer(UPDATE_FILTERABLE, (handler, buf) -> {
                IUpgradeWrapper wrapper = handler.getWrapper();
                if (wrapper instanceof IBasicFilterable upgrade) {
                    handler.setDelegatedStackHandler(upgrade::getFilterItems);
                }
            });

            DelegatedStackHandlerSHRegistry.registerServer(UPDATE_ORE_DICT, (handler, buf) -> {
                IUpgradeWrapper wrapper = handler.getWrapper();
                if (!(wrapper instanceof IAdvancedFilterable upgrade)) return;
                handler.setDelegatedStackHandler(upgrade::getOreDictItem);
            });

            DelegatedStackHandlerSHRegistry.registerServer(UPDATE_STORAGE, (handler, buf) -> {
                IUpgradeWrapper wrapper = handler.getWrapper();
                if (!(wrapper instanceof IStorageUpgrade upgrade)) return;
                handler.setDelegatedStackHandler(upgrade::getStorage);
            });

            DelegatedStackHandlerSHRegistry.registerServer(UPDATE_FUEL_FILTER, (handler, buf) -> {
                IUpgradeWrapper wrapper = handler.getWrapper();
                if (!(wrapper instanceof ISmeltingUpgrade upgrade)) return;
                handler.setDelegatedStackHandler(upgrade::getFuelFilterItems);
            });

            DelegatedStackHandlerSHRegistry.registerServer(UPDATE_CRAFTING, (handler, buf) -> {
                IUpgradeWrapper wrapper = handler.getWrapper();
                if (!(wrapper instanceof IStorageUpgrade upgrade)) return;
                handler.setDelegatedStackHandler(upgrade::getStorage);
            });

            DelegatedStackHandlerSHRegistry.registerClient(UPDATE_CRAFTING, (handler, buf) -> {
                IUpgradeWrapper wrapper = handler.getWrapper();
                if (!(wrapper instanceof IStorageUpgrade upgrade)) return;
                try {
                    upgrade.getStorage()
                        .setStackInSlot(9, buf.readItemStackFromBuffer());
                } catch (IOException ignored) {}
            });

            DelegatedStackHandlerSHRegistry.registerServer(UPDATE_CRAFTING_CHANGES, (handler, buf) -> {
                if (!(handler.getInventory() instanceof IndexedInventoryCraftingWrapper inventoryCrafting)) return;
                if (handler.getInventory() != null) {
                    inventoryCrafting.detectChanges();

                    if (!(handler.delegatedStackHandler.get() instanceof EmptyHandler)) {
                        int resultSlot = inventoryCrafting.getSizeInventory() - 1;

                        ItemStack result = handler.delegatedStackHandler.get()
                            .getStackInSlot(resultSlot);

                        handler.syncToClient(
                            DelegatedStackHandlerSH.getId(UPDATE_CRAFTING),
                            buffer -> buffer.writeItemStackToBuffer(result));
                    }
                }

            });
        }
    }
}
