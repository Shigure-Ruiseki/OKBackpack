package ruiseki.okbackpack.api;

import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.client.gui.handler.BackpackItemStackHandler;
import ruiseki.okbackpack.client.gui.handler.UpgradeItemStackHandler;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.item.IItemHandler;
import ruiseki.okcore.item.IItemHandlerModifiable;
import ruiseki.okcore.persist.nbt.INBTSerializable;

public interface IStorageWrapper extends IItemHandlerModifiable, IItemHandler, ITintable, INBTSerializable,
    IMemoryStorage, ILockedStorage, ISetting, ISleepable {

    String BACKPACK_NBT = "BackpackNBT";

    String BACKPACK_INV = "BackpackInv";
    String UPGRADE_INV = "UpgradeInv";
    String BACKPACK_SLOTS = "BackpackSlots";
    String UPGRADE_SLOTS = "UpgradeSlots";

    String TAB_INDEX_TAG = "TabIndex";

    String CUSTOM_NAME_TAG = "CustomName";

    String SORT_TYPE_TAG = "SortType";

    String UUID_TAG = "UUID";
    String LOCKED_BACKPACK_TAG = "LockedBackpack";
    String PLAYER_UUID_TAG = "PlayerUUID";

    String KEEP_TAB_TAG = "KeepTab";

    String SHIFT_CLICK_INTO_OPEN_TAB_TAG = "ShiftClickIntoOpenTab";

    String KEEP_SEARCH_PHRASE_TAG = "KeepSearchPhrase";
    String SEARCH_PHRASE_TAG = "SearchPhrase";

    UpgradeItemStackHandler getUpgradeHandler();

    <T extends BackpackItemStackHandler> T getStackHandler();

    String getDisplayName();

    @Nullable
    ItemStack insertItem(@Nullable ItemStack stack, boolean simulate);

    @Nullable
    ItemStack extractItem(ItemStack wanted, int amount, boolean simulate);

    double applySlotLimitModifiers();

    double applyStackLimitModifiers();

    boolean canAddUpgrade(int slot, ItemStack stack);

    boolean canInsert(int slot, @Nullable ItemStack stack);

    boolean canExtract(int slot, @Nullable ItemStack stack);

    boolean canAddStack(int slot, ItemStack stack);

    boolean canRemoveUpgrade(int slot);

    UpgradeSlotChangeResult getRemoveUpgradeResult(int slot);

    boolean canReplaceUpgrade(int slot, ItemStack replacement);

    UpgradeSlotChangeResult getReplaceUpgradeResult(int slot, ItemStack replacement);

    boolean tick(World world, BlockPos pos);

    void applyContainerEntity(World world, Entity selfEntity);

    <T> Map<Integer, T> gatherCapabilityUpgrades(Class<T> capabilityClass);

    void setSortType(SortType sortType);

    SortType getSortType();

    boolean isKeepTab();

    void setKeepTab(boolean keepTab);

    boolean isLockStorage();

    void setLockStorage(boolean lockStorage);

    boolean isUsePlayerSettings();

    void setUsePlayerSettings(boolean usePlayerSettings);

    boolean isShiftClickIntoOpenTab();

    void setShiftClickIntoOpenTab(boolean shiftClickIntoOpenTab);

    boolean isKeepSearchPhrase();

    void setKeepSearchPhrase(boolean keepSearchPhrase);

    String getSearchPhrase();

    void setSearchPhrase(String searchPhrase);

    String getPlayerUUID();

    void setPlayerUUID(String playerUUID);

    boolean canPlayerAccess(UUID playerUUID);

    int getTabStartIndex();

    void setTabStartIndex(int index);

    boolean isDirty();

    void markDirty();

    void markClean();

    void setInventorySlotChangeHandler(Runnable contentsChangeHandler);
}
