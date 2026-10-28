package ruiseki.okstoragecore.upgrades.feeding;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.filter.FilterLogicControl;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class FeedingUpgradeTab extends ExpandedUpgradeTab<FeedingUpgradeWrapper> {

    public FeedingUpgradeTab(int slotIndex, FeedingUpgradeWrapper wrapper, ItemStack stack,
        IStorageWrapper storageWrapper, StoragePanel storagePanel, String titleKey) {
        super(slotIndex, wrapper, stack, storageWrapper, storagePanel, titleKey);
    }

    public static class Basic extends FeedingUpgradeTab {

        public Basic(int slotIndex, FeedingUpgradeWrapper wrapper, ItemStack stack, IStorageWrapper storageWrapper,
            StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storageWrapper, storagePanel, "gui.okstoragecore.upgrades.feeding");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn()
                .child(new FilterLogicControl.Basic(this.getSlotIndex(), this.getWrapper(), "basic_filter_"));
        }
    }

    public static class Advanced extends FeedingUpgradeTab {

        private static final List<CyclicVariantButtonWidget.Variant> HUNGER_VARIANTS = Arrays.asList(
            new CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.okstoragecore.upgrades.buttons.hunger_level_any"),
                OSCTextures.FULL_HUNGER),
            new CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.okstoragecore.upgrades.buttons.hunger_level_half"),
                OSCTextures.HALF_HUNGER),
            new CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.okstoragecore.upgrades.buttons.hunger_level_full"),
                OSCTextures.ANY_HUNGER));

        private static final List<CyclicVariantButtonWidget.Variant> HEART_VARIANTS = Arrays.asList(
            new CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.okstoragecore.upgrades.buttons.feed_immediately_when_hurt"),
                OSCTextures.HALF_HEART),
            new CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.okstoragecore.upgrades.buttons.do_not_consider_health"),
                OSCTextures.IGNORE_HALF_HEART));

        public Advanced(int slotIndex, FeedingUpgradeWrapper wrapper, ItemStack stack, IStorageWrapper storageWrapper,
            StoragePanel storagePanel) {
            super(
                slotIndex,
                wrapper,
                stack,
                storageWrapper,
                storagePanel,
                "gui.okstoragecore.upgrades.advanced_feeding");
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
                .child(this.addHungerLevelButton())
                .child(this.addFeedWhenHurtButton());
        }

        protected CyclicVariantButtonWidget addHungerLevelButton() {
            return new CyclicVariantButtonWidget(
                HUNGER_VARIANTS,
                this.getWrapper()
                    .getFeedAtHungerLevel()
                    .ordinal(),
                index -> {
                    this.getWrapper()
                        .setFeedAtHungerLevel(HungerLevel.fromOrdinal(index));
                }).size(20, 20);
        }

        protected CyclicVariantButtonWidget addFeedWhenHurtButton() {
            return new CyclicVariantButtonWidget(
                HEART_VARIANTS,
                this.getWrapper()
                    .shouldFeedImmediatelyWhenHurt() ? 0 : 1,
                index -> {
                    this.getWrapper()
                        .setFeedImmediatelyWhenHurt(index == 0);
                }).size(20, 20);
        }
    }
}
