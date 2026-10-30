
package ruiseki.backpack.helpers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;

import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.backpack.capabilities.CapabilityBackpackWrapperConfig;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.inventory.ClickType;
import ruiseki.okstoragecore.client.gui.slot.IndexedModularSlot;
import ruiseki.okstoragecore.client.gui.slot.ModularUpgradeSlot;
import ruiseki.okstoragecore.inventory.InventoryHandler;

public class InteractionHelpers {

    private static final int RIGHT_MOUSE_BUTTON = 1;

    private InteractionHelpers() {}

    public static boolean isSupportedClick(int slotId, int mouseButton, ClickType mode) {
        return GeneralConfig.enableBackpackInventoryInteraction && slotId >= 0
            && mouseButton == RIGHT_MOUSE_BUTTON
            && mode == ClickType.PICKUP;
    }

    public static boolean isBackpack(ItemStack stack) {
        return CapabilityHelpers.getCapability(stack, CapabilityBackpackWrapperConfig.CAPABILITY)
            .isPresent();
    }

    public static boolean slotClick(Container container, EntityPlayer player, int slotId, int mouseButton,
        ClickType clickType) {
        if (container == null || player == null
            || !isSupportedClick(slotId, mouseButton, clickType)
            || slotId >= container.inventorySlots.size()) {
            return false;
        }

        Slot slot = container.inventorySlots.get(slotId);
        ItemStack cursor = player.inventory.getItemStack();

        InteractionResult result = analyze(player, slot, cursor);
        if (!result.canExecuteTransfer()) {
            return false;
        }

        ItemStack backpack = result.backpackStack;

        return CapabilityHelpers.getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
            .map(wrapper -> {
                // Client only predicts the interaction.
                if (player.worldObj.isRemote) return true;

                InventoryHandler handler = wrapper.getInventoryHandler();
                if (handler == null) return false;

                if (result.symbol == InteractionResult.Symbol.MINUS) {
                    // Slot -> Backpack
                    ItemStack extracted = slot.decrStackSize(result.insertedCount);
                    if (ItemHelpers.isEmpty(extracted)) return false;
                    ItemStack remaining = handler.insertItem(extracted.copy(), false);
                    if (!ItemHelpers.isEmpty(remaining)) {
                        if (!player.inventory.addItemStackToInventory(remaining)) {
                            player.dropPlayerItemWithRandomChoice(remaining, false);
                        }
                    }

                    slot.onPickupFromSlot(player, extracted);
                    slot.onSlotChanged();
                } else {
                    // Cursor -> Backpack
                    ItemStack remaining = handler.insertItem(cursor.copy(), false);
                    if (ItemHelpers.getCount(remaining) >= result.sourceCount) return false;
                    player.inventory.setItemStack(ItemHelpers.isEmpty(remaining) ? ItemHelpers.EMPTY : remaining);
                    slot.onSlotChanged();
                }

                player.inventory.markDirty();
                container.detectAndSendChanges();

                if (player instanceof EntityPlayerMP playerMP) {
                    // Sync hovered slot.
                    playerMP.playerNetServerHandler.sendPacket(
                        new S2FPacketSetSlot(
                            container.windowId,
                            slotId,
                            ItemHelpers.isEmpty(slot.getStack()) ? null
                                : slot.getStack()
                                    .copy()));

                    // Sync cursor item.
                    playerMP.playerNetServerHandler.sendPacket(
                        new S2FPacketSetSlot(
                            -1,
                            -1,
                            ItemHelpers.isEmpty(player.inventory.getItemStack()) ? null
                                : player.inventory.getItemStack()
                                    .copy()));

                    playerMP.inventoryContainer.detectAndSendChanges();
                }
                return true;
            })
            .orElse(false);
    }

    public static boolean isAllowSlot(Slot slot, EntityPlayer player) {
        if (slot == null) return false;
        if (slot instanceof ModularUpgradeSlot) return false;
        if (slot instanceof IndexedModularSlot) return true;
        ItemStack stack = slot.getStack();
        return !ItemHelpers.isEmpty(stack) && (player == null || slot.canTakeStack(player));
    }

    public static final class InteractionResult {

        public enum State {
            GREEN,
            YELLOW
        }

        public enum Symbol {
            PLUS,
            MINUS
        }

        public static final InteractionResult NONE = new InteractionResult(null, null, false, 0, 0, null);

