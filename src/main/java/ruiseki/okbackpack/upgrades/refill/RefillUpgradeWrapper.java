package ruiseki.okbackpack.upgrades.refill;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;
import com.google.common.collect.ImmutableMap;

import ruiseki.okbackpack.api.IBlockPickResponseUpgrade;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.datastructure.StringRepresentable;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.item.capability.CapabilityItemHandler;
import ruiseki.okcore.item.handler.IItemHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.inventory.ITrackedContentsItemHandler;
import ruiseki.okstoragecore.upgrades.FilterLogic;
import ruiseki.okstoragecore.upgrades.IFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class RefillUpgradeWrapper extends UpgradeWrapperBase<RefillUpgradeWrapper, ItemRefillUpgrade>
    implements IFilteredUpgrade, ITickableUpgrade, IBlockPickResponseUpgrade {

    private static final int COOLDOWN = 5;

    private final Map<Integer, TargetSlot> targetSlots;

    private final FilterLogic filterLogic;
    private final int slotCount;

    public RefillUpgradeWrapper(IStorageWrapper backpackWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(backpackWrapper, upgrade, upgradeSaveHandler);
        filterLogic = new FilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getFilterSlotCount());
        this.slotCount = upgradeItem.getFilterSlotCount();
        targetSlots = NBTHelpers
            .getMap(
                NBTHelpers.getOrCreateTag(upgrade),
                "targetSlots",
                Integer::valueOf,
                (k, tag) -> Optional.of(TargetSlot.fromName(((NBTTagString) tag).func_150285_a_())))
            .orElseGet(HashMap::new);
        if (upgradeItem.allowsTargetSlotSelection()) {
            FilterLogic.ObservableFilterItemStackHandler filterHandler = filterLogic.getFilterHandler();
            filterHandler.setOnSlotChange(s -> onFilterChange(filterHandler, s));
        }
        filterLogic.setAllowByDefault(true);
    }

    public int getSlotCount() {
        return slotCount;
    }

    private void onFilterChange(FilterLogic.ObservableFilterItemStackHandler filterHandler, int slot) {
        if (ItemHelpers.isEmpty(filterHandler.getStackInSlot(slot))) {
            targetSlots.remove(slot);
            saveTargetSlots();
        } else {
            if (!targetSlots.containsKey(slot)) {
                setTargetSlot(slot, TargetSlot.ANY);
            }
        }
    }

    public Map<Integer, TargetSlot> getTargetSlots() {
        return targetSlots;
    }

    public void setTargetSlot(int slot, TargetSlot targetSlot) {
        targetSlots.put(slot, targetSlot);
        saveTargetSlots();
    }

    private void saveTargetSlots() {
        NBTHelpers.setMap(
            NBTHelpers.getOrCreateTag(upgrade),
            "targetSlots",
            targetSlots,
            String::valueOf,
            t -> new NBTTagString(t.getSerializedName()));
        save();
    }

    @Override
    public FilterLogic getFilterLogic() {
        return filterLogic;
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (entity == null /* not supported in block form */ || isInCooldown(world)) {
            return;
        }
        CapabilityHelpers.getCapability(entity, CapabilityItemHandler.ITEM_HANDLER, null)
            .ifPresent(playerInvHandler -> InventoryHelpers.iterate(filterLogic.getFilterHandler(), (slot, filter) -> {
                if (ItemHelpers.isEmpty(filter)) {
                    return;
                }
                tryRefillFilter(entity, playerInvHandler, filter, getTargetSlots().getOrDefault(slot, TargetSlot.ANY));
            }));
        setCooldown(world, COOLDOWN);
    }

    private void tryRefillFilter(@NotNull EntityLivingBase entity, IItemHandler playerInvHandler, ItemStack filter,
        TargetSlot targetSlot) {
        if (!(entity instanceof EntityPlayer player)) {
            return;
        }
        int missingCount = targetSlot.missingCountGetter.getMissingCount(player, playerInvHandler, filter);
        if (ItemHandlerHelper.canItemStacksStack(player.inventory.getItemStack(), filter)) {
            missingCount -= Math.min(missingCount, ItemHelpers.getCount(player.inventory.getItemStack()));
        }
        if (missingCount == 0) {
            return;
        }
        IItemHandler extractFromHandler = storageWrapper.getInventoryForUpgradeProcessing();
        ItemStack toMove = filter.copy();
        ItemHelpers.setCount(toMove, missingCount);
        ItemStack extracted = InventoryHelpers.extractFromInventory(toMove, extractFromHandler, true);
        if (ItemHelpers.isEmpty(extracted)) {
            return;
        }
        ItemStack remaining = targetSlot.filler.fill(player, playerInvHandler, extracted);
        if (ItemHelpers.getCount(remaining) != ItemHelpers.getCount(extracted)) {
            ItemStack toExtract = extracted.copy();
            ItemHelpers.setCount(toExtract, ItemHelpers.getCount(extracted) - ItemHelpers.getCount(remaining));
            InventoryHelpers.extractFromInventory(toExtract, extractFromHandler, false);
        }
    }

    public boolean allowsTargetSlotSelection() {
        return upgradeItem.allowsTargetSlotSelection();
    }

    @Override
    public boolean pickBlock(EntityPlayer player, ItemStack filter) {
        if (!upgradeItem.supportsBlockPick()) {
            return false;
        }

        AtomicInteger stashSlot = new AtomicInteger(-1);
        AtomicBoolean hasItemInBackpack = new AtomicBoolean(false);

        ITrackedContentsItemHandler inventoryHandler = storageWrapper.getInventoryForUpgradeProcessing();
        InventoryHelpers.iterate(inventoryHandler, (slot, stack) -> {
            if (ItemHandlerHelper.canItemStacksStack(stack, filter)) {
                hasItemInBackpack.set(true);
                stashSlot.set(slot);
            }
        }, () -> stashSlot.get() > -1);

        ItemStack mainHandItem = player.getHeldItem();
        ItemStack toExtract = filter.copy();
        ItemHelpers.setCount(toExtract, filter.getMaxStackSize());

        if (hasItemInBackpack.get()
            && !ItemHelpers.isEmpty(InventoryHelpers.extractFromInventory(toExtract, inventoryHandler, true))) {
            ItemStack stashStack = inventoryHandler.getStackInSlot(stashSlot.get());
            boolean stashTooLarge = !ItemHelpers.isEmpty(stashStack)
                && ItemHelpers.getCount(stashStack) > filter.getMaxStackSize();
            boolean mainHandItemValid = ItemHelpers.isEmpty(mainHandItem)
                || inventoryHandler.isItemValid(stashSlot.get(), mainHandItem);
            boolean cannotInsertMainHandItem = !ItemHelpers.isEmpty(mainHandItem)
                && !ItemHelpers.isEmpty(inventoryHandler.insertItem(mainHandItem, true));
            if ((stashTooLarge || !mainHandItemValid) && cannotInsertMainHandItem) {
                if (canMoveMainHandToInventory(player)) {
                    ItemStack extracted = InventoryHelpers.extractFromInventory(toExtract, inventoryHandler, false);
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, extracted);
                    player.inventory.addItemStackToInventory(mainHandItem);
                    return true;
                } else {
                    player.addChatComponentMessage(
                        new ChatComponentTranslation("gui.okbackpack.status.no_space_for_mainhand_item"));
                    return false;
                }
            } else {
                ItemStack extracted = InventoryHelpers.extractFromInventory(toExtract, inventoryHandler, false);
                if (!ItemHelpers.isEmpty(mainHandItem)) {
                    inventoryHandler.insertItem(mainHandItem, false);
                }
                player.inventory.setInventorySlotContents(player.inventory.currentItem, extracted);
                return true;
            }
        }

        return false;
    }

    private boolean canMoveMainHandToInventory(EntityPlayer player) {
        int countToAdd = ItemHelpers.getCount(player.getHeldItem());
        for (int slot = 0; slot < player.inventory.getSizeInventory() - 5; slot++) {
            if (slot == player.inventory.currentItem) {
                continue;
            }
            ItemStack slotStack = player.inventory.getStackInSlot(slot);
            if (ItemHelpers.isEmpty(slotStack)) {
                return true;
            } else if (ItemHandlerHelper.canItemStacksStack(slotStack, player.getHeldItem())) {
                countToAdd -= (slotStack.getMaxStackSize() - ItemHelpers.getCount(slotStack));
                if (countToAdd <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public enum TargetSlot implements StringRepresentable {

        ANY("any", LangHelpers.localize("gui.okbackpack.upgrades.refill.target_slot.any"),
            EnumChatFormatting.DARK_GREEN
                + LangHelpers.localize("gui.okbackpack.upgrades.refill.target_slot.any.tooltip"),
            (player, playerInvHandler, filter) -> InventoryHelpers
                .getCountMissingInHandler(playerInvHandler, filter, filter.getMaxStackSize()),
            (player, playerInvHandler, stackToAdd) -> refillAnywhereInInventory(playerInvHandler, stackToAdd)),
        MAIN_HAND("main_hand", LangHelpers.localize("gui.okbackpack.upgrades.refill.target_slot.main_hand"),
            EnumChatFormatting.DARK_GREEN
                + LangHelpers.localize("gui.okbackpack.upgrades.refill.target_slot.main_hand.tooltip"),
            (player, playerInvHandler, filter) -> getMissingCount(player.getHeldItem(), filter),
            (player, playerInvHandler, stackToAdd) -> refillSlot(
                player::getHeldItem,
                stackToAdd,
                stack -> InventoryHelpers.setItemAtIndex(player, player.inventory.currentItem, stack))),
        TOOLBAR_1("toolbar_1", "1", EnumChatFormatting.DARK_GREEN + LangHelpers.localize(Constants.HOTBAR_TRANSL, 1),
            (player, playerInvHandler, filter) -> getMissingCount(player.inventory.getStackInSlot(0), filter),
            (player, playerInvHandler, stackToAdd) -> refillSlot(
                () -> player.inventory.getStackInSlot(0),
                stackToAdd,
                stack -> player.inventory.setInventorySlotContents(0, stack))),
        TOOLBAR_2("toolbar_2", "2", EnumChatFormatting.DARK_GREEN + LangHelpers.localize(Constants.HOTBAR_TRANSL, 2),
            (player, playerInvHandler, filter) -> getMissingCount(player.inventory.getStackInSlot(1), filter),
            (player, playerInvHandler, stackToAdd) -> refillSlot(
                () -> player.inventory.getStackInSlot(1),
                stackToAdd,
                stack -> player.inventory.setInventorySlotContents(1, stack))),
        TOOLBAR_3("toolbar_3", "3", EnumChatFormatting.DARK_GREEN + LangHelpers.localize(Constants.HOTBAR_TRANSL, 3),
            (player, playerInvHandler, filter) -> getMissingCount(player.inventory.getStackInSlot(2), filter),
            (player, playerInvHandler, stackToAdd) -> refillSlot(
                () -> player.inventory.getStackInSlot(2),
                stackToAdd,
                stack -> player.inventory.setInventorySlotContents(2, stack))),
        TOOLBAR_4("toolbar_4", "4", EnumChatFormatting.DARK_GREEN + LangHelpers.localize(Constants.HOTBAR_TRANSL, 4),
            (player, playerInvHandler, filter) -> getMissingCount(player.inventory.getStackInSlot(3), filter),
            (player, playerInvHandler, stackToAdd) -> refillSlot(
                () -> player.inventory.getStackInSlot(3),
                stackToAdd,
                stack -> player.inventory.setInventorySlotContents(3, stack))),
        TOOLBAR_5("toolbar_5", "5", EnumChatFormatting.DARK_GREEN + LangHelpers.localize(Constants.HOTBAR_TRANSL, 5),
            (player, playerInvHandler, filter) -> getMissingCount(player.inventory.getStackInSlot(4), filter),
            (player, playerInvHandler, stackToAdd) -> refillSlot(
                () -> player.inventory.getStackInSlot(4),
                stackToAdd,
                stack -> player.inventory.setInventorySlotContents(4, stack))),
        TOOLBAR_6("toolbar_6", "6", EnumChatFormatting.DARK_GREEN + LangHelpers.localize(Constants.HOTBAR_TRANSL, 6),
            (player, playerInvHandler, filter) -> getMissingCount(player.inventory.getStackInSlot(5), filter),
            (player, playerInvHandler, stackToAdd) -> refillSlot(
                () -> player.inventory.getStackInSlot(5),
                stackToAdd,
                stack -> player.inventory.setInventorySlotContents(5, stack))),
        TOOLBAR_7("toolbar_7", "7", EnumChatFormatting.DARK_GREEN + LangHelpers.localize(Constants.HOTBAR_TRANSL, 7),
            (player, playerInvHandler, filter) -> getMissingCount(player.inventory.getStackInSlot(6), filter),
            (player, playerInvHandler, stackToAdd) -> refillSlot(
                () -> player.inventory.getStackInSlot(6),
                stackToAdd,
                stack -> player.inventory.setInventorySlotContents(6, stack))),
        TOOLBAR_8("toolbar_8", "8", EnumChatFormatting.DARK_GREEN + LangHelpers.localize(Constants.HOTBAR_TRANSL, 8),
            (player, playerInvHandler, filter) -> getMissingCount(player.inventory.getStackInSlot(7), filter),
            (player, playerInvHandler, stackToAdd) -> refillSlot(
                () -> player.inventory.getStackInSlot(7),
                stackToAdd,
                stack -> player.inventory.setInventorySlotContents(7, stack))),
        TOOLBAR_9("toolbar_9", "9", EnumChatFormatting.DARK_GREEN + LangHelpers.localize(Constants.HOTBAR_TRANSL, 9),
            (player, playerInvHandler, filter) -> getMissingCount(player.inventory.getStackInSlot(8), filter),
            (player, playerInvHandler, stackToAdd) -> refillSlot(
                () -> player.inventory.getStackInSlot(8),
                stackToAdd,
                stack -> player.inventory.setInventorySlotContents(8, stack)));

        private final String name;

        private final String acronym;
        private final String description;
        private final MissingCountGetter missingCountGetter;
        private final Filler filler;

        TargetSlot(String name, String acronym, String description, MissingCountGetter missingCountGetter,
            Filler filler) {
            this.name = name;
            this.acronym = acronym;
            this.description = description;
            this.missingCountGetter = missingCountGetter;
            this.filler = filler;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public TargetSlot next() {
            return VALUES[(ordinal() + 1) % VALUES.length];
        }

        public TargetSlot previous() {
            return VALUES[Math.floorMod(ordinal() - 1, VALUES.length)];
        }

        private static final Map<String, TargetSlot> NAME_VALUES;
        private static final TargetSlot[] VALUES;

        static {
            ImmutableMap.Builder<String, TargetSlot> builder = new ImmutableMap.Builder<>();
            for (TargetSlot value : TargetSlot.values()) {
                builder.put(value.getSerializedName(), value);
            }
            NAME_VALUES = builder.build();
            VALUES = values();
        }

        public static TargetSlot fromOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal >= VALUES.length) {
                return ANY;
            }
            return VALUES[ordinal];
        }

        public static TargetSlot fromName(String name) {
            return NAME_VALUES.getOrDefault(name, ANY);
        }

        public String getAcronym() {
            return acronym;
        }

        public String getDescription() {
            return description;
        }

        private static class Constants {

            private static final String HOTBAR_TRANSL = "gui.okbackpack.upgrades.refill.target_slot.hotbar.tooltip";
        }

        private interface MissingCountGetter {

            int getMissingCount(EntityPlayer player, IItemHandler playerInventory, ItemStack filter);
        }

        private interface Filler {

            ItemStack fill(EntityPlayer player, IItemHandler playerInventory, ItemStack stackToAdd);
        }

        private static ItemStack refillAnywhereInInventory(IItemHandler playerInvHandler, ItemStack extracted) {
            AtomicReference<ItemStack> remainingStack = new AtomicReference<>(extracted);
            InventoryHelpers.iterate(playerInvHandler, (slot, stack) -> {
                if (ItemHandlerHelpers.canItemStacksStack(stack, remainingStack.get())) {
                    remainingStack.set(playerInvHandler.insertItem(slot, remainingStack.get(), false));
                }
            }, () -> ItemHelpers.isEmpty(remainingStack.get()));

            ItemStack remaining = remainingStack.get();

            if (!ItemHelpers.isEmpty(remaining)) {
                ItemStack afterInsert = InventoryHelpers.insertIntoInventory(remaining, playerInvHandler, true);
                if (ItemHelpers.getCount(afterInsert) == ItemHelpers.getCount(remaining)) {
                    return remaining;
                }
                ItemStack toInsert = remaining.copy();
                ItemHelpers.setCount(toInsert, ItemHelpers.getCount(remaining) - ItemHelpers.getCount(afterInsert));

                ItemStack insertRemainder = InventoryHelpers.insertIntoInventory(toInsert, playerInvHandler, false);
                if (ItemHelpers.isEmpty(insertRemainder)) {
                    return afterInsert;
                }
                if (ItemHelpers.isEmpty(afterInsert)) {
                    return insertRemainder;
                }

                ItemHelpers.grow(afterInsert, ItemHelpers.getCount(insertRemainder));
                return afterInsert;
            }
            return remaining;
        }

        private static int getMissingCount(ItemStack stack, ItemStack filter) {
            if (ItemHandlerHelper.canItemStacksStack(stack, filter)) {
                return filter.getMaxStackSize() - ItemHelpers.getCount(stack);
            }
            return filter.getMaxStackSize();
        }

        private static ItemStack refillSlot(Supplier<ItemStack> getSlotContents, ItemStack stackToAdd,
            Consumer<ItemStack> setSlotContents) {
            ItemStack contents = getSlotContents.get();
            if (ItemHelpers.isEmpty(contents)) {
                setSlotContents.accept(stackToAdd);
                return ItemHelpers.EMPTY;
            }
            if (ItemHandlerHelper.canItemStacksStack(contents, stackToAdd)) {
                ItemHelpers.grow(contents, ItemHelpers.getCount(stackToAdd));
                return ItemHelpers.EMPTY;
            }
            return stackToAdd;
        }
    }
}
