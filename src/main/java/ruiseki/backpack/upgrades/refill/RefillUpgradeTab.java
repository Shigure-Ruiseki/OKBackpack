package ruiseki.backpack.upgrades.refill;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.backpack.client.gui.slot.RefillPhantomSlot;
import ruiseki.backpack.client.gui.sync.OBUpgradeSlotSHRegistries;
import ruiseki.backpack.upgrades.refill.RefillUpgradeWrapper.TargetSlot;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.StorageSlotGrid;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class RefillUpgradeTab extends ExpandedUpgradeTab<RefillUpgradeWrapper> {

    public RefillUpgradeTab(int slotIndex, RefillUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel,
        String titleKey) {
        super(slotIndex, wrapper, stack, storagePanel, titleKey);
    }

    public static class Basic extends RefillUpgradeTab {

        public Basic(int slotIndex, RefillUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okbackpack.upgrades.refill");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn()
                .child(new RefillControl(this.getSlotIndex(), this.getWrapper(), 6, "basic_refill"));
        }
    }

    public static class Advanced extends RefillUpgradeTab {

        public Advanced(int slotIndex, RefillUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okbackpack.upgrades.advanced_refill");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn()
                .child(new RefillControl(this.getSlotIndex(), this.getWrapper(), 12, "adv_refill"));
        }
    }

    protected static class RefillControl extends ParentWidget<RefillControl> {

        private final int slotIndex;
        private final RefillUpgradeWrapper wrapper;
        private final int slotCount;
        private final String syncKey;

        // Temporary scroll state, indexed by refill filter slot.
        private final Map<Integer, TargetSlot> changedTargetSlots = new HashMap<>();

        protected RefillControl(int slotIndex, RefillUpgradeWrapper filterLogic, int slotCount, String syncKey) {
            this.slotIndex = slotIndex;
            this.wrapper = filterLogic;
            this.slotCount = slotCount;
            this.syncKey = syncKey;
            this.coverChildren();

            this.syncHandler("upgrades", this.getSlotIndex());
            this.child(this.addContentColumn());
        }

        protected Flow addContentColumn() {
            return Flow.column()
                .coverChildren()
                .childPadding(2)
                .collapseDisabledChild()
                .child(this.addFilterGroup());
        }

        protected Flow addFilterGroup() {
            final int columns = 4;
            final int slotCount = this.getSlotCount();
            final int rows = (slotCount + columns - 1) / columns;

            StorageSlotGrid<ItemSlot> slotGrid = StorageSlotGrid.<ItemSlot>builder()
                .matrix(columns, rows, 'F')
                .maxSlots(slotCount)
                .key(
                    'F',
                    i -> new RefillPhantomSlot(
                        () -> this.changedTargetSlots.getOrDefault(
                            i,
                            this.getWrapper()
                                .getTargetSlots()
                                .get(i)),
                        scrollDir -> handleScroll(i, scrollDir))
                            .name(this.getSyncKey() + "_" + this.getSlotIndex() + "_" + i)
                            .syncHandler(this.getSyncKey() + "_" + this.getSlotIndex(), i))
                .build();

            return Flow.column()
                .coverChildren()
                .child(slotGrid);
        }

        private void handleScroll(int slotIdx, UpOrDown scrollDirection) {
            TargetSlot currentTarget = this.changedTargetSlots.getOrDefault(
                slotIdx,
                this.getWrapper()
                    .getTargetSlots()
                    .get(slotIdx));

            TargetSlot newTarget = scrollDirection == UpOrDown.UP ? currentTarget.next() : currentTarget.previous();

            this.changedTargetSlots.put(slotIdx, newTarget);

            this.getWrapper()
                .setTargetSlot(slotIdx, newTarget);

            getSyncHandler().syncToServer(OBUpgradeSlotSHRegistries.REFILL_TARGET_SLOT, buf -> {
                buf.writeInt(slotIdx);
                buf.writeInt(newTarget.ordinal());
            });
        }

        @Override
        public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
            return syncOrValue instanceof UpgradeSlotSH;
        }

        @Override
        public @NotNull UpgradeSlotSH getSyncHandler() {
            return (UpgradeSlotSH) super.getSyncHandler();
        }

        public int getSlotCount() {
            return slotCount;
        }

        public int getSlotIndex() {
            return slotIndex;
        }

        public String getSyncKey() {
            return syncKey;
        }

        public RefillUpgradeWrapper getWrapper() {
            return wrapper;
        }
    }

}
