package ruiseki.okstoragecore.upgrades.pump;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSHRegistries;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.filter.FluidFilterLogicControl;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class PumpUpgradeTab extends ExpandedUpgradeTab<PumpUpgradeWrapper> {

    protected static final List<CyclicVariantButtonWidget.Variant> INPUT_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.pump_input"),
            OSCTextures.PUMP_INPUT),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.pump_output"),
            OSCTextures.PUMP_OUTPUT));

    private static final List<CyclicVariantButtonWidget.Variant> HAND_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.interact_with_hand"),
            OSCTextures.PUMP_INTERACT_HAND),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.do_not_interact_with_hand"),
            OSCTextures.PUMP_NO_INTERACT_HAND));

    private static final List<CyclicVariantButtonWidget.Variant> WORLD_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.interact_with_world"),
            OSCTextures.PUMP_INTERACT_WORLD),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.do_not_interact_with_world"),
            OSCTextures.PUMP_NO_INTERACT_WORLD));

    public PumpUpgradeTab(int slotIndex, PumpUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel,
        String titleKey) {
        super(slotIndex, wrapper, stack, storagePanel, titleKey);
    }

    protected CyclicVariantButtonWidget addInputButton() {
        return new CyclicVariantButtonWidget(INPUT_VARIANTS, wrapper.isInput() ? 0 : 1, index -> {
            wrapper.setIsInput(index == 0);
            this.getSyncHandler()
                .syncToServer(UpgradeSlotSHRegistries.PUMP_INPUT, buffer -> buffer.writeBoolean(index == 0));
        });
    }

    protected CyclicVariantButtonWidget addHandButton() {
        return new CyclicVariantButtonWidget(HAND_VARIANTS, wrapper.shouldInteractWithHand() ? 0 : 1, index -> {
            wrapper.setInteractWithHand(index == 0);
            this.getSyncHandler()
                .syncToServer(UpgradeSlotSHRegistries.INTERACTION_WITH_HAND, buffer -> buffer.writeBoolean(index == 0));
        });
    }

    protected CyclicVariantButtonWidget addWorldButton() {
        return new CyclicVariantButtonWidget(WORLD_VARIANTS, wrapper.shouldInteractWithWorld() ? 0 : 1, index -> {
            wrapper.setInteractWithWorld(index == 0);
            this.getSyncHandler()
                .syncToServer(
                    UpgradeSlotSHRegistries.INTERACTION_WITH_WORLD,
                    buffer -> buffer.writeBoolean(index == 0));
        });
    }

    public static class Basic extends PumpUpgradeTab {

        public Basic(int slotIndex, PumpUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.pump");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn().child(this.addButtonRow())
                .child(new FluidFilterLogicControl.Basic(this.getSlotIndex(), this.getWrapper(), "basic_fluid_filter"));
        }

        protected Flow addButtonRow() {
            return Flow.row()
                .coverChildrenHeight()
                .childPadding(2)
                .child(this.addInputButton());
        }
    }

    public static class Advanced extends PumpUpgradeTab {

        public Advanced(int slotIndex, PumpUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.advanced_pump");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn().child(this.addButtonRow())
                .child(
                    new FluidFilterLogicControl.Advanced(this.getSlotIndex(), this.getWrapper(), "adv_fluid_filter"));
        }

        protected Flow addButtonRow() {
            return Flow.row()
                .coverChildrenHeight()
                .childPadding(2)
                .child(this.addInputButton())
                .child(this.addHandButton())
                .child(this.addWorldButton());
        }
    }
}
