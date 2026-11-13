package ruiseki.backpack.upgrades.everlasting;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeType;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class ItemEverlastingUpgrade extends ItemUpgradeBase<ItemEverlastingUpgrade.Wrapper> {

    public static final UpgradeType<Wrapper> TYPE = new UpgradeType<>(Wrapper::new);
    public static final List<UpgradeConflictDefinition> UPGRADE_CONFLICT_DEFINITIONS = List.of(
        new UpgradeConflictDefinition(
            ItemEverlastingUpgrade.class::isInstance,
            0,
            LangHelpers.localize("gui.okbackpack.error.add.everlasting_exists")));

    public ItemEverlastingUpgrade() {
        super(GeneralConfig.maxUpgradesPerStorageConfig);
    }

    @Override
    public UpgradeType<Wrapper> getType() {
        return TYPE;
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return UPGRADE_CONFLICT_DEFINITIONS;
    }

    public static class Wrapper extends UpgradeWrapperBase<Wrapper, ItemEverlastingUpgrade> {

        public Wrapper(IStorageWrapper backpackWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
            super(backpackWrapper, upgrade, upgradeSaveHandler);
        }

        @Override
        public boolean hideSettingsTab() {
            return true;
        }

        @Override
        public boolean canBeDisabled() {
            return false;
        }
    }
}
