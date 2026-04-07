package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.client.gui.slot.RefillPhantomSlot;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.common.item.refill.AdvancedRefillUpgradeWrapper;
import ruiseki.okbackpack.common.item.refill.TargetSlot;

public class AdvancedRefillUpgradeWidget extends ExpandedUpgradeTabWidget<AdvancedRefillUpgradeWrapper> {

    private static final int SLOTS_PER_ROW = 4;

    private final AdvancedRefillUpgradeWrapper wrapper;
    private final List<RefillPhantomSlot> filterSlots;
    private final String filterSyncKey;

    // Scroll tracking state
    private int slotBeingChanged = -1;
    private TargetSlot targetSlotBeingChanged = null;

    public AdvancedRefillUpgradeWidget(int slotIndex, AdvancedRefillUpgradeWrapper wrapper,
        ItemStack delegatedIconStack, IStoragePanel<?> panel, String titleKey) {
        super(slotIndex, 6, delegatedIconStack, titleKey, 90);

        this.wrapper = wrapper;
        this.filterSyncKey = "adv_refill_filter";
        this.filterSlots = new ArrayList<>();

        SlotGroupWidget slotGroup = new SlotGroupWidget().name(filterSyncKey + "s");
        slotGroup.coverChildren();

        int slotCount = wrapper.getFilterSlotCount();
        for (int i = 0; i < slotCount; i++) {
            final int slotIdx = i;
            RefillPhantomSlot slot = new RefillPhantomSlot(() -> {
                if (slotBeingChanged == slotIdx && targetSlotBeingChanged != null) {
                    return targetSlotBeingChanged;
                }
                return wrapper.getTargetSlot(slotIdx);
            }, scrollDir -> handleScroll(slotIdx, scrollDir));
            slot.name(filterSyncKey + "_" + slotIndex)
                .syncHandler(filterSyncKey + "_" + slotIndex, i)
                .pos(i % SLOTS_PER_ROW * 18, i / SLOTS_PER_ROW * 18);

            this.filterSlots.add(slot);
            slotGroup.child(slot);
        }

        Column column = (Column) new Column().pos(8, 28)
            .coverChildren()
            .childPadding(2)
            .child(slotGroup);

        child(column);
        height(110);
    }

    @Override
    protected AdvancedRefillUpgradeWrapper getWrapper() {
        return wrapper;
    }

    @Override
    public void dispose() {
        saveTargetSlot();
        super.dispose();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        // Save pending target slot change when mouse leaves the slot
        if (slotBeingChanged >= 0 && slotBeingChanged < filterSlots.size()) {
            if (!filterSlots.get(slotBeingChanged)
                .isHovering()) {
                saveTargetSlot();
            }
        }
    }

    private void handleScroll(int slotIdx, UpOrDown scrollDirection) {
        if (slotBeingChanged != slotIdx) {
            if (slotBeingChanged >= 0) {
                saveTargetSlot();
            }
            slotBeingChanged = slotIdx;
            targetSlotBeingChanged = wrapper.getTargetSlot(slotIdx);
        }
        targetSlotBeingChanged = scrollDirection == UpOrDown.UP ? targetSlotBeingChanged.next()
            : targetSlotBeingChanged.previous();
    }

    private void saveTargetSlot() {
        if (slotBeingChanged >= 0 && targetSlotBeingChanged != null) {
            wrapper.setTargetSlot(slotBeingChanged, targetSlotBeingChanged);

            if (slotSyncHandler != null) {
                final int slot = slotBeingChanged;
                final int ordinal = targetSlotBeingChanged.ordinal();
                slotSyncHandler
                    .syncToServer(UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_REFILL_TARGET_SLOT), buf -> {
                        buf.writeInt(slot);
                        buf.writeInt(ordinal);
                    });
            }
        }
        slotBeingChanged = -1;
        targetSlotBeingChanged = null;
    }
}
