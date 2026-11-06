package ruiseki.okstoragecore.upgrades.voiding;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSHRegistries;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.filter.FilterLogicControl;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class VoidUpgradeTab extends ExpandedUpgradeTab<VoidUpgradeWrapper> {

    private static final List<CyclicVariantButtonWidget.Variant> WORK_IN_GUI_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.works_in_gui"),
            OKBGuiTextures.VOID_ALL),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.only_automatic"),
            OKBGuiTextures.VOID_AUTOMATION));

    private static final List<CyclicVariantButtonWidget.Variant> VOID_TYPE_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.void_overflow"),
            OKBGuiTextures.VOID_OVERFLOW),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.void_any"),
            OKBGuiTextures.VOID_ANY));

    public VoidUpgradeTab(int slotIndex, VoidUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel,
        String titleKey) {
        super(slotIndex, wrapper, stack, storagePanel, titleKey);
    }

    protected CyclicVariantButtonWidget addVoidTypeButton() {
        return new CyclicVariantButtonWidget(
            VOID_TYPE_VARIANTS,
            this.getWrapper()
                .shouldVoidOverflow() ? 0 : 1,
            index -> {
                this.getWrapper()
                    .setShouldVoidOverflow(index == 0);
                this.getSyncHandler()
                    .syncToServer(UpgradeSlotSHRegistries.VOID_OVERFLOW, buffer -> buffer.writeBoolean(index == 0));
            }).size(20, 20);
    }

    protected CyclicVariantButtonWidget addWorkInGUIButton() {
        return new CyclicVariantButtonWidget(
            WORK_IN_GUI_VARIANTS,
            this.getWrapper()
                .shouldWorkInGUI() ? 0 : 1,
            index -> {
                this.getWrapper()
                    .setShouldWorkInGUI(index == 0);
                this.getSyncHandler()
                    .syncToServer(UpgradeSlotSHRegistries.VOID_WORK_IN_GUI, buffer -> buffer.writeBoolean(index == 0));
            }).size(20, 20);
    }

    public static class Basic extends VoidUpgradeTab {

        public Basic(int slotIndex, VoidUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.void");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn()
                .child(new FilterLogicControl.Basic(this.getSlotIndex(), this.getWrapper(), "basic_filter"));
        }
    }

    public static class Advanced extends VoidUpgradeTab {

        public Advanced(int slotIndex, VoidUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.advanced_void");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn().child(this.addButtonRow())
                .child(new FilterLogicControl.Advanced(this.getSlotIndex(), this.getWrapper(), "adv_filter"));
        }

        protected Flow addButtonRow() {
            return Flow.row()
                .coverChildrenHeight()
                .childPadding(2)
                .child(this.addWorkInGUIButton())
                .child(this.addVoidTypeButton());
        }
    }
}
