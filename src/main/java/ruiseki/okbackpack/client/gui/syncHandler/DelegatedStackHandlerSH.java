package ruiseki.okbackpack.client.gui.syncHandler;

import java.util.function.Supplier;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.utils.item.EmptyHandler;
import com.cleanroommc.modularui.utils.item.IItemHandler;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import ruiseki.okbackpack.client.gui.handler.DelegatedItemHandler;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.item.wrapper.IBasicFilterable;
import ruiseki.okbackpack.common.item.wrapper.IStorageUpgrade;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapper;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapperFactory;

public class DelegatedStackHandlerSH extends SyncHandler {

    public static final int UPDATE_FILTERABLE = 0;
    public static final int UPDATE_STORAGE = 1;

    private final BackpackWrapper wrapper;
    private final int slotIndex;
    private final int wrappedSlotAmount;

    public DelegatedItemHandler delegatedStackHandler;

    public DelegatedStackHandlerSH(BackpackWrapper wrapper, int slotIndex, int wrappedSlotAmount) {
        this.wrapper = wrapper;
        this.slotIndex = slotIndex;
        this.wrappedSlotAmount = wrappedSlotAmount;

        this.delegatedStackHandler = new DelegatedItemHandler(() -> EmptyHandler.INSTANCE, this.wrappedSlotAmount);
    }

    public void setDelegatedStackHandler(Supplier<IItemHandler> delegated) {
        delegatedStackHandler.setDelegated(delegated);
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {}

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        ItemStack stack = wrapper.getUpgradeHandler()
            .getStackInSlot(slotIndex);
        if (id == UPDATE_FILTERABLE) {
            UpgradeWrapper wrapper = UpgradeWrapperFactory.createWrapper(stack);
            if (wrapper instanceof IBasicFilterable upgrade) {
                setDelegatedStackHandler(upgrade::getFilterItems);
            }
        }
        if (id == UPDATE_STORAGE) {
            UpgradeWrapper wrapper = UpgradeWrapperFactory.createWrapper(stack);
            if (wrapper instanceof IStorageUpgrade upgrade) {
                setDelegatedStackHandler(upgrade::getStorage);
            }
        }
        wrapper.writeToItem();
    }
}
