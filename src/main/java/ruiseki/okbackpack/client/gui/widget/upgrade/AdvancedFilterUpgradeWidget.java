package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.network.NetworkUtils;

import lombok.Getter;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.wrapper.IFilterUpgrade;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.common.item.filter.AdvancedFilterUpgradeWrapper;

public class AdvancedFilterUpgradeWidget extends AdvancedExpandedTabWidget<AdvancedFilterUpgradeWrapper> {

    private static final List<CyclicVariantButtonWidget.Variant> FILTER_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.input_output"), OKBGuiTextures.IN_OUT_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.input"), OKBGuiTextures.IN_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.output"), OKBGuiTextures.OUT_ICON));

    @Getter
    private final CyclicVariantButtonWidget filterButton;

    public AdvancedFilterUpgradeWidget(int slotIndex, AdvancedFilterUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        super(slotIndex, wrapper, stack, titleKey, "adv_common_filter", 6, 100);

        this.filterButton = new CyclicVariantButtonWidget(
            FILTER_VARIANTS,
            wrapper.getfilterWay()
                .ordinal(),
            index -> {
                wrapper.setFilterWay(IFilterUpgrade.FilterWayType.values()[index]);
                if (this.filterWidget.getSlotSyncHandler() != null) {
                    this.filterWidget.getSyncHandler()
                        .syncToServer(
                            UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_FILTER),
                            buf -> { NetworkUtils.writeEnumValue(buf, wrapper.getfilterWay()); });
                }
            });

        this.startingRow.leftRel(0.5f)
            .height(20)
            .childPadding(2)
            .child(this.filterButton);
    }

}
