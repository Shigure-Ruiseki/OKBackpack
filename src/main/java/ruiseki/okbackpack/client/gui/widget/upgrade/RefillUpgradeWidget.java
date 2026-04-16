package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.common.item.refill.RefillUpgradeWrapper;

public class RefillUpgradeWidget extends ExpandedUpgradeTabWidget<RefillUpgradeWrapper> {

    protected final RefillUpgradeWrapper wrapper;
    protected final List<ItemSlot> filterSlots;
    protected final String filterSyncKey;
    protected final int slotsPerRow;

    public RefillUpgradeWidget(int slotIndex, RefillUpgradeWrapper wrapper, ItemStack delegatedIconStack,
        IStoragePanel<?> panel, String titleKey) {
        this(slotIndex, wrapper, delegatedIconStack, panel, titleKey, "refill_filter", 4, 3, 90);
    }

    protected RefillUpgradeWidget(int slotIndex, RefillUpgradeWrapper wrapper, ItemStack delegatedIconStack,
        IStoragePanel<?> panel, String titleKey, String filterSyncKey, int slotsPerRow, int coveredTabSize, int width) {
        super(slotIndex, coveredTabSize, delegatedIconStack, panel, titleKey, width);

        this.wrapper = wrapper;
        this.filterSyncKey = filterSyncKey;
        this.slotsPerRow = slotsPerRow;
        this.filterSlots = new ArrayList<>();

        SlotGroupWidget slotGroup = new SlotGroupWidget().name(filterSyncKey + "s");
        slotGroup.coverChildren();

        int slotCount = wrapper.getFilterSlotCount();
        for (int i = 0; i < slotCount; i++) {
            ItemSlot slot = ItemSlot.create(true);
            slot.name(filterSyncKey + "_" + slotIndex)
                .syncHandler(filterSyncKey + "_" + slotIndex, i)
                .pos(i % slotsPerRow * 18, i / slotsPerRow * 18);

            this.filterSlots.add(slot);
            slotGroup.child(slot);
        }

        Flow column = Flow.column()
            .pos(8, 32)
            .coverChildren()
            .childPadding(2)
            .child(slotGroup);

        child(column);
    }

    @Override
    protected RefillUpgradeWrapper getWrapper() {
        return wrapper;
    }
}
