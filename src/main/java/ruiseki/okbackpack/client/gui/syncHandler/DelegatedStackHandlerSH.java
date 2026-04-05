package ruiseki.okbackpack.client.gui.syncHandler;

import java.util.function.Supplier;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.utils.item.EmptyHandler;
import com.cleanroommc.modularui.utils.item.IItemHandler;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import ruiseki.okbackpack.api.wrapper.IAdvancedFilterable;
import ruiseki.okbackpack.api.wrapper.IBasicFilterable;
import ruiseki.okbackpack.api.wrapper.ISmeltingUpgrade;
import ruiseki.okbackpack.api.wrapper.IStorageUpgrade;
import ruiseki.okbackpack.client.gui.handler.DelegatedItemHandler;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapperBase;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapperFactory;

public class DelegatedStackHandlerSH extends SyncHandler {

    public static final int UPDATE_FILTERABLE = 0;
    public static final int UPDATE_ORE_DICT = 1;
    public static final int UPDATE_STORAGE = 2;
    public static final int UPDATE_FUEL_FILTER = 4;

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
    public void readOnClient(int id, PacketBuffer buf) {

    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        ItemStack stack = wrapper.getUpgradeHandler()
            .getStackInSlot(slotIndex);
        UpgradeWrapperBase wrapper = UpgradeWrapperFactory.createWrapper(stack, this.wrapper);

        switch (id) {
            case UPDATE_FILTERABLE:
                if (wrapper instanceof IBasicFilterable upgrade) {
                    setDelegatedStackHandler(upgrade::getFilterItems);
                }
                break;
            case UPDATE_ORE_DICT:
                if (wrapper instanceof IAdvancedFilterable upgrade) {
                    setDelegatedStackHandler(upgrade::getOreDictItem);
                }
                break;
            case UPDATE_STORAGE:
                if (wrapper instanceof IStorageUpgrade upgrade) {
                    setDelegatedStackHandler(upgrade::getStorage);
                }
                break;
            case UPDATE_FUEL_FILTER:
                if (wrapper instanceof ISmeltingUpgrade upgrade) {
                    setDelegatedStackHandler(upgrade::getFuelFilterItems);
                }
                break;
            default:
                return;
        }
    }
}
