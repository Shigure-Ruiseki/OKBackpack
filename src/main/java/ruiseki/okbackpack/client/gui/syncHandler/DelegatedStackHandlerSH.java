package ruiseki.okbackpack.client.gui.syncHandler;

import java.io.IOException;
import java.util.function.Supplier;

import net.minecraft.inventory.IInventory;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.utils.item.EmptyHandler;
import com.cleanroommc.modularui.utils.item.IItemHandler;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import ruiseki.okbackpack.api.IStorageContainer;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.DelegatedStackHandlerSHRegistry;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.handler.DelegatedItemHandler;

public class DelegatedStackHandlerSH extends SyncHandler {

    public final Supplier<IStorageContainer<?>> containerProvider;
    public final IStorageWrapper wrapper;
    public final int slotIndex;
    public final int wrappedSlotAmount;

    public DelegatedItemHandler delegatedStackHandler;
    public IInventory inventory;

    public DelegatedStackHandlerSH(Supplier<IStorageContainer<?>> containerProvider, IStorageWrapper wrapper,
        int slotIndex, int wrappedSlotAmount) {
        this.wrapper = wrapper;
        this.slotIndex = slotIndex;
        this.wrappedSlotAmount = wrappedSlotAmount;
        this.containerProvider = containerProvider;

        this.delegatedStackHandler = new DelegatedItemHandler(() -> EmptyHandler.INSTANCE, this.wrappedSlotAmount);
    }

    public void setDelegatedStackHandler(Supplier<IItemHandler> delegated) {
        this.delegatedStackHandler.setDelegated(delegated);
    }

    @SuppressWarnings("unchecked")
    public <T> T getInventory() {
        return (T) inventory;
    }

    public void setInventory(IInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (!DelegatedStackHandlerSHRegistry.isClientEmpty()) {
            try {
                DelegatedStackHandlerSHRegistry.handleClient(this, id, buf);
                this.wrapper.markDirty();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (!DelegatedStackHandlerSHRegistry.isServerEmpty()) {
            try {
                DelegatedStackHandlerSHRegistry.handleServer(this, id, buf);
                this.wrapper.markDirty();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public IUpgradeWrapper getWrapper() {
        return this.wrapper.getUpgradeHandler()
            .getWrapperInSlot(slotIndex);
    }

    public static int getId(String name) {
        return DelegatedStackHandlerSHRegistry.getId(name);
    }
}
