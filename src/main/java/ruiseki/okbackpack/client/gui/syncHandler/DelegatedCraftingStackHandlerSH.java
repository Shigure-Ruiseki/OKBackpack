package ruiseki.okbackpack.client.gui.syncHandler;

import java.io.IOException;
import java.util.function.Supplier;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.utils.item.EmptyHandler;
import com.cleanroommc.modularui.utils.item.IItemHandler;

import ruiseki.okbackpack.api.IStorageContainer;
import ruiseki.okbackpack.api.wrapper.IAdvancedFilterable;
import ruiseki.okbackpack.api.wrapper.IBasicFilterable;
import ruiseki.okbackpack.api.wrapper.ICraftingUpgrade;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.handler.IndexedInventoryCraftingWrapper;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.item.crafting.CraftingUpgradeWrapper;

public class DelegatedCraftingStackHandlerSH extends DelegatedStackHandlerSH {

    public static final int UPDATE_CRAFTING = 3;
    public static final int DETECT_CHANGES = 4;

    private final Supplier<IStorageContainer<?>> containerProvider;
    private final BackpackWrapper wrapper;
    private final int slotIndex;

    private IndexedInventoryCraftingWrapper inventoryCrafting;

    public DelegatedCraftingStackHandlerSH(Supplier<IStorageContainer<?>> containerProvider, BackpackWrapper wrapper,
        int slotIndex, int wrappedSlotAmount) {

        super(wrapper, slotIndex, wrappedSlotAmount);

        this.containerProvider = containerProvider;
        this.wrapper = wrapper;
        this.slotIndex = slotIndex;
    }

    @Override
    public void setDelegatedStackHandler(Supplier<IItemHandler> delegated) {
        super.setDelegatedStackHandler(delegated);
        updateInventoryCrafting();
    }

    public void updateInventoryCrafting() {

        if (inventoryCrafting == null) {

            inventoryCrafting = new IndexedInventoryCraftingWrapper(
                slotIndex,
                containerProvider.get()
                    .getContainer(),
                3,
                3,
                delegatedStackHandler,
                0);

            containerProvider.get()
                .registerInventoryCrafting(slotIndex, inventoryCrafting);
        }

        IUpgradeWrapper wrapper = this.wrapper.getUpgradeHandler()
            .getWrapperInSlot(slotIndex);
        if (!(wrapper instanceof ICraftingUpgrade craftingWrapper)) {
            return;
        }

        inventoryCrafting.setCraftingDestination(craftingWrapper.getCraftingDes());

        inventoryCrafting.detectChanges();

        if (isValid() && !getSyncManager().isClient() && !(delegatedStackHandler.get() instanceof EmptyHandler)) {
            int resultSlot = inventoryCrafting.getSizeInventory() - 1;

            ItemStack result = delegatedStackHandler.get()
                .getStackInSlot(resultSlot);

            syncToClient(UPDATE_CRAFTING, buffer -> buffer.writeItemStackToBuffer(result));
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {

        if (id == UPDATE_CRAFTING) {
            IUpgradeWrapper wrapper = this.wrapper.getUpgradeHandler()
                .getWrapperInSlot(slotIndex);
            if (!(wrapper instanceof CraftingUpgradeWrapper craftingWrapper)) return;

            try {
                craftingWrapper.getStorage()
                    .setStackInSlot(9, buf.readItemStackFromBuffer());
            } catch (IOException ignored) {}
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        IUpgradeWrapper wrapper = this.wrapper.getUpgradeHandler()
            .getWrapperInSlot(slotIndex);
        switch (id) {

            case UPDATE_FILTERABLE: {
                if (wrapper instanceof IBasicFilterable upgrade) {
                    setDelegatedStackHandler(upgrade::getFilterItems);
                }
                break;
            }

            case UPDATE_ORE_DICT:
                if (wrapper instanceof IAdvancedFilterable upgrade) {
                    setDelegatedStackHandler(upgrade::getOreDictItem);
                }
                break;

            case UPDATE_CRAFTING: {
                if (wrapper instanceof ICraftingUpgrade upgrade) {
                    setDelegatedStackHandler(upgrade::getStorage);
                }
                break;
            }

            case DETECT_CHANGES: {

                if (inventoryCrafting != null) {
                    inventoryCrafting.detectChanges();

                    if (!(delegatedStackHandler.get() instanceof EmptyHandler)) {
                        int resultSlot = inventoryCrafting.getSizeInventory() - 1;

                        ItemStack result = delegatedStackHandler.get()
                            .getStackInSlot(resultSlot);

                        syncToClient(UPDATE_CRAFTING, buffer -> buffer.writeItemStackToBuffer(result));
                    }
                }

                break;
            }
            default:
                return;
        }
    }
}
