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
import ruiseki.okbackpack.common.item.deposit.DepositFilterType;
import ruiseki.okbackpack.common.item.deposit.DepositUpgradeWrapper;

public class DepositUpgradeWidget extends BasicExpandedTabWidget<DepositUpgradeWrapper> {

    private static final List<CyclicVariantButtonWidget.Variant> DEPOSIT_FILTER_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.deposit_filter_allow"),
            OKBGuiTextures.CHECK_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.deposit_filter_block"),
            OKBGuiTextures.CROSS_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.deposit_filter_inventory"),
            OKBGuiTextures.MATCH_INVENTORY_ICON));

    @Getter
    private final CyclicVariantButtonWidget depositFilterButton;

    public DepositUpgradeWidget(int slotIndex, DepositUpgradeWrapper wrapper, ItemStack stack, IStoragePanel<?> panel,
        String titleKey) {
        super(slotIndex, wrapper, stack, panel, titleKey, "deposit_filter", 4, 75);

        this.depositFilterButton = new CyclicVariantButtonWidget(
            DEPOSIT_FILTER_VARIANTS,
            wrapper.getDepositFilterType()
                .ordinal(),
            index -> {
                wrapper.setDepositFilterType(DepositFilterType.values()[index]);
                if (this.filterWidget.getSlotSyncHandler() != null) {
                    this.filterWidget.getSyncHandler()
                        .syncToServer(
                            UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_DEPOSIT),
                            buf -> NetworkUtils.writeEnumValue(buf, wrapper.getDepositFilterType()));
                }
            });

        this.filterWidget.replaceFilterTypeButton(this.depositFilterButton);
        this.filterWidget.setSlotsDisabled(() -> wrapper.getDepositFilterType() == DepositFilterType.INVENTORY);
    }
}
