package ruiseki.storagecore.upgrades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.datastructure.NonNullList;
import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.MinecraftHelpers;
import ruiseki.okcore.item.handler.ItemStackHandler;
import ruiseki.storagecore.api.IStorageWrapper;

public class UpgradeHandler extends ItemStackHandler {

    public static final String UPGRADE_INVENTORY_TAG = "upgradeInventory";
    private final IStorageWrapper storageWrapper;
    private final Runnable contentsSaveHandler;
    private final Runnable onInvalidateUpgradeCaches;
    private final NBTTagCompound contentsNbt;
    @Nullable
    private Runnable refreshCallBack = null;
    private final Map<Integer, IUpgradeWrapper> slotWrappers = new LinkedHashMap<>();
    private final Map<UpgradeType<? extends IUpgradeWrapper>, List<? extends IUpgradeWrapper>> typeWrappers = new HashMap<>();
    private boolean justSavingNbtChange = false;
    private boolean wrappersInitialized = false;
    private boolean typeWrappersInitialized = false;
    @Nullable
    private IUpgradeWrapperAccessor wrapperAccessor = null;
    private final Map<Class<? extends IUpgradeWrapper>, Consumer<? extends IUpgradeWrapper>> upgradeDefaultsHandlers = new HashMap<>();

    public UpgradeHandler(int numberOfUpgradeSlots, IStorageWrapper storageWrapper, NBTTagCompound contentsNbt,
        Runnable contentsSaveHandler, Runnable onInvalidateUpgradeCaches) {
        super(numberOfUpgradeSlots);
        this.contentsNbt = contentsNbt;
        this.storageWrapper = storageWrapper;
        this.contentsSaveHandler = contentsSaveHandler;
        this.onInvalidateUpgradeCaches = onInvalidateUpgradeCaches;
        deserializeNBT(contentsNbt.getCompoundTag(UPGRADE_INVENTORY_TAG));
    }

    public <T extends IUpgradeWrapper> void registerUpgradeDefaultsHandler(Class<T> upgradeClass,
        Consumer<T> defaultsHandler) {
        upgradeDefaultsHandlers.put(upgradeClass, defaultsHandler);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return ItemHelpers.isEmpty(stack) || stack.getItem() instanceof IUpgradeItem;
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        saveInventory();
        contentsSaveHandler.run();
        if (!justSavingNbtChange) {
            refreshUpgradeWrappers();
        }
    }

    @Override
    public void setSize(int size) {
        super.setSize(stacks.size());
    }

    public void saveInventory() {
        contentsNbt.setTag(UPGRADE_INVENTORY_TAG, serializeNBT());
    }

    public void setRefreshCallBack(Runnable refreshCallBack) {
        this.refreshCallBack = refreshCallBack;
    }

    public void removeRefreshCallback() {
        refreshCallBack = null;
    }

    private void initializeWrappers() {
        if (wrappersInitialized) {
            return;
        }
        wrappersInitialized = true;
        slotWrappers.clear();
        typeWrappers.clear();
        if (wrapperAccessor != null) {
            wrapperAccessor.clearCache();
        }

        InventoryHelpers.iterate(this, (slot, upgrade) -> {
            if (ItemHelpers.isEmpty(upgrade) || !(upgrade.getItem() instanceof IUpgradeItem<?>)) {
                return;
            }
            UpgradeType<?> type = ((IUpgradeItem<?>) upgrade.getItem()).getType();
            IUpgradeWrapper wrapper = type.create(storageWrapper, upgrade, upgradeStack -> {
                justSavingNbtChange = true;
                setStackInSlot(slot, upgradeStack);
                justSavingNbtChange = false;
            });
            setUpgradeDefaults(wrapper);
            slotWrappers.put(slot, wrapper);
        });
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        ItemStack result = super.insertItem(slot, stack, simulate);
        if (MinecraftHelpers.isServerSide() && ItemHelpers.isEmpty(result) && !ItemHelpers.isEmpty(stack)) {
            onUpgradeAdded(slot);
        }
        return result;
    }

    private void onUpgradeAdded(int slot) {
        Map<Integer, IUpgradeWrapper> wrappers = getSlotWrappers();
        if (wrappers.containsKey(slot)) {
            wrappers.get(slot)
                .onAdded();
        }
    }

    private void setUpgradeDefaults(IUpgradeWrapper wrapper) {
        getUpgradeDefaultsHandler(wrapper).accept(wrapper);
    }

    private <T extends IUpgradeWrapper> Consumer<T> getUpgradeDefaultsHandler(T wrapper) {
        // noinspection unchecked
        return (Consumer<T>) upgradeDefaultsHandlers.getOrDefault(wrapper.getClass(), w -> {});
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        ItemStack originalStack = getStackInSlot(slot);
        Map<Integer, IUpgradeWrapper> wrappers = getSlotWrappers();
        boolean itemsDiffer = !ItemHandlerHelpers.canItemStacksStack(originalStack, stack);
        if (MinecraftHelpers.isServerSide() && itemsDiffer && wrappers.containsKey(slot)) {
            wrappers.get(slot)
                .onBeforeRemoved();
        }

        super.setStackInSlot(slot, stack);

        if (MinecraftHelpers.isServerSide() && itemsDiffer) {
            onUpgradeAdded(slot);
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!simulate && MinecraftHelpers.isServerSide()) {
            ItemStack slotStack = getStackInSlot(slot);
            if (!ItemHelpers.isEmpty(slotStack) && amount == 1) {
                Map<Integer, IUpgradeWrapper> wrappers = getSlotWrappers();
                if (wrappers.containsKey(slot)) {
                    wrappers.get(slot)
                        .onBeforeRemoved();
                }
            }
        }
        return super.extractItem(slot, amount, simulate);
    }

