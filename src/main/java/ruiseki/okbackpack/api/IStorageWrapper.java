package ruiseki.okbackpack.api;

import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.utils.item.IItemHandler;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;

import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;
import ruiseki.okbackpack.client.gui.handler.UpgradeItemStackHandler;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.persist.nbt.INBTSerializable;

public interface IStorageWrapper
    extends IItemHandlerModifiable, IItemHandler, ITintable, INBTSerializable, IMemoryStorage, ILockedStorage {

    UpgradeItemStackHandler getUpgradeHandler();

    <T extends BaseItemStackHandler> T getStackHandler();

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

    String getPlayerUUID();

    void setPlayerUUID(String playerUUID);

    int getTabStartIndex();

    void setTabStartIndex(int index);

    boolean isDirty();

    void markDirty();

    void markClean();

    void setInventorySlotChangeHandler(Runnable contentsChangeHandler);
}
