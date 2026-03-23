package ruiseki.okbackpack.common.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.utils.item.IItemHandler;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;

import lombok.Getter;
import lombok.Setter;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.client.gui.handler.BackpackItemStackHandler;
import ruiseki.okbackpack.client.gui.handler.UpgradeItemStackHandler;
import ruiseki.okbackpack.common.SortType;
import ruiseki.okbackpack.common.init.ModBlocks;
import ruiseki.okbackpack.common.init.ModItems;
import ruiseki.okbackpack.common.item.ItemEverlastingUpgrade;
import ruiseki.okbackpack.common.item.ItemInceptionUpgrade;
import ruiseki.okbackpack.common.item.ItemStackUpgrade;
import ruiseki.okbackpack.common.item.wrapper.IFeedingUpgrade;
import ruiseki.okbackpack.common.item.wrapper.IFilterUpgrade;
import ruiseki.okbackpack.common.item.wrapper.IMagnetUpgrade;
import ruiseki.okbackpack.common.item.wrapper.IPickupUpgrade;
import ruiseki.okbackpack.common.item.wrapper.IVoidUpgrade;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapper;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapperFactory;
import ruiseki.okbackpack.common.network.PacketBackpackNBT;
import ruiseki.okbackpack.config.ModConfig;
import ruiseki.okcore.helper.ItemNBTHelpers;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okcore.persist.nbt.INBTSerializable;

public class BackpackWrapper implements IItemHandlerModifiable, INBTSerializable {

    @Getter
    private final ItemStack backpack;
    @Getter
    private final BackpackItemStackHandler backpackHandler;
    @Getter
    private final UpgradeItemStackHandler upgradeHandler;

    public static final String BACKPACK_NBT = "BackpackNBT";
    public static final String BACKPACK_INV = "BackpackInv";
    public static final String UPGRADE_INV = "UpgradeInv";
    public static final String BACKPACK_SLOTS = "BackpackSlots";
    public static final String UPGRADE_SLOTS = "UpgradeSlots";

    @Getter
    @Setter
    private SortType sortType;

    @Getter
    @Setter
    private boolean lockBackpack;

    @Getter
    @Setter
    private boolean keepTab;

    @Getter
    @Setter
    private String uuid;

    @Getter
    @Setter
    private String customName;

    @Getter
    @Setter
    private boolean sleepingBagDeployed;
    @Getter
    @Setter
    private int sleepingBagX;
    @Getter
    @Setter
    private int sleepingBagY;
    @Getter
    @Setter
    private int sleepingBagZ;

    public static final String MEMORY_STACK_ITEMS_TAG = "MemoryItems";
    public static final String MEMORY_STACK_RESPECT_NBT_TAG = "MemoryRespectNBT";
    public static final String SORT_TYPE_TAG = "SortType";
    public static final String LOCKED_SLOTS_TAG = "LockedSlots";
    public static final String LOCKED_BACKPACK_TAG = "LockedBackpack";
    public static final String UUID_TAG = "UUID";
    public static final String KEEP_TAB_TAG = "KeepTab";
    public static final String CUSTOM_NAME_TAG = "CustomName";
    public static final String SLEEPING_BAG_DEPLOYED_TAG = "SleepingBagDeloyed";
    public static final String SLEEPING_BAG_X = "SleepingBagX";
    public static final String SLEEPING_BAG_Y = "SleepingBagY";
    public static final String SLEEPING_BAG_Z = "SleepingBagZ";

    @Getter
    @Setter
    private int backpackSlots;
    @Getter
    @Setter
    private int upgradeSlots;

    @Getter
    @Setter
    private int mainColor;
    @Getter
    @Setter
    private int accentColor;
    @Getter
    public static final String MAIN_COLOR = "MainColor";
    @Getter
    public static final String ACCENT_COLOR = "AccentColor";
    @Getter
    @Setter
    public Integer slotIndex;
    @Getter
    @Setter
    public InventoryType type;

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
        this.uuid = "";
        this.keepTab = true;
        this.sleepingBagDeployed = false;

