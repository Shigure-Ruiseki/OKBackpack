package ruiseki.okbackpack.common.item.toolswapper;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IToolSwapperUpgrade;
import ruiseki.okbackpack.common.item.AdvancedUpgradeWrapper;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class AdvancedToolSwapperUpgradeWrapper extends AdvancedUpgradeWrapper implements IToolSwapperUpgrade {

    public AdvancedToolSwapperUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage,
        Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.advanced_tool_swapper_settings";
    }

    @Override
    public WeaponSwapMode getWeaponSwapMode() {
        int ordinal = ItemNBTHelpers.getInt(upgrade, WEAPON_SWAP_TAG, WeaponSwapMode.SWAP_WEAPON.ordinal());
        WeaponSwapMode[] modes = WeaponSwapMode.values();
        if (ordinal < 0 || ordinal >= modes.length) return WeaponSwapMode.SWAP_WEAPON;
        return modes[ordinal];
    }

    @Override
    public void setWeaponSwapMode(WeaponSwapMode mode) {
        if (mode == null) mode = WeaponSwapMode.SWAP_WEAPON;
        ItemNBTHelpers.setInt(upgrade, WEAPON_SWAP_TAG, mode.ordinal());
        save();
    }

    @Override
    public ToolSwapMode getToolSwapMode() {
        int ordinal = ItemNBTHelpers.getInt(upgrade, TOOL_SWAP_TAG, ToolSwapMode.SWAP_TOOL.ordinal());
        ToolSwapMode[] modes = ToolSwapMode.values();
        if (ordinal < 0 || ordinal >= modes.length) return ToolSwapMode.SWAP_TOOL;
        return modes[ordinal];
    }

    @Override
    public void setToolSwapMode(ToolSwapMode mode) {
        if (mode == null) mode = ToolSwapMode.SWAP_TOOL;
        ItemNBTHelpers.setInt(upgrade, TOOL_SWAP_TAG, mode.ordinal());
        save();
    }
}
