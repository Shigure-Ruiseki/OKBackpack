package ruiseki.okstoragecore.upgrades.xppump;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemXpPumpUpgrade extends ItemUpgradeBase<XpPumpUpgradeWrapper> {

    public static final UpgradeType<XpPumpUpgradeWrapper> TYPE = new UpgradeType<>(
        new UpgradeType.IFactory<XpPumpUpgradeWrapper>() {

            @Override
            public XpPumpUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
                Consumer<ItemStack> upgradeSaveHandler) {
                return new XpPumpUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
            }

            @Override
            public @NotNull ExpandedUpgradeTab<XpPumpUpgradeWrapper> createExpandedTab(int slotIndex,
                XpPumpUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
                return new XpPumpUpgradeTab(
                    slotIndex,
                    wrapper,
                    stack,
                    storagePanel,
                    "gui.okstoragecore.upgrades.xp_pump");
            }
        });
    private final XpPumpUpgradeConfig xpPumpUpgradeConfig;

    public ItemXpPumpUpgrade(XpPumpUpgradeConfig xpPumpUpgradeConfig, IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.xpPumpUpgradeConfig = xpPumpUpgradeConfig;
    }

    public XpPumpUpgradeConfig getXpPumpUpgradeConfig() {
        return xpPumpUpgradeConfig;
    }

    @Override
    public UpgradeType<XpPumpUpgradeWrapper> getType() {
        return TYPE;
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return List.of();
    }
}
