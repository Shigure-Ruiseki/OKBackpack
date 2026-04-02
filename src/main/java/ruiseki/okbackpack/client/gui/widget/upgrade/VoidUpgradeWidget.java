package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.network.NetworkUtils;

import ruiseki.okbackpack.api.wrapper.IVoidUpgrade;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.common.block.BackpackPanel;
import ruiseki.okbackpack.common.item.wrapper.VoidUpgradeWrapper;

public class VoidUpgradeWidget extends BasicExpandedTabWidget<VoidUpgradeWrapper> {

    private static final List<CyclicVariantButtonWidget.Variant> VOID_INPUT_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.void_all"), OKBGuiTextures.VOID_ALL),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.void_automation"),
            OKBGuiTextures.VOID_AUTOMATION));

    private static final List<CyclicVariantButtonWidget.Variant> VOID_TYPE_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.void_overflow"), OKBGuiTextures.VOID_OVERFLOW),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.void_any"), OKBGuiTextures.VOID_ANY));

    public VoidUpgradeWidget(int slotIndex, VoidUpgradeWrapper wrapper, ItemStack stack, BackpackPanel panel,
        String titleKey) {
        super(slotIndex, wrapper, stack, titleKey, "common_filter", 5, 80);

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
                .syncToServer(UpgradeSlotSH.UPDATE_VOID, buf -> {
                    NetworkUtils.writeEnumValue(buf, wrapper.getVoidType());
                    NetworkUtils.writeEnumValue(buf, wrapper.getVoidInput());
                });
        }
    }
}
