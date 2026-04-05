package ruiseki.okbackpack.common.block;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;

import baubles.api.BaublesApi;
import cpw.mods.fml.common.network.NetworkRegistry;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.IBackpackWrapper;
import ruiseki.okbackpack.api.wrapper.IEntityApplicable;
import ruiseki.okbackpack.api.wrapper.IFilterUpgrade;
import ruiseki.okbackpack.api.wrapper.IInventoryModifiable;
import ruiseki.okbackpack.api.wrapper.IJukeboxUpgrade;
import ruiseki.okbackpack.api.wrapper.IPickupUpgrade;
import ruiseki.okbackpack.api.wrapper.ISlotModifiable;
import ruiseki.okbackpack.api.wrapper.ISmeltingUpgrade;
import ruiseki.okbackpack.api.wrapper.ITickable;
import ruiseki.okbackpack.api.wrapper.IToggleable;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.handler.BackpackItemStackHandler;
import ruiseki.okbackpack.client.gui.handler.UpgradeItemStackHandler;
import ruiseki.okbackpack.common.SortType;
import ruiseki.okbackpack.common.helpers.BackpackItemStackHelpers;
import ruiseki.okbackpack.common.init.ModBlocks;
import ruiseki.okbackpack.common.network.PacketJukeboxPlaybackState;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;
import ruiseki.okcore.helper.LangHelpers;

public class BackpackWrapper implements IBackpackWrapper {

    public ItemStack backpack;
    public final TileEntity tile;

    public final BackpackItemStackHandler backpackHandler;
    public UpgradeItemStackHandler upgradeHandler;
    public int backpackSlots;
    public int upgradeSlots;

    public int mainColor;
    public int accentColor;

    public SortType sortType;

    public boolean lockBackpack;
    public String playerUuid;
    public boolean keepTab;

    public String customName;

    public boolean isDirty;

    private Runnable onInventoryHandlerRefresh = () -> {};

    public String uuid;

    public boolean sleepingBagDeployed;
    public int sleepingBagX;
    public int sleepingBagY;
    public int sleepingBagZ;

    private final Set<Integer> pendingJukeboxStops = new HashSet<>();

    public int slotIndex = -1;
    public InventoryType type = null;

    public static final String BACKPACK_NBT = "BackpackNBT";

    public static final String BACKPACK_INV = "BackpackInv";
    public static final String UPGRADE_INV = "UpgradeInv";
    public static final String BACKPACK_SLOTS = "BackpackSlots";
    public static final String UPGRADE_SLOTS = "UpgradeSlots";
    public static final String MEMORY_STACK_ITEMS_TAG = "MemoryItems";
    public static final String MEMORY_STACK_RESPECT_NBT_TAG = "MemoryRespectNBT";
    public static final String LOCKED_SLOTS_TAG = "LockedSlots";

    public static final String MAIN_COLOR = "MainColor";
    public static final String ACCENT_COLOR = "AccentColor";

    public static final String SORT_TYPE_TAG = "SortType";

    public static final String UUID_TAG = "UUID";

    public static final String LOCKED_BACKPACK_TAG = "LockedBackpack";
    public static final String PLAYER_UUID_TAG = "PlayerUUID";

    public static final String KEEP_TAB_TAG = "KeepTab";

    public static final String CUSTOM_NAME_TAG = "CustomName";

    public static final String SLEEPING_BAG_DEPLOYED_TAG = "SleepingBagDeloyed";
    public static final String SLEEPING_BAG_X = "SleepingBagX";
    public static final String SLEEPING_BAG_Y = "SleepingBagY";
    public static final String SLEEPING_BAG_Z = "SleepingBagZ";

    public BackpackWrapper() {
        this(null, null, 120, 7);
    }

    public BackpackWrapper(TileEntity tile) {
        this(null, tile, 120, 7);
    }

    public BackpackWrapper(ItemStack backpack) {
        this(backpack, null, 120, 7);
    }

    public BackpackWrapper(ItemStack backpack, TileEntity tile) {
        this(backpack, tile, 120, 7);
    }

