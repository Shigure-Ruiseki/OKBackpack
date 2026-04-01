package ruiseki.okbackpack.common.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;

import baubles.api.BaublesApi;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IFilterUpgrade;
import ruiseki.okbackpack.api.wrapper.IPickupUpgrade;
import ruiseki.okbackpack.api.wrapper.ITickable;
import ruiseki.okbackpack.api.wrapper.IVoidUpgrade;
import ruiseki.okbackpack.api.wrapper.UpgradeWrapperFactory;
import ruiseki.okbackpack.client.gui.handler.BackpackItemStackHandler;
import ruiseki.okbackpack.client.gui.handler.UpgradeItemStackHandler;
import ruiseki.okbackpack.common.SortType;
import ruiseki.okbackpack.common.helpers.BackpackItemStackHelpers;
import ruiseki.okbackpack.common.init.ModBlocks;
import ruiseki.okbackpack.common.init.ModItems;
import ruiseki.okbackpack.common.item.ItemEverlastingUpgrade;
import ruiseki.okbackpack.common.item.ItemInceptionUpgrade;
import ruiseki.okbackpack.common.item.ItemStackUpgrade;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapperBase;
import ruiseki.okbackpack.common.network.PacketBackpackNBT;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okbackpack.config.ModConfig;
import ruiseki.okcore.helper.ItemNBTHelpers;
import ruiseki.okcore.helper.LangHelpers;

public class BackpackWrapper implements IStorageWrapper {

    public ItemStack backpack;
    public final BackpackItemStackHandler backpackHandler;
    public final UpgradeItemStackHandler upgradeHandler;
    public int backpackSlots;
    public int upgradeSlots;
    public int mainColor;
    public int accentColor;
    public SortType sortType;
    public boolean lockBackpack;
    public boolean keepTab;
    public String uuid;
    public String playerUuid;
    public String customName;
    public boolean sleepingBagDeployed;
    public int sleepingBagX;
    public int sleepingBagY;
    public int sleepingBagZ;
    public int slotIndex = -1;
    public InventoryType type = null;
    public boolean isDirty;
    private Runnable markDirtyCallback;

    public static final String BACKPACK_NBT = "BackpackNBT";
    public static final String BACKPACK_INV = "BackpackInv";
    public static final String UPGRADE_INV = "UpgradeInv";
    public static final String BACKPACK_SLOTS = "BackpackSlots";
    public static final String UPGRADE_SLOTS = "UpgradeSlots";
    public static final String MAIN_COLOR = "MainColor";
    public static final String ACCENT_COLOR = "AccentColor";
    public static final String MEMORY_STACK_ITEMS_TAG = "MemoryItems";
    public static final String MEMORY_STACK_RESPECT_NBT_TAG = "MemoryRespectNBT";
    public static final String SORT_TYPE_TAG = "SortType";
    public static final String LOCKED_SLOTS_TAG = "LockedSlots";
    public static final String LOCKED_BACKPACK_TAG = "LockedBackpack";
    public static final String UUID_TAG = "UUID";
    public static final String PLAYER_UUID_TAG = "PlayerUUID";
    public static final String KEEP_TAB_TAG = "KeepTab";
    public static final String CUSTOM_NAME_TAG = "CustomName";
    public static final String SLEEPING_BAG_DEPLOYED_TAG = "SleepingBagDeloyed";
    public static final String SLEEPING_BAG_X = "SleepingBagX";
    public static final String SLEEPING_BAG_Y = "SleepingBagY";
    public static final String SLEEPING_BAG_Z = "SleepingBagZ";

    public BackpackWrapper() {
        this(null, 120, 7);
    }

    public BackpackWrapper(ItemStack backpack) {
        this(backpack, 120, 7);
    }

    public BackpackWrapper(ItemStack backpack, BlockBackpack.ItemBackpack itemBackpack) {
        this(backpack, itemBackpack.getBackpackSlots(), itemBackpack.getUpgradeSlots());
    }

    public BackpackWrapper(ItemStack backpack, BlockBackpack blockBackpack) {
        this(backpack, blockBackpack.getBackpackSlots(), blockBackpack.getUpgradeSlots());
    }