        public final Symbol symbol;
        public final State state;
        public final boolean fullFitRequired;
        public final int sourceCount;
        public final int insertedCount;
        public final ItemStack backpackStack;

        private InteractionResult(Symbol symbol, State state, boolean fullFitRequired, int sourceCount,
            int insertedCount, ItemStack backpackStack) {

            this.symbol = symbol;
            this.state = state;
            this.fullFitRequired = fullFitRequired;
            this.sourceCount = sourceCount;
            this.insertedCount = insertedCount;
            this.backpackStack = backpackStack;
        }

        public static InteractionResult of(Symbol symbol, boolean matchingSpace, boolean fullFitRequired,
            int sourceCount, int insertedCount, ItemStack backpackStack) {

            if (symbol == null || sourceCount <= 0 || insertedCount <= 0) {
                return NONE;
            }

            return new InteractionResult(
                symbol,
                matchingSpace ? State.GREEN : State.YELLOW,
                fullFitRequired,
                sourceCount,
                insertedCount,
                backpackStack);
        }

        public int getRemainingCount() {
            return Math.max(0, sourceCount - insertedCount);
        }

        public boolean canExecuteTransfer() {
            return symbol != null && insertedCount > 0 && (!fullFitRequired || insertedCount == sourceCount);
        }

        public boolean canRenderOverlay() {
            return canExecuteTransfer();
        }
    }

    public static InteractionResult getResult(EntityPlayer player, Slot slot, ItemStack cursorStack) {
        return analyze(player, slot != null ? slot.getStack() : null, cursorStack);
    }

    public static InteractionResult analyze(EntityPlayer player, Slot slot, ItemStack cursorStack) {
        if (!GeneralConfig.enableBackpackInventoryInteraction || player == null
            || !isAllowSlot(slot, player)
            || ItemHelpers.isEmpty(cursorStack)) {
            return InteractionResult.NONE;
        }
        return analyze(player, slot.getStack(), cursorStack);
    }

    public static InteractionResult analyze(EntityPlayer player, ItemStack slotStack, ItemStack cursorStack) {
        if (!GeneralConfig.enableBackpackInventoryInteraction || player == null || ItemHelpers.isEmpty(cursorStack)) {
            return InteractionResult.NONE;
        }

        boolean carried = isBackpack(cursorStack);
        boolean slotted = isBackpack(slotStack);

        ItemStack backpack = carried ? cursorStack : slotStack;
        ItemStack source = carried ? slotStack : cursorStack;

        if (carried == slotted || ItemHelpers.isEmpty(source)) {
            return new InteractionResult(null, null, false, 0, 0, backpack);
        }

        InteractionResult result = analyzeInsert(
            source,
            backpack,
            carried ? InteractionResult.Symbol.MINUS : InteractionResult.Symbol.PLUS,
            carried);

        return result == InteractionResult.NONE ? new InteractionResult(null, null, false, 0, 0, backpack) : result;
    }

    private static InteractionResult analyzeInsert(ItemStack source, ItemStack backpack,
        InteractionResult.Symbol symbol, boolean fullFitRequired) {

        if (ItemHelpers.isEmpty(source) || ItemHelpers.isEmpty(backpack) || symbol == null) {
            return InteractionResult.NONE;
        }

        return CapabilityHelpers.getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
            .map(wrapper -> {
                InventoryHandler handler = wrapper.getInventoryHandler();
                if (handler == null) return InteractionResult.NONE;

                int sourceCount = ItemHelpers.getCount(source);
                if (sourceCount <= 0) return InteractionResult.NONE;

                ItemStack remaining = handler.insertItem(source.copy(), true);
                int inserted = Math.clamp(sourceCount - ItemHelpers.getCount(remaining), 0, sourceCount);

                return InteractionResult.of(
                    symbol,
                    hasMatchingStackSpace(wrapper, source),
                    fullFitRequired,
                    sourceCount,
                    inserted,
                    backpack);
            })
            .orElse(InteractionResult.NONE);
    }

    private static boolean hasMatchingStackSpace(IBackpackWrapper wrapper, ItemStack source) {
        InventoryHandler handler = wrapper.getInventoryHandler();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack existing = handler.getStackInSlot(i);
            if (!ItemHelpers.isEmpty(existing) && ItemHandlerHelpers.canItemStacksStack(existing, source)
                && existing.stackSize < handler.getStackLimit(i, existing)
                && handler.isItemValid(i, source)) {
                return true;
            }
        }
        return false;
    }

}