    public BackpackWrapper(ItemStack backpack, BlockBackpack.ItemBackpack item) {
        this(backpack, null, item.backpackSlots, item.upgradeSlots);
    }

    public BackpackWrapper(TileEntity tile, int backpackSlots, int upgradeSlots) {
        this(null, tile, backpackSlots, upgradeSlots);
    }

    public BackpackWrapper(ItemStack backpack, int backpackSlots, int upgradeSlots) {
        this(backpack, null, backpackSlots, upgradeSlots);
    }

    public BackpackWrapper(ItemStack backpack, BlockBackpack blockBackpack, TileEntity tile) {
        this(backpack, tile, blockBackpack.getBackpackSlots(), blockBackpack.getUpgradeSlots());
    }

    public BackpackWrapper(ItemStack backpack, TileEntity tile, int backpackSlots, int upgradeSlots) {
        this.backpack = backpack;
        this.tile = tile;
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

        this.upgradeHandler = new UpgradeItemStackHandler(upgradeSlots, this) {

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                detectPlayingJukeboxRemoval(slot);
                super.setStackInSlot(slot, stack);
            }

            private void detectPlayingJukeboxRemoval(int slot) {
                ItemStack existing = getStackInSlot(slot);
                if (existing != null && ItemNBTHelpers.getBoolean(existing, IJukeboxUpgrade.PLAYING_TAG, false)) {
                    pendingJukeboxStops.add(slot);
                }
            }

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                markDirty();
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                ItemStack extracted = super.extractItem(slot, amount, simulate);
                if (!simulate) {
                    detectPlayingJukeboxRemoval(slot);
                }
                if (!simulate && extracted != null) {
                    NBTTagCompound tag = extracted.getTagCompound();
                    if (tag != null && tag.hasKey(ISmeltingUpgrade.COOK_TIME_TAG)) {
                        tag.removeTag(ISmeltingUpgrade.COOK_TIME_TAG);
                        tag.removeTag(ISmeltingUpgrade.BURN_TIME_TAG);
                        tag.removeTag(ISmeltingUpgrade.BURN_TIME_TOTAL_TAG);
                    }
                }
                return extracted;
            }
        };

        readFromItem();
    }

    @Override
    public UpgradeItemStackHandler getUpgradeHandler() {
        return upgradeHandler;
    }

    @Override
    public String getDisplayName() {
        if (hasCustomInventoryName()) {
            return this.customName;
        }

        if (backpack != null && backpack.getItem() != null) {
            return LangHelpers.localize(
                backpack.getItem()
                    .getUnlocalizedName(backpack) + ".name");
        }

        if (tile != null && tile.getWorldObj() != null) {
            Block block = tile.getWorldObj()
                .getBlock(tile.xCoord, tile.yCoord, tile.zCoord);
            if (block != null) {
                return LangHelpers.localize(block.getUnlocalizedName() + ".name");
            }
        }

        return LangHelpers.localize("container.inventory");
    }

    @Override
    public int getSlots() {
        return backpackHandler.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        ItemStack stack = backpackHandler.getStackInSlot(slot);

        Map<Integer, IInventoryModifiable> mods = gatherCapabilityUpgrades(IInventoryModifiable.class);
        for (IInventoryModifiable mod : mods.values()) {
            stack = mod.onGet(slot, stack);
        }

        return stack;
    }

    @Override
    public void setStackInSlot(int slot, @Nullable ItemStack stack) {
        Map<Integer, IInventoryModifiable> mods = gatherCapabilityUpgrades(IInventoryModifiable.class);
        for (IInventoryModifiable mod : mods.values()) {
            stack = mod.onSet(slot, stack);
        }

        backpackHandler.setStackInSlot(slot, stack);
    }

    @Override
    public @Nullable ItemStack insertItem(int slot, @Nullable ItemStack stack, boolean simulate) {

        // Apply slot-level modifications
        Map<Integer, IInventoryModifiable> mods = gatherCapabilityUpgrades(IInventoryModifiable.class);
        for (IInventoryModifiable mod : mods.values()) {
            stack = mod.onInsert(slot, stack, simulate);
            if (stack == null) return null;
        }

        return backpackHandler.prioritizedInsertion(slot, stack, simulate);
    }

    @Override
    public @Nullable ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack extracted = backpackHandler.extractItem(slot, amount, simulate);
        if (extracted == null) return null;

        // Apply IInventoryModifiable wrappers
        Map<Integer, IInventoryModifiable> mods = gatherCapabilityUpgrades(IInventoryModifiable.class);
        for (IInventoryModifiable mod : mods.values()) {
            extracted = mod.onExtract(slot, extracted, simulate);
            if (extracted == null) return null; // cancel extraction
        }

        return extracted;
    }

    @Override
    public @Nullable ItemStack insertItem(@Nullable ItemStack stack, boolean simulate) {
        if (stack == null || stack.stackSize <= 0) return null;

        ItemStack remaining = ItemHandlerHelper.copyStackWithSize(stack, stack.stackSize);

        for (int i = 0; i < backpackHandler.getSlots() && remaining != null; i++) {
            remaining = insertItem(i, remaining, simulate);
        }

        return remaining;
    }

    @Override
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
    @Override
    public boolean isSlotMemorized(int slotIndex) {
        return backpackHandler.isSlotMemorized(slotIndex);
    }

    @Override
    public ItemStack getMemoryStack(int slotIndex) {
        return backpackHandler.getMemoryStack(slotIndex);
    }

    @Override
    public void setMemoryStack(int slotIndex, boolean respectNBT) {
        ItemStack currentStack = getStackInSlot(slotIndex);
        if (currentStack == null) return;

        ItemStack copiedStack = currentStack.copy();
        copiedStack.stackSize = 1;
        backpackHandler.setMemoryStack(slotIndex, copiedStack);
        backpackHandler.setRespectNBT(slotIndex, respectNBT);
    }

    @Override
    public void unsetMemoryStack(int slotIndex) {
        backpackHandler.setMemoryStack(slotIndex, null);
        backpackHandler.setRespectNBT(slotIndex, false);
    }

    @Override
    public boolean isMemoryStackRespectNBT(int slotIndex) {
        return backpackHandler.isRespectNBT(slotIndex);
    }

    @Override
    public void setMemoryStackRespectNBT(int slotIndex, boolean respect) {
        backpackHandler.setRespectNBT(slotIndex, respect);
    }

    @Override
    public boolean isSlotLocked(int slotIndex) {
        return backpackHandler.isSlotLocked(slotIndex);
    }

    @Override
    public void setSlotLocked(int slotIndex, boolean locked) {
        backpackHandler.setSlotLocked(slotIndex, locked);
    }

    @Override
    public int applyStackLimitModifiers(int original, int slot, ItemStack stack) {
        int result = original;

        Map<Integer, ISlotModifiable> gathered = gatherCapabilityUpgrades(ISlotModifiable.class);
        if (gathered.isEmpty()) return result;

        for (ISlotModifiable mod : gathered.values()) {
            result = mod.modifyStackLimit(result, slot, stack);
        }

        if (result != original) {
            return result - original;
        }

        return original;
    }

    @Override
    public int applySlotLimitModifiers(int original, int slot) {
        int result = original;

        Map<Integer, ISlotModifiable> gathered = gatherCapabilityUpgrades(ISlotModifiable.class);
        if (gathered.isEmpty()) return result;

        for (ISlotModifiable mod : gathered.values()) {
            result = mod.modifySlotLimit(result, slot);
        }

        if (result != original) {
            return result - original;
        }

        return original;
    }

    @Override
    public boolean canAddUpgrade(int slot, ItemStack stack) {
        for (int i = 0; i < upgradeSlots; i++) {
            ItemStack upgradeStack = upgradeHandler.getStackInSlot(i);
            if (upgradeStack == null) continue;

            IUpgradeWrapper wrapper = this.getUpgradeHandler()
                .getWrapperInSlot(slotIndex);
            if (wrapper == null) continue;
            if (wrapper instanceof IToggleable toggleable && !toggleable.isEnabled()) continue;

            if (wrapper instanceof ISlotModifiable modifiable) {
                if (!modifiable.canAddUpgrade(slot, stack)) return false;
            }
        }
        return true;
    }

    @Override
    public boolean canAddStack(int slot, ItemStack stack) {
        Map<Integer, ISlotModifiable> gathered = gatherCapabilityUpgrades(ISlotModifiable.class);
        if (stack != null && stack.getItem() instanceof BlockBackpack.ItemBackpack) {

            for (ISlotModifiable mod : gathered.values()) {
                if (mod.canAddStack(slot, stack)) {
                    return true;
                }
            }

            return false;
        }

        for (ISlotModifiable mod : gathered.values()) {
            if (!mod.canAddStack(slot, stack)) return false;
        }

        return true;
    }

    @Override
    public boolean canRemoveUpgrade(int slot) {
        ItemStack upgradeStack = upgradeHandler.getStackInSlot(slot);
        if (upgradeStack == null) return true;

        IUpgradeWrapper wrapper = this.getUpgradeHandler()
            .getWrapperInSlot(slotIndex);
        if (wrapper == null) return true;
        if (wrapper instanceof IToggleable toggleable && !toggleable.isEnabled()) return true;

        if (wrapper instanceof ISlotModifiable modifiable) {
            return modifiable.canRemoveUpgrade(slot);
        }

        return true;
    }

    @Override
    public boolean canReplaceUpgrade(int slot, ItemStack replacement) {
        ItemStack upgradeStack = upgradeHandler.getStackInSlot(slot);
        if (upgradeStack == null) return true;

        IUpgradeWrapper wrapper = this.getUpgradeHandler()
            .getWrapperInSlot(slotIndex);
        if (wrapper == null) return true;
        if (wrapper instanceof IToggleable toggleable && !toggleable.isEnabled()) return true;

        if (wrapper instanceof ISlotModifiable modifiable) {
            return modifiable.canReplaceUpgrade(slot, replacement);
        }
        return true;
    }

    @Override
    public boolean tick(EntityPlayer player) {
        Map<Integer, ITickable> gathered = gatherCapabilityUpgrades(ITickable.class);

        boolean dirty = false;

        for (ITickable wrapper : gathered.values()) {
            dirty |= wrapper.tick(player);
        }

        // Process disabled jukebox upgrades that have a pending stop sync
        for (int i = 0; i < upgradeSlots; i++) {
            if (gathered.containsKey(i)) continue;
            ItemStack stack = upgradeHandler.getStackInSlot(i);
            if (stack == null) continue;
            if (!ItemNBTHelpers.getBoolean(stack, IJukeboxUpgrade.PENDING_STOP_SYNC_TAG, false)) continue;
            IUpgradeWrapper wrapper2 = this.getUpgradeHandler()
                .getWrapperInSlot(slotIndex);
            if (wrapper2 instanceof ITickable tickable) {
                dirty |= tickable.tick(player);
            }
        }

        // Process removed jukebox upgrades that were playing
        processPendingJukeboxStops(player);

        return dirty;
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        Map<Integer, ITickable> gathered = gatherCapabilityUpgrades(ITickable.class);

        boolean dirty = false;

        for (ITickable wrapper : gathered.values()) {
            dirty |= wrapper.tick(world, pos);
        }

        // Process disabled jukebox upgrades that have a pending stop sync
        for (int i = 0; i < upgradeSlots; i++) {
            if (gathered.containsKey(i)) continue;
            ItemStack stack = upgradeHandler.getStackInSlot(i);
            if (stack == null) continue;
            if (!ItemNBTHelpers.getBoolean(stack, IJukeboxUpgrade.PENDING_STOP_SYNC_TAG, false)) continue;
            IUpgradeWrapper wrapper2 = this.getUpgradeHandler()
                .getWrapperInSlot(slotIndex);
            if (wrapper2 instanceof ITickable tickable) {
                dirty |= tickable.tick(world, pos);
            }
        }

        // Process removed jukebox upgrades that were playing
        processPendingJukeboxStops(world, pos);

        return dirty;
    }

    public void processPendingJukeboxStops(EntityPlayer player) {
        if (pendingJukeboxStops.isEmpty()) return;
        if (!(player instanceof EntityPlayerMP playerMP)) return;
        float x = (float) player.posX;
        float y = (float) player.posY;
        float z = (float) player.posZ;
        int carrierEntityId = player.getEntityId();
        var targetPoint = new NetworkRegistry.TargetPoint(player.worldObj.provider.dimensionId, x, y, z, 64);
        for (int slot : pendingJukeboxStops) {
            var packet = new PacketJukeboxPlaybackState(uuid, slot, false, 0, 0, x, y, z, "", carrierEntityId);
            OKBackpack.instance.getPacketHandler()
                .sendToAllAround(packet, targetPoint);
        }
        pendingJukeboxStops.clear();
    }

    public void processPendingJukeboxStops(World world, BlockPos pos) {
        if (pendingJukeboxStops.isEmpty()) return;
        float x = pos.x + 0.5f;
        float y = pos.y + 0.5f;
        float z = pos.z + 0.5f;
        var targetPoint = new NetworkRegistry.TargetPoint(world.provider.dimensionId, x, y, z, 64);
        for (int slot : pendingJukeboxStops) {
            var packet = new PacketJukeboxPlaybackState(uuid, slot, false, 0, 0, x, y, z, "", -1);
            OKBackpack.instance.getPacketHandler()
                .sendToAllAround(packet, targetPoint);
        }
        pendingJukeboxStops.clear();
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack) {
        Map<Integer, IFilterUpgrade> gathered = gatherCapabilityUpgrades(IFilterUpgrade.class);
        if (gathered.isEmpty()) return true;
        for (IFilterUpgrade mod : gathered.values()) {
            if (!mod.canInsert(slot, stack)) return false;
        }
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack) {
        Map<Integer, IFilterUpgrade> gathered = gatherCapabilityUpgrades(IFilterUpgrade.class);
        if (gathered.isEmpty()) return true;
        for (IFilterUpgrade mod : gathered.values()) {
            if (!mod.canExtract(slot, stack)) return false;
        }
        return true;
    }

    public boolean canPickupItem(ItemStack stack) {
        Map<Integer, IPickupUpgrade> gathered = gatherCapabilityUpgrades(IPickupUpgrade.class);
        if (gathered.isEmpty()) return false;
        for (IPickupUpgrade upgrade : gathered.values()) {
            if (upgrade.canPickup(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void applyContainerEntity(World world, Entity selfEntity) {
        Map<Integer, IEntityApplicable> gathered = gatherCapabilityUpgrades(IEntityApplicable.class);
        if (gathered.isEmpty()) return;
        for (IEntityApplicable mod : gathered.values()) {
            mod.applyContainerEntity(world, selfEntity);
        }
    }

    public boolean canPlayerAccess(UUID playerUUID) {
        if (!lockBackpack) return true;
        if (playerUUID == null || playerUuid == null || playerUuid.isEmpty()) return false;
        return playerUUID.equals(UUID.fromString(playerUuid));
    }

    public boolean hasCustomInventoryName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    @Override
    public NBTTagCompound getBackpackNBT() {
        if (backpack == null) {
            return null;
        }
        return backpack.getTagCompound();
    }

    public ItemStack findStackByUUID(EntityPlayer player) {
        if (player == null || uuid == null || type == null) return backpack;

        // Check held item first (fastest)
        ItemStack held = player.getHeldItem();
        if (isSameBackpack(held)) return held;

        // Check player inventory
        if (type == InventoryTypes.PLAYER) {
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (isSameBackpack(stack)) return stack;
            }
        }

        // Check Baubles if loaded
        if (type == InventoryTypes.BAUBLES) {
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

    @Override
    public void writeToItem() {
        if (backpack == null) return;

        NBTTagCompound root = backpack.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
            backpack.setTagCompound(root);
        }

        root.setTag(BACKPACK_NBT, serializeNBT());
        backpack.setTagCompound(root);
    }

    @Override
    public void readFromItem() {
        if (backpack == null) return;
        NBTTagCompound tag = ItemNBTHelpers.getNBT(backpack);
        if (tag.hasKey(BACKPACK_NBT)) deserializeNBT(tag.getCompoundTag(BACKPACK_NBT));
    }

    @Override
    public void writeToItem(EntityPlayer player) {
        this.backpack = findStackByUUID(player);
        writeToItem();
    }

    @Override
    public InventoryType getType() {
        return type;
    }

    @Override
    public void setType(InventoryType type) {
        this.type = type;
    }

    @Override
    public int getSlotIndex() {
        return slotIndex;
    }

    @Override
    public void setSlotIndex(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        if (backpackHandler.isSizeInconsistent(backpackSlots)) {
            backpackHandler.resize(backpackSlots);
        }
        if (getUpgradeHandler().isSizeInconsistent(upgradeSlots)) {
            getUpgradeHandler().resize(upgradeSlots);
        }

        tag.setInteger(BACKPACK_SLOTS, backpackSlots);
        tag.setInteger(UPGRADE_SLOTS, upgradeSlots);
        tag.setInteger(MAIN_COLOR, mainColor);
        tag.setInteger(ACCENT_COLOR, accentColor);

        tag.setTag(BACKPACK_INV, backpackHandler.serializeNBT());
        tag.setTag(UPGRADE_INV, upgradeHandler.serializeNBT());

        NBTTagCompound memoryTag = new NBTTagCompound();
        BackpackItemStackHelpers.saveAllSlotsExtended(memoryTag, backpackHandler.getMemorizedStacks());
        tag.setTag(MEMORY_STACK_ITEMS_TAG, memoryTag);

        List<Boolean> respectList = backpackHandler.getRespectNBTList();
        byte[] respectBytes = new byte[backpackSlots];
        for (int i = 0; i < backpackSlots; i++) {
            boolean val = i < respectList.size() && respectList.get(i);
            respectBytes[i] = (byte) (val ? 1 : 0);
        }
        tag.setByteArray(MEMORY_STACK_RESPECT_NBT_TAG, respectBytes);

        List<Boolean> locked = backpackHandler.getLockedSlotList();
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
                .loadAllItemsExtended(tag.getCompoundTag(MEMORY_STACK_ITEMS_TAG), backpackHandler.getMemorizedStacks());
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

    @Override
    public <T> Map<Integer, T> gatherCapabilityUpgrades(Class<T> capabilityClass) {
        Map<Integer, T> result = new HashMap<>();

        for (int i = 0; i < upgradeSlots; i++) {
            ItemStack stack = upgradeHandler.getStackInSlot(i);
            if (stack == null) continue;

            IUpgradeWrapper wrapper = this.getUpgradeHandler()
                .getWrapperInSlot(i);
            if (wrapper == null) continue;
            if (capabilityClass.isAssignableFrom(wrapper.getClass())) {
                result.put(i, capabilityClass.cast(wrapper));
            }
        }

        return result;
    }

    @Override
    public void setSortType(SortType sortType) {
        this.sortType = sortType;
    }

    @Override
    public SortType getSortType() {
        return sortType;
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

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    @Override
    public void markDirty() {
        this.isDirty = true;
        if (onInventoryHandlerRefresh != null) {
            onInventoryHandlerRefresh.run();
        }
    }

    @Override
    public void markClean() {
        this.isDirty = false;
    }

    @Override
    public void setInventorySlotChangeHandler(Runnable contentsChangeHandler) {
        this.onInventoryHandlerRefresh = contentsChangeHandler;
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

    @Override
    public IBackpackWrapper setBackpackStack(ItemStack backpackStack) {
        this.backpack = backpackStack;
        return this;
    }

    @Override
    public ItemStack getBackpack() {
        return backpack;
    }
}