    public BackpackWrapper(ItemStack backpack, int backpackSlots, int upgradeSlots) {
        this.backpack = backpack;
        this.backpackSlots = backpackSlots;
        this.upgradeSlots = upgradeSlots;
        this.mainColor = 0xFFCC613A;
        this.accentColor = 0xFF622E1A;
        this.sortType = SortType.BY_NAME;
        this.lockBackpack = false;
        this.uuid = UUID.randomUUID()
            .toString();
        this.playerUuid = "";
        this.keepTab = true;
        this.sleepingBagDeployed = false;

        this.backpackHandler = new BackpackItemStackHandler(backpackSlots, this) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                markDirty();
            }
        };

        this.upgradeHandler = new UpgradeItemStackHandler(upgradeSlots) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                markDirty();
            }
        };

        readFromItem();
    }

    public String getDisplayName() {
        if (hasCustomInventoryName()) {
            return this.customName;
        }

        if (backpack != null && backpack.getItem() != null) {
            return LangHelpers.localize(
                backpack.getItem()
                    .getUnlocalizedName(backpack) + ".name");
        }

        return LangHelpers.localize("container.inventory");
    }

    @Override
    public int getSlots() {
        return backpackHandler.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return backpackHandler.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int slot, @Nullable ItemStack stack) {
        backpackHandler.setStackInSlot(slot, stack);
    }

    @Override
    public @Nullable ItemStack insertItem(int slot, @Nullable ItemStack stack, boolean simulate) {
        return backpackHandler.prioritizedInsertion(slot, stack, simulate);
    }

    public @Nullable ItemStack insertItem(@Nullable ItemStack stack, boolean simulate) {
        if (stack == null || stack.stackSize <= 0) return null;

        // Void ANY
        if (canVoid(stack, IVoidUpgrade.VoidType.ANY, IVoidUpgrade.VoidInput.ALL)) {
            return simulate ? stack : null;
        }

        // Void Overflow early have max item
        for (int i = 0; i < backpackHandler.getSlots(); i++) {
            ItemStack slotStack = getStackInSlot(i);
            if (slotStack != null && ItemHandlerHelper.canItemStacksStack(slotStack, stack)) {

                int limit = getSlotLimit(i);
                if (slotStack.stackSize >= limit
                    && canVoid(stack, IVoidUpgrade.VoidType.OVERFLOW, IVoidUpgrade.VoidInput.ALL)) {
                    return null;
                }
            }
        }

        ItemStack remaining = stack;

        for (int i = 0; i < backpackHandler.getSlots() && remaining != null; i++) {

            int before = remaining.stackSize;
            remaining = insertItem(i, remaining, simulate);

            boolean changed = (remaining == null) || (remaining.stackSize != before);

            // Void Overflow
            if (changed && remaining != null && remaining.stackSize > 0) {
                if (canVoid(remaining, IVoidUpgrade.VoidType.OVERFLOW, IVoidUpgrade.VoidInput.ALL)) {
                    return simulate ? remaining : null;
                }
            }
        }

        if (remaining != null && remaining.stackSize > 0) {
            if (simulate) {
                return null;
            }
        }

        return remaining;
    }

    @Override
    public @Nullable ItemStack extractItem(int slot, int amount, boolean simulate) {
        return backpackHandler.extractItem(slot, amount, simulate);
    }

    public ItemStack extractItem(ItemStack wanted, int amount, boolean simulate) {
        if (wanted == null || amount <= 0) return null;

        int remaining = amount;
        ItemStack result = null;

        for (int i = 0; i < backpackHandler.getSlots(); i++) {
            ItemStack slotStack = getStackInSlot(i);
            if (slotStack != null && slotStack.isItemEqual(wanted)) {
                int take = Math.min(slotStack.stackSize, remaining);
                ItemStack extracted = extractItem(i, take, simulate);

                if (result == null) {
                    result = extracted;
                } else if (extracted != null) {
                    result.stackSize += extracted.stackSize;
                }

                remaining -= take;
                if (remaining <= 0) break;
            }
        }

        return result;
    }

    @Override
    public int getSlotLimit(int slot) {
        return backpackHandler.getSlotLimit(slot);
    }

    // Setting
    public boolean isSlotMemorized(int slotIndex) {
        return !(backpackHandler.memorizedSlotStack.get(slotIndex) == null);
    }

    public ItemStack getMemorizedStack(int slotIndex) {
        return backpackHandler.memorizedSlotStack.get(slotIndex);
    }

    public void setMemoryStack(int slotIndex, boolean respectNBT) {
        ItemStack currentStack = getStackInSlot(slotIndex);
        if (currentStack == null) return;

        ItemStack copiedStack = currentStack.copy();
        copiedStack.stackSize = 1;
        backpackHandler.memorizedSlotStack.set(slotIndex, copiedStack);
        backpackHandler.memorizedSlotRespectNbtList.set(slotIndex, respectNBT);
    }

    public void unsetMemoryStack(int slotIndex) {
        backpackHandler.memorizedSlotStack.set(slotIndex, null);
        backpackHandler.memorizedSlotRespectNbtList.set(slotIndex, false);
    }

    public boolean isMemoryStackRespectNBT(int slotIndex) {
        return backpackHandler.memorizedSlotRespectNbtList.get(slotIndex);
    }

    public void setMemoryStackRespectNBT(int slotIndex, boolean respect) {
        backpackHandler.memorizedSlotRespectNbtList.set(slotIndex, respect);
    }

    public boolean isSlotLocked(int slotIndex) {
        return backpackHandler.sortLockedSlots.get(slotIndex);
    }

    public void setSlotLocked(int slotIndex, boolean locked) {
        backpackHandler.sortLockedSlots.set(slotIndex, locked);
    }

    public int getTotalStackMultiplier() {
        List<ItemStack> stacks = upgradeHandler.getStacks();
        int result = 0;
        boolean hasstackUpdateOmega = false;

        if (stacks.isEmpty()) {
            return 1;
        }

        for (ItemStack stack : stacks) {
            if (stack == null) {
                continue;
            }
            if (stack.getItem() instanceof ItemStackUpgrade upgrade) {
                result += upgrade.multiplier(stack);
            }
            if (stack.equals(ModItems.STACK_UPGRADE.newItemStack(1, 4))) {
                hasstackUpdateOmega = true;
            }
        }
        if (hasstackUpdateOmega) return ModConfig.stackUpgradeTierOmegaMul;
        else return result == 0 ? 1 : result;
    }

    public int getStackMultiplierExcluding(int excludeSlot, ItemStack replacement) {
        List<ItemStack> stacks = upgradeHandler.getStacks();

        int result = 0;
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            ItemStack stack = stacks.get(i);

            if (i == excludeSlot) {
                if (replacement != null && replacement.getItem() instanceof ItemStackUpgrade up) {
                    result += up.multiplier(replacement);
                }
                continue;
            }

            if (stack == null) {
                continue;
            }

            if (stack.getItem() instanceof ItemStackUpgrade up) {
                result += up.multiplier(stack);
            }
        }
        return result == 0 ? 1 : result;
    }

    public boolean canAddStackUpgrade(int newMultiplier) {
        long result = (long) getTotalStackMultiplier() * 64L * newMultiplier;
        return result == (int) result;
    }

    public boolean canReplaceStackUpgrade(int slotIndex, ItemStack replacement) {
        ItemStack old = upgradeHandler.getStacks()
            .get(slotIndex);

        if (old != null && replacement != null
            && old.getItem() instanceof ItemStackUpgrade oldUp
            && replacement.getItem() instanceof ItemStackUpgrade newUp
            && oldUp.multiplier(old) == newUp.multiplier(replacement)) {
            return true;
        }

        int newStackMultiplier = getStackMultiplierExcluding(slotIndex, replacement);

        for (ItemStack stack : backpackHandler.getStacks()) {
            if (stack == null) continue;

            if (stack.stackSize > stack.getMaxStackSize() * newStackMultiplier) {
                return false;
            }
        }

        return true;
    }

    public boolean canNestBackpack() {
        for (ItemStack stack : upgradeHandler.getStacks()) {
            if (stack != null && stack.getItem() instanceof ItemInceptionUpgrade) {
                return true;
            }
        }
        return false;
    }

    public boolean canRemoveInceptionUpgrade() {
        boolean containsBackpack = false;
        for (ItemStack stack : backpackHandler.getStacks()) {
            if (stack != null && stack.getItem() instanceof BlockBackpack.ItemBackpack) {
                containsBackpack = true;
                break;
            }
        }

        if (!containsBackpack) {
            return true;
        }

        int inceptionCount = 0;
        for (ItemStack stack : upgradeHandler.getStacks()) {
            if (stack != null && stack.getItem() instanceof ItemInceptionUpgrade) {
                inceptionCount++;
            }
        }

        return inceptionCount > 1;
    }

    public boolean tick(EntityPlayer player) {

        Map<Integer, ITickable> gathered = gatherCapabilityUpgrades(ITickable.class);
        if (gathered.isEmpty()) return false;

        boolean dirty = false;

        for (ITickable wrapper : gathered.values()) {
            dirty |= wrapper.tick(player);
        }

        return dirty;
    }

    public boolean canInsert(ItemStack stack) {
        Map<Integer, IFilterUpgrade> upgrades = gatherCapabilityUpgrades(IFilterUpgrade.class);
        if (upgrades.isEmpty()) return true;

        for (IFilterUpgrade upgrade : upgrades.values()) {
            if (upgrade.canInsert(stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean canExtract(ItemStack stack) {
        Map<Integer, IFilterUpgrade> upgrades = gatherCapabilityUpgrades(IFilterUpgrade.class);
        if (upgrades.isEmpty()) return true;

        for (IFilterUpgrade upgrade : upgrades.values()) {
            if (upgrade.canExtract(stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean canPickupItem(ItemStack stack) {

        for (IPickupUpgrade upgrade : gatherCapabilityUpgrades(IPickupUpgrade.class).values()) {
            if (upgrade.canPickup(stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean canVoid(ItemStack newStack, IVoidUpgrade.VoidType type, IVoidUpgrade.VoidInput input) {

        for (IVoidUpgrade upgrade : gatherCapabilityUpgrades(IVoidUpgrade.class).values()) {
            if (upgrade.canVoid(newStack) && upgrade.getVoidType() == type
                && (upgrade.getVoidInput() == input || input == IVoidUpgrade.VoidInput.AUTOMATION)) {
                return true;
            }
        }
        return false;
    }

    public boolean canImportant() {

        for (ItemStack stack : upgradeHandler.getStacks()) {
            if (stack != null && stack.getItem() instanceof ItemEverlastingUpgrade) {
                return true;
            }
        }
        return false;
    }

    public boolean canPlayerAccess(UUID playerUUID) {
        if (!lockBackpack) return true;
        if (playerUUID == null || playerUuid == null || playerUuid.isEmpty()) return false;
        return playerUUID.equals(UUID.fromString(playerUuid));
    }

    public boolean hasCustomInventoryName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    // ---------- ITEM STACK ----------
    public NBTTagCompound getTagCompound() {
        if (backpack == null) {
            return null;
        }
        return backpack.getTagCompound();
    }

    public ItemStack findActualStack(EntityPlayer player) {
        if (player == null || uuid == null) return backpack;

        // Check held item first (fastest)
        ItemStack held = player.getHeldItem();
        if (isSameBackpack(held)) return held;

        // Check player inventory
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (isSameBackpack(stack)) return stack;
        }

        // Check Baubles if loaded
        if (Mods.Baubles.isLoaded()) {
            IInventory baubles = BaublesApi.getBaubles(player);
            if (baubles != null) {
                for (int i = 0; i < baubles.getSizeInventory(); i++) {
                    ItemStack stack = baubles.getStackInSlot(i);
                    if (isSameBackpack(stack)) return stack;
                }
            }
        }
        return backpack; // Fallback
    }

    private boolean isSameBackpack(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof BlockBackpack.ItemBackpack)) return false;
        NBTTagCompound tag = stack.getTagCompound();
        NBTTagCompound backpackTag = tag.getCompoundTag(BACKPACK_NBT);
        return backpackTag != null && uuid.equals(backpackTag.getString(UUID_TAG));
    }

    public void writeToItem() {
        if (backpack == null) return;

        NBTTagCompound root = backpack.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
            backpack.setTagCompound(root);
        }

        root.setTag(BACKPACK_NBT, serializeNBT());
    }

    public void writeToItem(EntityPlayer player) {
        this.backpack = findActualStack(player);
        writeToItem();
    }

    public void readFromItem() {
        if (backpack == null) return;
        NBTTagCompound tag = ItemNBTHelpers.getNBT(backpack);
        if (tag.hasKey(BACKPACK_NBT)) deserializeNBT(tag.getCompoundTag(BACKPACK_NBT));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        if (backpackHandler.isSizeInconsistent(backpackSlots)) {
            backpackHandler.resize(backpackSlots);
        }
        if (upgradeHandler.isSizeInconsistent(upgradeSlots)) {
            upgradeHandler.resize(upgradeSlots);
        }

        tag.setInteger(BACKPACK_SLOTS, backpackSlots);
        tag.setInteger(UPGRADE_SLOTS, upgradeSlots);
        tag.setInteger(MAIN_COLOR, getMainColor());
        tag.setInteger(ACCENT_COLOR, getAccentColor());

        tag.setTag(BACKPACK_INV, backpackHandler.serializeNBT());
        tag.setTag(UPGRADE_INV, upgradeHandler.serializeNBT());

        NBTTagCompound memoryTag = new NBTTagCompound();
        BackpackItemStackHelpers.saveAllSlotsExtended(memoryTag, backpackHandler.memorizedSlotStack);
        tag.setTag(MEMORY_STACK_ITEMS_TAG, memoryTag);

        List<Boolean> respectList = backpackHandler.memorizedSlotRespectNbtList;
        byte[] respectBytes = new byte[backpackSlots];
        for (int i = 0; i < backpackSlots; i++) {
            boolean val = i < respectList.size() && respectList.get(i);
            respectBytes[i] = (byte) (val ? 1 : 0);
        }
        tag.setByteArray(MEMORY_STACK_RESPECT_NBT_TAG, respectBytes);

        List<Boolean> locked = backpackHandler.sortLockedSlots;
        byte[] lockedBytes = new byte[backpackSlots];
        for (int i = 0; i < backpackSlots; i++) {
            boolean val = i < locked.size() && locked.get(i);
            lockedBytes[i] = (byte) (val ? 1 : 0);
        }
        tag.setByteArray(LOCKED_SLOTS_TAG, lockedBytes);

        tag.setByte(SORT_TYPE_TAG, (byte) sortType.ordinal());

        tag.setBoolean(LOCKED_BACKPACK_TAG, lockBackpack);

        tag.setBoolean(KEEP_TAB_TAG, keepTab);

        tag.setString(UUID_TAG, uuid);

        if (lockBackpack && playerUuid != null) {
            tag.setString(PLAYER_UUID_TAG, playerUuid);
        }

        if (hasCustomInventoryName() && this.customName != null) {
            tag.setString(CUSTOM_NAME_TAG, this.customName);
        }

        tag.setBoolean(SLEEPING_BAG_DEPLOYED_TAG, sleepingBagDeployed);
        tag.setInteger(SLEEPING_BAG_X, sleepingBagX);
        tag.setInteger(SLEEPING_BAG_Y, sleepingBagY);
        tag.setInteger(SLEEPING_BAG_Z, sleepingBagZ);

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        if (tag == null) return;
        if (tag.hasKey(BACKPACK_SLOTS, 3)) {
            this.backpackSlots = tag.getInteger(BACKPACK_SLOTS);
        }
        if (tag.hasKey(UPGRADE_SLOTS, 3)) {
            this.upgradeSlots = tag.getInteger(UPGRADE_SLOTS);
        }

        if (tag.hasKey(MAIN_COLOR, 3)) this.mainColor = tag.getInteger(MAIN_COLOR);
        if (tag.hasKey(ACCENT_COLOR, 3)) this.accentColor = tag.getInteger(ACCENT_COLOR);

        if (tag.hasKey(BACKPACK_INV, 10)) {
            backpackHandler.deserializeNBT(tag.getCompoundTag(BACKPACK_INV));

            if (backpackHandler.isSizeInconsistent(this.backpackSlots)) {
                backpackHandler.resize(this.backpackSlots);
            }

            BackpackItemStackHelpers
                .loadAllItemsExtended(tag.getCompoundTag(BACKPACK_INV), backpackHandler.getStacks());
        }

        if (tag.hasKey(MEMORY_STACK_ITEMS_TAG, 10)) {
            BackpackItemStackHelpers
                .loadAllItemsExtended(tag.getCompoundTag(MEMORY_STACK_ITEMS_TAG), backpackHandler.memorizedSlotStack);
        }

        if (tag.hasKey(MEMORY_STACK_RESPECT_NBT_TAG, 7)) {
            byte[] respectArr = tag.getByteArray(MEMORY_STACK_RESPECT_NBT_TAG);
            for (int i = 0; i < respectArr.length && i < this.backpackSlots; i++) {
                setMemoryStackRespectNBT(i, respectArr[i] != 0);
            }
        }

        if (tag.hasKey(LOCKED_SLOTS_TAG, 7)) {
            byte[] lockedArr = tag.getByteArray(LOCKED_SLOTS_TAG);
            for (int i = 0; i < lockedArr.length && i < this.backpackSlots; i++) {
                setSlotLocked(i, lockedArr[i] != 0);
            }
        }

        if (tag.hasKey(UPGRADE_INV, 10)) {
            upgradeHandler.deserializeNBT(tag.getCompoundTag(UPGRADE_INV));
            if (upgradeHandler.isSizeInconsistent(this.upgradeSlots)) {
                upgradeHandler.resize(this.upgradeSlots);
            }
        }

        if (tag.hasKey(SORT_TYPE_TAG, 1)) {
            byte type = tag.getByte(SORT_TYPE_TAG);
            if (type >= 0 && type < SortType.values().length) {
                this.sortType = SortType.values()[type];
            }
        }

        if (tag.hasKey(LOCKED_BACKPACK_TAG, 1)) this.lockBackpack = tag.getBoolean(LOCKED_BACKPACK_TAG);
        if (tag.hasKey(KEEP_TAB_TAG, 1)) this.keepTab = tag.getBoolean(KEEP_TAB_TAG);

        if (tag.hasKey(UUID_TAG, 8)) {
            this.uuid = tag.getString(UUID_TAG);
        }

        if (tag.hasKey(PLAYER_UUID_TAG, 8)) {
            this.playerUuid = tag.getString(PLAYER_UUID_TAG);
        }

        if (tag.hasKey("display", 10)) {
            NBTTagCompound display = tag.getCompoundTag("display");
            if (display.hasKey("Name", 8)) this.customName = display.getString("Name");
        } else if (tag.hasKey(CUSTOM_NAME_TAG, 8)) {
            this.customName = tag.getString(CUSTOM_NAME_TAG);
        }

        if (tag.hasKey(SLEEPING_BAG_DEPLOYED_TAG, 1))
            this.sleepingBagDeployed = tag.getBoolean(SLEEPING_BAG_DEPLOYED_TAG);
        if (tag.hasKey(SLEEPING_BAG_X, 3)) this.sleepingBagX = tag.getInteger(SLEEPING_BAG_X);
        if (tag.hasKey(SLEEPING_BAG_Y, 3)) this.sleepingBagY = tag.getInteger(SLEEPING_BAG_Y);
        if (tag.hasKey(SLEEPING_BAG_Z, 3)) this.sleepingBagZ = tag.getInteger(SLEEPING_BAG_Z);
    }

    public <T> Map<Integer, T> gatherCapabilityUpgrades(Class<T> capabilityClass) {
        Map<Integer, T> result = new HashMap<>();

        for (int i = 0; i < upgradeSlots; i++) {
            ItemStack stack = upgradeHandler.getStackInSlot(i);
            if (stack == null) continue;

            UpgradeWrapperBase wrapper = UpgradeWrapperFactory.createWrapper(stack, this);
            if (wrapper == null) continue;

            if (capabilityClass.isAssignableFrom(wrapper.getClass())) {
                result.put(i, capabilityClass.cast(wrapper));
            }
        }

        return result;
    }

    public void syncToServer() {
        writeToItem();
        if (type != null) {
            OKBackpack.instance.getPacketHandler()
                .sendToServer(new PacketBackpackNBT(slotIndex, getTagCompound(), type));
        }
    }

    public boolean deploySleepingBag(EntityPlayer player, World world, int meta, int cX, int cY, int cZ) {
        if (world.isRemote) return false;

        if (sleepingBagDeployed) removeSleepingBag(world);

        sleepingBagDeployed = BlockSleepingBag.spawnSleepingBag(player, world, meta, cX, cY, cZ);
        if (sleepingBagDeployed) {
            sleepingBagX = cX;
            sleepingBagY = cY;
            sleepingBagZ = cZ;
            writeToItem();
        }
        return sleepingBagDeployed;
    }

    public void removeSleepingBag(World world) {
        if (this.sleepingBagDeployed) {
            if (world.getBlock(sleepingBagX, sleepingBagY, sleepingBagZ) == ModBlocks.SLEEPING_BAG.getBlock())
                world.func_147480_a(sleepingBagX, sleepingBagY, sleepingBagZ, false);
        }
        this.sleepingBagDeployed = false;
        writeToItem();
    }

    public void markDirty() {
        this.isDirty = true;
        if (markDirtyCallback != null) {
            markDirtyCallback.run();
        }
    }

    public void setMarkDirtyCallback(Runnable callback) {
        this.markDirtyCallback = callback;
    }

    public void clearDirty() {
        this.isDirty = false;
    }

    @Override
    public int getAccentColor() {
        return accentColor;
    }

    @Override
    public int getMainColor() {
        return mainColor;
    }

    @Override
    public void setColors(int mainColor, int accentColor) {
        this.mainColor = mainColor;
        this.accentColor = accentColor;
    }
}
