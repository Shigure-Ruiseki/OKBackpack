package ruiseki.okstoragecore.upgrades.infinity;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okcore.helper.PlayerHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.helper.TranslateHelpers;
import ruiseki.okstoragecore.inventory.IInventoryPartHandler;
import ruiseki.okstoragecore.inventory.InventoryHandler;
import ruiseki.okstoragecore.inventory.InventoryPartRegistry;
import ruiseki.okstoragecore.inventory.InventoryPartitioner.SlotRange;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeSlotChangeResult;
import ruiseki.okstoragecore.upgrades.UpgradeType;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class ItemInfinityUpgrade extends ItemUpgradeBase<ItemInfinityUpgrade.Wrapper> {

    public static final List<UpgradeConflictDefinition> UPGRADE_CONFLICT_DEFINITIONS = List.of(
        new UpgradeConflictDefinition(
            i -> true,
            0,
            TranslateHelpers.error("add.any_upgrade_exists"),
            TranslateHelpers.error("add.no_upgrade_can_be_added")));
    public static final UpgradeType<ItemInfinityUpgrade.Wrapper> TYPE = new UpgradeType<>(
        ItemInfinityUpgrade.Wrapper::new);

    static {
        InventoryPartRegistry.registerFactory(
            InfinityInventoryPart.Admin.NAME,
            (parent, slotRange, getMemorySettings) -> new InfinityInventoryPart.Admin(parent, slotRange));
        InventoryPartRegistry.registerFactory(
            InfinityInventoryPart.Survival.NAME,
            (parent, slotRange, getMemorySettings) -> new InfinityInventoryPart.Survival(parent, slotRange));
    }

    private final boolean admin;

    public ItemInfinityUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig, boolean admin) {
        super(upgradeTypeLimitConfig);
        this.admin = admin;
    }

    @Override
    public UpgradeType<Wrapper> getType() {
        return TYPE;
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return UPGRADE_CONFLICT_DEFINITIONS;
    }

    @Override
    public UpgradeSlotChangeResult canRemoveUpgradeFrom(IStorageWrapper storageWrapper, EntityPlayer player) {
        if (PlayerHelpers.hasPermissions(player, this.getPermissionLevel())) {
            return super.canRemoveUpgradeFrom(storageWrapper, player);
        }

        return new UpgradeSlotChangeResult.Fail(
            TranslateHelpers.error("remove.infinity_upgrade_only_admin"),
            Set.of(),
            Set.of(),
            Set.of());
    }

    public int getPermissionLevel() {
        return admin ? 2 : 0;
    }

    public IInventoryPartHandler createInventoryPartHandler(InventoryHandler parent, SlotRange slotRange) {
        return admin ? new InfinityInventoryPart.Admin(parent, slotRange)
            : new InfinityInventoryPart.Survival(parent, slotRange);
    }

    public static class Wrapper extends UpgradeWrapperBase<Wrapper, ItemInfinityUpgrade> {

        protected Wrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
            super(storageWrapper, upgrade, upgradeSaveHandler);
        }

        @Override
        public boolean canBeDisabled() {
            return false;
        }

        @Override
        public boolean hideSettingsTab() {
            return true;
        }

        @Override
        public void onAdded() {
            super.onAdded();
            InventoryHandler inventoryHandler = this.storageWrapper.getInventoryHandler();
            inventoryHandler.getInventoryPartitioner()
                .addInventoryPart(
                    0,
                    Integer.MAX_VALUE,
                    this.upgradeItem
                        .createInventoryPartHandler(inventoryHandler, new SlotRange(0, inventoryHandler.getSlots())));
        }

        @Override
        public void onBeforeRemoved() {
            super.onBeforeRemoved();
            this.storageWrapper.getInventoryHandler()
                .getInventoryPartitioner()
                .removeInventoryPart(0);
            this.save();
        }

        public int getPermissionLevel() {
            return this.upgradeItem.getPermissionLevel();
        }
    }
}
