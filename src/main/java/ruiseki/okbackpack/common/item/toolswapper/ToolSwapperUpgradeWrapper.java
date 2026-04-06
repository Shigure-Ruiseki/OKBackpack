package ruiseki.okbackpack.common.item.toolswapper;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IToggleable;
import ruiseki.okbackpack.api.wrapper.IToolSwapperUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class ToolSwapperUpgradeWrapper extends UpgradeWrapperBase implements IToggleable, IToolSwapperUpgrade {

    public ToolSwapperUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public boolean isEnabled() {
        return ItemNBTHelpers.getBoolean(upgrade, ENABLED_TAG, true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        ItemNBTHelpers.setBoolean(upgrade, ENABLED_TAG, enabled);
        save();
    }

    @Override
    public void toggle() {
        setEnabled(!isEnabled());
    }

    @Override
    public WeaponSwapMode getWeaponSwapMode() {
        return WeaponSwapMode.SWAP_WEAPON;
    }

    @Override
    public void setWeaponSwapMode(WeaponSwapMode mode) {
        // Basic upgrade always swaps weapons, not configurable
    }

    @Override
    public ToolSwapMode getToolSwapMode() {
        return ToolSwapMode.SWAP_TOOL;
    }

    @Override
    public void setToolSwapMode(ToolSwapMode mode) {
        // Basic upgrade always swaps tools, not configurable
    }
}
