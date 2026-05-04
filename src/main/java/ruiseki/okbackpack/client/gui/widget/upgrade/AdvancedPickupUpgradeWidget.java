package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.network.NetworkUtils;

import lombok.Getter;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.wrapper.IPickupUpgrade.PickupFilterType;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.common.item.pickup.AdvancedPickupUpgradeWrapper;

public class AdvancedPickupUpgradeWidget extends AdvancedExpandedTabWidget<AdvancedPickupUpgradeWrapper> {

    private static final List<CyclicVariantButtonWidget.Variant> PICKUP_FILTER_VARIANTS = Arrays.asList(
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
    private final CyclicVariantButtonWidget pickupFilterButton;

    public AdvancedPickupUpgradeWidget(int slotIndex, AdvancedPickupUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        super(slotIndex, wrapper, stack, panel, titleKey, "adv_common_filter", 6, 100);

        this.pickupFilterButton = new CyclicVariantButtonWidget(
            PICKUP_FILTER_VARIANTS,
            wrapper.getPickupFilterType()
                .ordinal(),
            index -> {
                wrapper.setPickupFilterType(PickupFilterType.values()[index]);
                if (this.filterWidget.getSlotSyncHandler() != null) {
                    this.filterWidget.getSyncHandler()
                        .syncToServer(
                            UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_PICKUP),
                            buf -> NetworkUtils.writeEnumValue(buf, wrapper.getPickupFilterType()));
                }
            });

        this.filterWidget.replaceFilterTypeButton(this.pickupFilterButton);
        this.filterWidget.setSlotsDisabled(() -> wrapper.getPickupFilterType() == PickupFilterType.STORAGE);
    }
}
