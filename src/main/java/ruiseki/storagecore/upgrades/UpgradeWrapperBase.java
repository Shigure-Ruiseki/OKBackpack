package ruiseki.storagecore.upgrades;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.storagecore.api.IStorageWrapper;

public abstract class UpgradeWrapperBase<W extends IUpgradeWrapper, T extends ItemUpgradeBase<W>>
    implements IUpgradeWrapper {

    protected final IStorageWrapper storageWrapper;
    protected final Consumer<ItemStack> upgradeSaveHandler;
    protected final ItemStack upgrade;
    protected final T upgradeItem;

    private long cooldown = 0;

    protected UpgradeWrapperBase(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        this.storageWrapper = storageWrapper;
        this.upgrade = upgrade;
        // noinspection unchecked
        upgradeItem = (T) upgrade.getItem();
        this.upgradeSaveHandler = upgradeSaveHandler;
    }

    @Override
    public ItemStack getUpgradeStack() {
        return upgrade;
    }

    protected void save() {
        upgradeSaveHandler.accept(upgrade);
    }

    protected void setCooldown(World world, int time) {
        cooldown = world.getWorldTime() + time;
    }

    public long getCooldownTime() {
        return cooldown;
    }

    public boolean isInCooldown(World world) {
        return getCooldownTime() > world.getWorldTime();
    }

    @Override
    public boolean isEnabled() {
        return NBTHelpers.getBoolean(upgrade, "enabled")
            .orElse(true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        NBTHelpers.setBoolean(upgrade, "enabled", enabled);
        save();
        storageWrapper.getUpgradeHandler()
            .refreshWrappersThatImplementAndTypeWrappers();
    }

    @Override
    public String getSettingLangKey() {
        return "";
    }
}
