package ruiseki.backpack.upgrades.inception;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.backpack.client.gui.sync.OBUpgradeSlotSHRegistries;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class InceptionUpgradeTab extends ExpandedUpgradeTab<InceptionUpgradeWrapper> {

    private static final List<CyclicVariantButtonWidget.Variant> INVENTORY_ORDER_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okbackpack.upgrades.buttons.inventory_order_main_first"),
            OSCTextures.MAIN_FIRST),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okbackpack.upgrades.buttons.inventory_order_incepted_first"),
            OSCTextures.INCEPTION_FIRST));

    public InceptionUpgradeTab(int slotIndex, InceptionUpgradeWrapper wrapper, ItemStack stack,
        StoragePanel storagePanel) {
        super(slotIndex, wrapper, stack, storagePanel, "gui.okbackpack.upgrades.inception");
    }

    @Override
    protected Flow addUpgradeColumn() {
        return super.addUpgradeColumn().child(addInventoryOrderButton());
    }

    protected CyclicVariantButtonWidget addInventoryOrderButton() {
        return new CyclicVariantButtonWidget(
            INVENTORY_ORDER_VARIANTS,
            this.getWrapper()
                .getInventoryOrder()
                .ordinal(),
            index -> {
                this.getWrapper()
                    .setInventoryOrder(InventoryOrder.fromOrdinal(index));
                this.getSyncHandler()
                    .syncToServer(OBUpgradeSlotSHRegistries.INVENTORY_ORDER, buffer -> buffer.writeInt(index));
            }).size(20, 20);
    }
}
