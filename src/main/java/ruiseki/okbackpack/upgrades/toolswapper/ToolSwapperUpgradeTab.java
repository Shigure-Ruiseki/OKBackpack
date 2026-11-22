package ruiseki.okbackpack.upgrades.toolswapper;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okbackpack.client.gui.sync.OBUpgradeSlotSHRegistries;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.filter.FilterLogicControl;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class ToolSwapperUpgradeTab extends ExpandedUpgradeTab<ToolSwapperUpgradeWrapper> {

    private static final List<CyclicVariantButtonWidget.Variant> WEAPON_SWAP_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okbackpack.upgrades.buttons.swap_weapon"),
            OSCTextures.SWAP_WEAPON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okbackpack.upgrades.buttons.do_not_swap_weapon"),
            OSCTextures.NO_SWAP_WEAPON));

    private static final List<CyclicVariantButtonWidget.Variant> TOOL_SWAP_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okbackpack.upgrades.buttons.swap_tools"),
            OSCTextures.SWAP_TOOL),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okbackpack.upgrades.buttons.only_swap_for_tools"),
            OSCTextures.ONLY_TOOL_SWAP_TOOL),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okbackpack.upgrades.buttons.do_not_swap_tools"),
            OSCTextures.NO_SWAP_TOOL));

    public ToolSwapperUpgradeTab(int slotIndex, ToolSwapperUpgradeWrapper wrapper, ItemStack stack,
        StoragePanel storagePanel, String titleKey) {
        super(slotIndex, wrapper, stack, storagePanel, titleKey);
    }

    @Override
    protected Flow addUpgradeColumn() {
        return super.addUpgradeColumn().child(this.addButtonRow());
    }

    protected Flow addButtonRow() {
        return Flow.row()
            .coverChildren()
            .leftRel(0f)
            .child(this.addWeaponButton())
            .child(this.addToolSwapButton());
    }

    protected ButtonWidget<?> addWeaponButton() {
        return new CyclicVariantButtonWidget(
            WEAPON_SWAP_VARIANTS,
            this.getWrapper()
                .shouldSwapWeapon() ? 0 : 1,
            index -> {
                this.getWrapper()
                    .setSwapWeapon(index == 0);
                this.getSyncHandler()
                    .syncToServer(OBUpgradeSlotSHRegistries.WEAPON_SWAP, buffer -> buffer.writeBoolean(index == 0));
            });
    }

    protected ButtonWidget<?> addToolSwapButton() {
        return new CyclicVariantButtonWidget(
            TOOL_SWAP_VARIANTS,
            this.getWrapper()
                .getToolSwapMode()
                .ordinal(),
            index -> {
                this.getWrapper()
                    .setToolSwapMode(ToolSwapMode.fromOrdinal(index));
                this.getSyncHandler()
                    .syncToServer(OBUpgradeSlotSHRegistries.TOOL_SWAP, buffer -> buffer.writeInt(index));
            });
    }

    public static class Basic extends ToolSwapperUpgradeTab {

        public Basic(int slotIndex, ToolSwapperUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okbackpack.upgrades.tool_swapper");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn().child(
                new FilterLogicControl.Basic(
                    this.getSlotIndex(),
                    this.getWrapper()
                        .getFilterLogic(),
                    "basic_filter"));
        }
    }

    public static class Advanced extends ToolSwapperUpgradeTab {

        public Advanced(int slotIndex, ToolSwapperUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okbackpack.upgrades.advanced_tool_swapper");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn().child(
                new FilterLogicControl.Advanced(
                    this.getSlotIndex(),
                    this.getWrapper()
                        .getFilterLogic(),
                    "adv_filter"));
        }
    }
}