        this.backpackHandler = new BackpackItemStackHandler(backpackSlots, this) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                writeToItem();
            }
        };

        this.upgradeHandler = new UpgradeItemStackHandler(upgradeSlots) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                writeToItem();
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

    public boolean feed(EntityPlayer player, IItemHandler handler) {
        for (IFeedingUpgrade upgrade : gatherCapabilityUpgrades(IFeedingUpgrade.class).values()) {
            if (upgrade.feed(player, handler)) {
                return true;
            }
        }
        return false;
    }

    public List<Entity> getMagnetEntities(World world, AxisAlignedBB aabb) {
        Set<Entity> result = new HashSet<>();

        List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, aabb);
        List<EntityXPOrb> xps = world.getEntitiesWithinAABB(EntityXPOrb.class, aabb);

        for (IMagnetUpgrade upgrade : gatherCapabilityUpgrades(IMagnetUpgrade.class).values()) {
            if (upgrade.isCollectItem()) {
                for (EntityItem item : items) {
                    if (upgrade.canCollectItem(item.getEntityItem())) {
                        result.add(item);
                    }
                }
            }
            if (upgrade.isCollectExp()) {
                result.addAll(xps);
            }
        }

        return new ArrayList<>(result);
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
        return !isLockBackpack() || playerUUID.equals(UUID.fromString(getUuid()));
    }

    public boolean hasCustomInventoryName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    // ---------- ITEM STACK ----------
    public NBTTagCompound getTagCompound() {
        return backpack.getTagCompound();
    }

    public void writeToItem() {
        if (backpack == null) {
            return;
        }
        NBTTagCompound tag = ItemNBTHelpers.getNBT(backpack);
        tag.setTag(BACKPACK_NBT, serializeNBT());
        backpack.setTagCompound(tag);
    }

    public void readFromItem() {
        if (backpack == null) {
            return;
        }
        NBTTagCompound tag = ItemNBTHelpers.getNBT(backpack);
        deserializeNBT(tag.getCompoundTag(BACKPACK_NBT));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(BACKPACK_SLOTS, getBackpackSlots());
        tag.setInteger(UPGRADE_SLOTS, getUpgradeSlots());
        tag.setInteger(MAIN_COLOR, getMainColor());
        tag.setInteger(ACCENT_COLOR, getAccentColor());

        tag.setTag(BACKPACK_INV, backpackHandler.serializeNBT());
        tag.setTag(UPGRADE_INV, upgradeHandler.serializeNBT());

        NBTTagCompound memoryTag = new NBTTagCompound();
        BackpackItemStackHelpers.saveAllSlotsExtended(memoryTag, backpackHandler.memorizedSlotStack);
        tag.setTag(MEMORY_STACK_ITEMS_TAG, memoryTag);

        List<Boolean> respectList = backpackHandler.memorizedSlotRespectNbtList;
        byte[] respectBytes = new byte[respectList.size()];
        for (int i = 0; i < respectList.size(); i++) {
            respectBytes[i] = (byte) (respectList.get(i) ? 1 : 0);
        }
        tag.setByteArray(MEMORY_STACK_RESPECT_NBT_TAG, respectBytes);

        List<Boolean> locked = backpackHandler.sortLockedSlots;
        byte[] lockedBytes = new byte[locked.size()];
        for (int i = 0; i < locked.size(); i++) {
            lockedBytes[i] = (byte) (locked.get(i) ? 1 : 0);
        }
        tag.setByteArray(LOCKED_SLOTS_TAG, lockedBytes);

        tag.setByte(SORT_TYPE_TAG, (byte) sortType.ordinal());

        tag.setBoolean(LOCKED_BACKPACK_TAG, lockBackpack);

        tag.setBoolean(KEEP_TAB_TAG, keepTab);

        tag.setString(UUID_TAG, uuid);

        if (hasCustomInventoryName()) {
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
        if (tag.hasKey(BACKPACK_SLOTS)) {
            backpackSlots = tag.getInteger(BACKPACK_SLOTS);
        }
        if (tag.hasKey(UPGRADE_SLOTS)) {
            upgradeSlots = tag.getInteger(UPGRADE_SLOTS);
        }

        if (tag.hasKey(MAIN_COLOR)) {
            mainColor = tag.getInteger(MAIN_COLOR);
        }
        if (tag.hasKey(ACCENT_COLOR)) {
            accentColor = tag.getInteger(ACCENT_COLOR);
        }

        if (tag.hasKey(BACKPACK_INV)) {
            backpackHandler.deserializeNBT(tag.getCompoundTag(BACKPACK_INV));

            BackpackItemStackHelpers
                .loadAllItemsExtended(tag.getCompoundTag(BACKPACK_INV), backpackHandler.getStacks());
            if (backpackHandler.getSlots() != backpackSlots) {
                backpackHandler.resize(backpackSlots);
            }
        }
        if (tag.hasKey(UPGRADE_INV)) {
            upgradeHandler.deserializeNBT(tag.getCompoundTag(UPGRADE_INV));
            if (upgradeHandler.getSlots() != upgradeSlots) {
                upgradeHandler.resize(upgradeSlots);
            }
        }

        if (tag.hasKey(MEMORY_STACK_ITEMS_TAG)) {
            BackpackItemStackHelpers
                .loadAllItemsExtended(tag.getCompoundTag(MEMORY_STACK_ITEMS_TAG), backpackHandler.memorizedSlotStack);
        }

        byte[] respectArr = tag.getByteArray(MEMORY_STACK_RESPECT_NBT_TAG);
        int maxRespect = backpackHandler.memorizedSlotRespectNbtList.size();
        for (int i = 0; i < respectArr.length && i < maxRespect; i++) {
            setMemoryStackRespectNBT(i, respectArr[i] != 0);
        }

        byte[] lockedArr = tag.getByteArray(LOCKED_SLOTS_TAG);
        int maxLocked = backpackHandler.sortLockedSlots.size();
        for (int i = 0; i < lockedArr.length && i < maxLocked; i++) {
            setSlotLocked(i, lockedArr[i] != 0);
        }

        if (tag.hasKey(SORT_TYPE_TAG)) {
            sortType = SortType.values()[tag.getByte(SORT_TYPE_TAG)];
        }

        if (tag.hasKey(LOCKED_BACKPACK_TAG)) {
            lockBackpack = tag.getBoolean(LOCKED_BACKPACK_TAG);
        }

        if (tag.hasKey(KEEP_TAB_TAG)) {
            keepTab = tag.getBoolean(KEEP_TAB_TAG);
        }

        if (tag.hasKey(UUID_TAG)) {
            uuid = tag.getString(UUID_TAG);
        }

        if (tag.hasKey("display", 10)) {
            NBTTagCompound display = tag.getCompoundTag("display");
            if (display.hasKey("Name", 8)) {
                customName = display.getString("Name");
            }
        } else {
            customName = tag.getString(CUSTOM_NAME_TAG);
        }

        if (tag.hasKey(SLEEPING_BAG_DEPLOYED_TAG)) {
            sleepingBagDeployed = tag.getBoolean(SLEEPING_BAG_DEPLOYED_TAG);
        }

        if (tag.hasKey(SLEEPING_BAG_X)) {
            sleepingBagX = tag.getInteger(SLEEPING_BAG_X);
        }

        if (tag.hasKey(SLEEPING_BAG_Y)) {
            sleepingBagY = tag.getInteger(SLEEPING_BAG_Y);
        }

        if (tag.hasKey(SLEEPING_BAG_Z)) {
            sleepingBagZ = tag.getInteger(SLEEPING_BAG_Z);
        }
    }

    public <T> Map<Integer, T> gatherCapabilityUpgrades(Class<T> capabilityClass) {
        Map<Integer, T> result = new HashMap<>();

        for (int i = 0; i < upgradeSlots; i++) {
            ItemStack stack = upgradeHandler.getStackInSlot(i);
            if (stack == null) continue;

            UpgradeWrapper wrapper = UpgradeWrapperFactory.createWrapper(stack);
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
}
