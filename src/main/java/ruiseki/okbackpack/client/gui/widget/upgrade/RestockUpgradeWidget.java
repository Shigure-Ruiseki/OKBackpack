package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.network.NetworkUtils;

import lombok.Getter;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.common.item.restock.RestockFilterType;
import ruiseki.okbackpack.common.item.restock.RestockUpgradeWrapper;

public class RestockUpgradeWidget extends BasicExpandedTabWidget<RestockUpgradeWrapper> {

    private static final List<CyclicVariantButtonWidget.Variant> RESTOCK_FILTER_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.restock_filter_allow"),
            OKBGuiTextures.CHECK_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.restock_filter_block"),
            OKBGuiTextures.CROSS_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.restock_filter_storage"),
            OKBGuiTextures.MATCH_BACKPACK_ICON));

    @Getter
    private final CyclicVariantButtonWidget restockFilterButton;

    public RestockUpgradeWidget(int slotIndex, RestockUpgradeWrapper wrapper, ItemStack stack, IStoragePanel<?> panel,
        String titleKey) {
        super(slotIndex, wrapper, stack, panel, titleKey, "restock_filter", 4, 75);

        this.restockFilterButton = new CyclicVariantButtonWidget(
            RESTOCK_FILTER_VARIANTS,
            wrapper.getRestockFilterType()
                .ordinal(),
            index -> {
                wrapper.setRestockFilterType(RestockFilterType.values()[index]);
                if (this.filterWidget.getSlotSyncHandler() != null) {
                    this.filterWidget.getSyncHandler()
                        .syncToServer(
                            UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_RESTOCK),
                            buf -> NetworkUtils.writeEnumValue(buf, wrapper.getRestockFilterType()));
                }
            });

        this.filterWidget.replaceFilterTypeButton(this.restockFilterButton);
        this.filterWidget.setSlotsDisabled(() -> wrapper.getRestockFilterType() == RestockFilterType.STORAGE);
    }
}
