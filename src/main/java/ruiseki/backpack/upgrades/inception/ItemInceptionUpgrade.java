package ruiseki.backpack.upgrades.inception;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.backpack.backpack.ItemBackpack;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeSlotChangeResult;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemInceptionUpgrade extends ItemUpgradeBase<InceptionUpgradeWrapper> {

    public static final UpgradeType<InceptionUpgradeWrapper> TYPE = new UpgradeType<>(
        new UpgradeType.IFactory<InceptionUpgradeWrapper>() {

            @Override
            public InceptionUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
                Consumer<ItemStack> upgradeSaveHandler) {
                return new InceptionUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
            }

            @Override
            public @NotNull ExpandedUpgradeTab<InceptionUpgradeWrapper> createExpandedTab(int slotIndex,
                InceptionUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
                return new InceptionUpgradeTab(slotIndex, wrapper, stack, storagePanel);
            }
        });
    public static final List<UpgradeConflictDefinition> UPGRADE_CONFLICT_DEFINITIONS = List.of(
        new UpgradeConflictDefinition(
            ItemInceptionUpgrade.class::isInstance,
            0,
            LangHelpers.localize("gui.okbackpack.error.add.inception_exists")));

    public ItemInceptionUpgrade() {
        super(GeneralConfig.maxUpgradesPerStorageConfig);
    }

    @Override
    public UpgradeType<InceptionUpgradeWrapper> getType() {
        return TYPE;
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper storageWrapper, ItemStack upgradeStack,
        EntityPlayer player) {
        UpgradeSlotChangeResult result = super.canAddUpgradeTo(storageWrapper, upgradeStack, player);
        if (!result.isSuccessful()) {
            return result;
        }

        Set<Integer> errorUpgradeSlots = new HashSet<>();
        storageWrapper.getUpgradeHandler()
            .getSlotWrappers()
            .forEach((slot, wrapper) -> {
                if (wrapper instanceof InceptionUpgradeWrapper) {
                    errorUpgradeSlots.add(slot);
                }
            });

        if (!errorUpgradeSlots.isEmpty()) {
            return new UpgradeSlotChangeResult.Fail(
                LangHelpers.localize("gui.okbackpack.error.add.inception_exists"),
                errorUpgradeSlots,
                Collections.emptySet(),
                Collections.emptySet());
        }

        return new UpgradeSlotChangeResult.Success();
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return UPGRADE_CONFLICT_DEFINITIONS;
    }

    @Override
    public UpgradeSlotChangeResult canRemoveUpgradeFrom(IStorageWrapper storageWrapper, EntityPlayer player) {
        Set<Integer> slots = InventoryHelpers
            .getItemSlots(storageWrapper.getInventoryHandler(), stack -> stack.getItem() instanceof ItemBackpack);
        if (!slots.isEmpty()) {
            return new UpgradeSlotChangeResult.Fail(
                LangHelpers.localize("gui.okbackpack.error.remove.inception_sub_backpack"),
                Collections.emptySet(),
                slots,
                Collections.emptySet());
        }
        return new UpgradeSlotChangeResult.Success();
    }

    @Override
    public UpgradeSlotChangeResult canSwapUpgradeFor(ItemStack upgradeStackToPut, int upgradeSlot,
        IStorageWrapper storageWrapper, EntityPlayer player) {
        if (upgradeStackToPut.getItem() == this) {
            return new UpgradeSlotChangeResult.Success();
        }

        return canRemoveUpgradeFrom(storageWrapper, player);
    }
}
