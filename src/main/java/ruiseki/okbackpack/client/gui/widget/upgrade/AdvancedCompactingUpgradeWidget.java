package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;

import lombok.Getter;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.common.item.wrapper.AdvancedCompactingUpgradeWrapper;

public class AdvancedCompactingUpgradeWidget extends AdvancedExpandedTabWidget<AdvancedCompactingUpgradeWrapper> {

    private static final List<CyclicVariantButtonWidget.Variant> REVERSIBLE_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.only_reversible"), OKBGuiTextures.CHECK_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.all_recipes"), OKBGuiTextures.CROSS_ICON));

    @Getter
    private final CyclicVariantButtonWidget reversibleButton;

    public AdvancedCompactingUpgradeWidget(int slotIndex, AdvancedCompactingUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel panel, String titleKey) {
        super(slotIndex, wrapper, stack, titleKey, "adv_common_filter", 6, 100);

        this.reversibleButton = new CyclicVariantButtonWidget(
            REVERSIBLE_VARIANTS,
            wrapper.isOnlyReversible() ? 0 : 1,
            index -> {
                wrapper.setOnlyReversible(index == 0);
                if (this.filterWidget.getSlotSyncHandler() != null) {
                    this.filterWidget.getSyncHandler()
                        .syncToServer(
                            UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_COMPACTING),
                            buf -> { buf.writeBoolean(wrapper.isOnlyReversible()); });
                }
            });

        this.startingRow.leftRel(0.5f)
            .height(20)
            .childPadding(2)
            .child(this.reversibleButton);
    }

}
