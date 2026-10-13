package ruiseki.backpack.backpack.wrapper;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Nullable;

import ruiseki.backpack.backpack.BackpackStorage;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.storagecore.inventory.InventoryHandler;

public class BackpackWrapper implements IBackpackWrapper {

    public static final int DEFAULT_CLOTH_COLOR = 13394234;
    public static final int DEFAULT_BORDER_COLOR = 6434330;
    private static final String CLOTH_COLOR_TAG = "clothColor";
    private static final String BORDER_COLOR_TAG = "borderColor";
    private static final String CONTENTS_UUID_TAG = "contentsUuid";
    private static final String INVENTORY_SLOTS_TAG = "inventorySlots";

    @Nullable
    private ItemStack backpack;
    private String tierId;
    private Runnable backpackSaveHandler = () -> {};
    private Runnable inventorySlotChangeHandler = () -> {};

    @Nullable
    private InventoryHandler handler;

    private Runnable onInventoryHandlerRefresh = () -> {};

    public BackpackWrapper(ItemStack backpack, String tierId) {
        this.backpack = backpack;
        this.tierId = tierId;
    }

    public InventoryHandler getInventoryHandler() {
        if (handler == null) {
            handler = new InventoryHandler(54, this, getBackpackContentsNbt(), () -> {
                this.markBackpackContentsDirty();
                this.inventorySlotChangeHandler.run();
            });
        }
        return handler;
    }

    @Override
    public ItemStack getBackpack() {
        return this.backpack;
    }

    @Override
    public void setContentsUuid(UUID storageUuid) {
        NBTHelpers.setUniqueId(backpack, CONTENTS_UUID_TAG, storageUuid);
    }

    @Override
    public void setSaveHandler(Runnable saveHandler) {
        backpackSaveHandler = saveHandler;
    }

    @Override
    public void setInventorySlotChangeHandler(Runnable slotChangeHandler) {
        inventorySlotChangeHandler = slotChangeHandler;
    }

    @Override
    public Optional<UUID> getContentsUuid() {
        return NBTHelpers.getUniqueId(backpack, CONTENTS_UUID_TAG);
    }

    private UUID getOrCreateContentsUuid() {
        Optional<UUID> contentsUuid = getContentsUuid();
        if (contentsUuid.isPresent()) {
            return contentsUuid.get();
        }
        clearDummyHandlers();
        UUID newUuid = UUID.randomUUID();
        setContentsUuid(newUuid);
        migrateBackpackContents(newUuid);
        return newUuid;
    }

    private void clearDummyHandlers() {
        // if (upgradeHandler == Noop.INSTANCE.getUpgradeHandler()) {
        // upgradeHandler = null;
        // }
        // if (settingsHandler == Noop.INSTANCE.getSettingsHandler()) {
        // settingsHandler = null;
        // }
    }

    private NBTTagCompound getBackpackContentsNbt() {
        return BackpackStorage.get()
            .getOrCreateBackpackContents(getOrCreateContentsUuid());
    }

    private void markBackpackContentsDirty() {
        BackpackStorage.get()
            .markDirty();
    }

    private void migrateBackpackContents(UUID newUuid) {
        migrateNbtTag(newUuid, InventoryHandler.INVENTORY_TAG);

    }

    private void migrateNbtTag(UUID newUuid, String key) {
        NBTHelpers.getCompoundTag(backpack, key)
            .ifPresent(nbt -> {
                BackpackStorage.get()
                    .getOrCreateBackpackContents(newUuid)
                    .setTag(key, nbt);
                markBackpackContentsDirty();
                NBTHelpers.removeTag(backpack, key);
            });
    }

    @Override
    public int getMainColor() {
        return NBTHelpers.getInteger(backpack, CLOTH_COLOR_TAG)
            .orElse(DEFAULT_CLOTH_COLOR);
    }

    @Override
    public int getAccentColor() {
        return NBTHelpers.getInteger(backpack, BORDER_COLOR_TAG)
            .orElse(DEFAULT_BORDER_COLOR);
    }

    @Override
    public void setColors(int mainColor, int accentColor) {
        NBTHelpers.setInteger(backpack, CLOTH_COLOR_TAG, mainColor);
        NBTHelpers.setInteger(backpack, BORDER_COLOR_TAG, accentColor);
        backpackSaveHandler.run();
    }

    @Override
    public void onContentsNbtUpdated() {
        handler = null;
        // upgradeHandler = null;
        // refreshInventoryForUpgradeProcessing();
        onInventoryHandlerRefresh.run();
    }

    @Override
    public void setPersistent(boolean persistent) {
        getInventoryHandler().setPersistent(persistent);
        // getUpgradeHandler().setPersistent(persistent);
    }

    @Override
    public void setSlotNumbers(int numberOfInventorySlots, int numberOfUpgradeSlots) {
        setNumberOfInventorySlots(numberOfInventorySlots);
        // setNumberOfUpgradeSlots(numberOfUpgradeSlots);
    }

    private void setNumberOfInventorySlots(int itemInventorySlots) {
        NBTHelpers.setInteger(backpack, INVENTORY_SLOTS_TAG, itemInventorySlots);
    }

    @Override
    public void registerOnInventoryHandlerRefreshListener(Runnable onInventoryHandlerRefresh) {
        this.onInventoryHandlerRefresh = onInventoryHandlerRefresh;
    }

    @Override
    public void unregisterOnInventoryHandlerRefreshListener() {
        onInventoryHandlerRefresh = () -> {};
    }

    @Override
    public String getDisplayName() {
        return getTierInventoryName();
    }

    public String getTierInventoryName() {
        if (tierId == null) return null;
        return TierRegistry.getDisplayName(tierId);
    }
}