    private void initializeTypeWrappers() {
        if (typeWrappersInitialized) {
            return;
        }
        initializeWrappers();
        typeWrappersInitialized = true;

        typeWrappers.clear();
        slotWrappers.values()
            .forEach(wrapper -> {
                if (wrapper.getUpgradeStack()
                    .getItem() instanceof IUpgradeItem<?>upgradeItem) {
                    UpgradeType<?> type = upgradeItem.getType();
                    if (wrapper.isEnabled()) {
                        addTypeWrapper(type, wrapper);
                    }
                }
            });
    }

    private <T extends IUpgradeWrapper> void addTypeWrapper(UpgradeType<?> type, T wrapper) {
        // noinspection unchecked
        ((List<T>) typeWrappers.computeIfAbsent(type, t -> new ArrayList<>())).add(wrapper);
    }

    public <T extends IUpgradeWrapper> List<T> getTypeWrappers(UpgradeType<T> type) {
        initializeTypeWrappers();
        // noinspection unchecked
        return (List<T>) typeWrappers.getOrDefault(type, Collections.emptyList());
    }

    public <T extends IUpgradeWrapper> boolean hasUpgrade(UpgradeType<T> type) {
        return !getTypeWrappers(type).isEmpty();
    }

    public <T> List<T> getWrappersThatImplement(Class<T> upgradeClass) {
        initializeWrappers();
        return getWrapperAccessor().getWrappersThatImplement(upgradeClass);
    }

    private IUpgradeWrapperAccessor getWrapperAccessor() {
        if (wrapperAccessor == null) {
            IUpgradeWrapperAccessor accessor = new Accessor(this);
            for (IUpgradeAccessModifier upgrade : getListOfWrappersThatImplement(IUpgradeAccessModifier.class)) {
                accessor = upgrade.wrapAccessor(accessor);
            }
            wrapperAccessor = accessor;
        }
        return wrapperAccessor;
    }

    public <T> List<T> getWrappersThatImplementFromMainStorage(Class<T> upgradeClass) {
        initializeWrappers();
        return getWrapperAccessor().getWrappersThatImplementFromMainStorage(upgradeClass);
    }

    public <T> List<T> getListOfWrappersThatImplement(Class<T> uc) {
        List<T> ret = new ArrayList<>();
        for (IUpgradeWrapper wrapper : slotWrappers.values()) {
            if (wrapper.isEnabled() && uc.isInstance(wrapper)) {
                // noinspection unchecked
                ret.add((T) wrapper);
            }
        }
        return ret;
    }

    public Map<Integer, IUpgradeWrapper> getSlotWrappers() {
        initializeWrappers();
        return slotWrappers;
    }

    public IUpgradeWrapper getSlotWrapper(int index) {
        return getSlotWrappers().get(index);
    }

    public void copyTo(UpgradeHandler otherHandler) {
        InventoryHelpers.copyTo(this, otherHandler);
    }

    public void refreshWrappersThatImplementAndTypeWrappers() {
        typeWrappersInitialized = false;
        if (wrapperAccessor != null) {
            wrapperAccessor.clearCache();
        }
        if (refreshCallBack != null) {
            refreshCallBack.run();
        }
    }

    public void refreshUpgradeWrappers() {
        wrappersInitialized = false;
        typeWrappersInitialized = false;
        if (wrapperAccessor != null) {
            wrapperAccessor.onBeforeDeconstruct();
            wrapperAccessor = null;
        }
        if (refreshCallBack != null) {
            refreshCallBack.run();
        }
        onInvalidateUpgradeCaches.run();
    }

    public void increaseSize(int diff) {
        List<ItemStack> previousStacks = stacks;
        stacks = NonNullList.withSize(previousStacks.size() + diff, ItemHelpers.EMPTY);
        for (int slot = 0; slot < previousStacks.size(); slot++) {
            stacks.set(slot, previousStacks.get(slot));
        }
        saveInventory();
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    private static class Accessor implements IUpgradeWrapperAccessor {

        private final Map<Class<?>, List<?>> interfaceWrappers = new HashMap<>();

        private final UpgradeHandler upgradeHandler;

        public Accessor(UpgradeHandler upgradeHandler) {
            this.upgradeHandler = upgradeHandler;
        }

        @Override
        public <T> List<T> getWrappersThatImplement(Class<T> upgradeClass) {
            // noinspection unchecked
            return (List<T>) interfaceWrappers
                .computeIfAbsent(upgradeClass, upgradeHandler::getListOfWrappersThatImplement);
        }

        @Override
        public <T> List<T> getWrappersThatImplementFromMainStorage(Class<T> upgradeClass) {
            // noinspection unchecked
            return (List<T>) interfaceWrappers
                .computeIfAbsent(upgradeClass, upgradeHandler::getListOfWrappersThatImplement);
        }

        @Override
        public void clearCache() {
            interfaceWrappers.clear();
        }
    }
}
