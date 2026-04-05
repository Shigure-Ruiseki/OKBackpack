package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.network.NetworkUtils;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.wrapper.IVoidUpgrade;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.common.item.voiding.AdvancedVoidUpgradeWrapper;

public class AdvancedVoidUpgradeWidget extends AdvancedExpandedTabWidget<AdvancedVoidUpgradeWrapper> {

    private static final List<CyclicVariantButtonWidget.Variant> VOID_INPUT_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.void_all"), OKBGuiTextures.VOID_ALL),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.void_automation"),
            OKBGuiTextures.VOID_AUTOMATION));

    private static final List<CyclicVariantButtonWidget.Variant> VOID_TYPE_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.void_overflow"), OKBGuiTextures.VOID_OVERFLOW),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.void_any"), OKBGuiTextures.VOID_ANY));

    public AdvancedVoidUpgradeWidget(int slotIndex, AdvancedVoidUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        super(slotIndex, wrapper, stack, titleKey, "adv_common_filter", 6, 100);

        // CyclicVariantButtonWidget inputButton = new CyclicVariantButtonWidget(
        // VOID_INPUT_VARIANTS,
        // wrapper.getVoidInput()
        // .ordinal(),
        // index -> {
        // wrapper.setVoidInput(IVoidUpgrade.VoidInput.values()[index]);
        // updateWrapper();
        // });

        CyclicVariantButtonWidget voidButton = new CyclicVariantButtonWidget(
            VOID_TYPE_VARIANTS,
            wrapper.getVoidType()
                .ordinal(),
            index -> {
                wrapper.setVoidType(IVoidUpgrade.VoidType.values()[index]);
                updateWrapper();
            });

        this.startingRow.leftRel(0.5f)
            .height(20)
            .childPadding(2)
            // .child(inputButton)
            .child(voidButton);
    }

    private void updateWrapper() {
        if (this.filterWidget.getSlotSyncHandler() != null) {
            this.filterWidget.getSyncHandler()
                .syncToServer(UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_VOID), buf -> {
                    NetworkUtils.writeEnumValue(buf, wrapper.getVoidType());
                    NetworkUtils.writeEnumValue(buf, wrapper.getVoidInput());
                });
        }
    }
}
