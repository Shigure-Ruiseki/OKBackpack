package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.network.NetworkUtils;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.wrapper.IToolSwapperUpgrade;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.common.item.toolswapper.AdvancedToolSwapperUpgradeWrapper;

public class ToolSwapperUpgradeWidget extends AdvancedExpandedTabWidget<AdvancedToolSwapperUpgradeWrapper> {

    private static final List<CyclicVariantButtonWidget.Variant> WEAPON_SWAP_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.swap_weapon"), OKBGuiTextures.SWAP_WEAPON_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.no_swap_weapon"),
            OKBGuiTextures.NO_SWAP_WEAPON_ICON));

    private static final List<CyclicVariantButtonWidget.Variant> TOOL_SWAP_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.swap_tool"), OKBGuiTextures.SWAP_TOOL_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.only_tool_swap_tool"),
            OKBGuiTextures.ONLY_TOOL_SWAP_TOOL_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.no_swap_tool"),
            OKBGuiTextures.NO_SWAP_TOOL_ICON));

    public ToolSwapperUpgradeWidget(int slotIndex, AdvancedToolSwapperUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        super(slotIndex, wrapper, stack, panel, titleKey, "adv_common_filter", 6, 100);

        CyclicVariantButtonWidget weaponButton = new CyclicVariantButtonWidget(
            WEAPON_SWAP_VARIANTS,
            wrapper.getWeaponSwapMode()
                .ordinal(),
            index -> {
                wrapper.setWeaponSwapMode(IToolSwapperUpgrade.WeaponSwapMode.values()[index]);
                updateWrapper();
            });

        CyclicVariantButtonWidget toolButton = new CyclicVariantButtonWidget(
            TOOL_SWAP_VARIANTS,
            wrapper.getToolSwapMode()
                .ordinal(),
            index -> {
                wrapper.setToolSwapMode(IToolSwapperUpgrade.ToolSwapMode.values()[index]);
                updateWrapper();
            });

        this.startingRow.leftRel(0.5f)
            .height(20)
            .childPadding(2)
            .child(weaponButton)
            .child(toolButton);
    }

    private void updateWrapper() {
        if (this.filterWidget.getSlotSyncHandler() != null) {
            this.filterWidget.getSyncHandler()
                .syncToServer(UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_TOOL_SWAPPER), buf -> {
                    NetworkUtils.writeEnumValue(buf, wrapper.getWeaponSwapMode());
                    NetworkUtils.writeEnumValue(buf, wrapper.getToolSwapMode());
                });
        }
    }
}
