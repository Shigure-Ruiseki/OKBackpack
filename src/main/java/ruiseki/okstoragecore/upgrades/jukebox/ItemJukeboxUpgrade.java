package ruiseki.okstoragecore.upgrades.jukebox;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.CommonSHRegistries;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedBooleanSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedIntSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeGroup;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemJukeboxUpgrade extends ItemUpgradeBase<JukeboxUpgradeWrapper> {

    public static final UpgradeGroup UPGRADE_GROUP = new UpgradeGroup(
        "jukebox_upgrades",
        LangHelpers.localize("jukebox_upgrades"));
    public static final UpgradeType<JukeboxUpgradeWrapper> TYPE = new UpgradeType<>(new UpgradeType.IFactory<>() {

        @Override
        public JukeboxUpgradeWrapper create(IStorageWrapper storageWrapper, ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler) {
            return new JukeboxUpgradeWrapper(storageWrapper, upgrade, upgradeSaveHandler);
        }

        @Override
        public void updateTabDelegates(JukeboxUpgradeWrapper wrapper, UpgradeSlotUpdate slotUpdate) {
            DelegatedItemHandlerSH handler = wrapper.getSlotCount() == 1 ? slotUpdate.get("basic_jukebox_handler")
                : slotUpdate.get("adv_jukebox_handler");

            handler.setItemHandler(wrapper::getStorage);
            handler.syncToServer(CommonSHRegistries.STORAGE);

            DelegatedIntSH slotActiveHandler = slotUpdate.get("jukebox_slot_active");
            slotActiveHandler.setDelegated(wrapper::getDiscSlotActive, wrapper::setDiscSlotActive);
            slotActiveHandler.syncToServer(CommonSHRegistries.JUKEBOX_SLOT_ACTIVE);

            DelegatedBooleanSH isPlayingHandler = slotUpdate.get("jukebox_is_playing");
            isPlayingHandler.setDelegated(wrapper::isPlaying, wrapper::setPlaying);
            isPlayingHandler.syncToServer(CommonSHRegistries.JUKEBOX_IS_PLAYING);
        }

        @Override
        public @NotNull ExpandedUpgradeTab<JukeboxUpgradeWrapper> createExpandedTab(int slotIndex,
            JukeboxUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            return wrapper.getSlotCount() == 1 ? new JukeboxUpgradeTab.Basic(slotIndex, wrapper, stack, storagePanel)
                : new JukeboxUpgradeTab.Advanced(slotIndex, wrapper, stack, storagePanel);
        }
    });
    private final IntSupplier numberOfSlots;

    public ItemJukeboxUpgrade(IntSupplier numberOfSlots, IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
        this.numberOfSlots = numberOfSlots;
    }

    @Override
    public UpgradeType<JukeboxUpgradeWrapper> getType() {
        return TYPE;
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return List.of();
    }

    @Override
    public UpgradeGroup getUpgradeGroup() {
        return UPGRADE_GROUP;
    }

    public int getNumberOfSlots() {
        return numberOfSlots.getAsInt();
    }
}
