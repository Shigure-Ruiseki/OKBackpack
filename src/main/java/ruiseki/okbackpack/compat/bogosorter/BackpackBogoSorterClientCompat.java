package ruiseki.okbackpack.compat.bogosorter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.client.event.GuiScreenEvent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.client.keybinds.control.BSKeybinds;
import com.cleanroommc.bogosorter.common.config.SortRulesConfig;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import com.cleanroommc.bogosorter.common.sort.color.ItemColorHelper;
import com.cleanroommc.modularui.api.event.KeyboardInputEvent;
import com.cleanroommc.modularui.api.event.MouseInputEvent;
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiContainerAccessor;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ruiseki.okbackpack.client.gui.container.BackPackContainer;
import ruiseki.okbackpack.client.gui.slot.ModularBackpackSlot;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSH;
import ruiseki.okbackpack.client.gui.syncHandler.BackpackSHRegisters;

public class BackpackBogoSorterClientCompat {

    private static final long SORT_COOLDOWN_MS = 500;

    private long lastSort;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKeyboardInput(KeyboardInputEvent.Pre event) {
        trySortHoveredBackpack(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseInput(MouseInputEvent.Pre event) {
        trySortHoveredBackpack(event);
    }

    private void trySortHoveredBackpack(GuiScreenEvent event) {
        if (!isSortKeyPressed()) return;
        if (!(event.gui instanceof GuiContainer gui)) return;
        if (!(gui.inventorySlots instanceof BackPackContainer container)) return;
        if (!canSortNow()) return;

        Slot hoveredSlot = ((GuiContainerAccessor) gui).getHoveredSlot();
        if (!(hoveredSlot instanceof ModularBackpackSlot)) return;

        sortBackpack(container);
        event.setCanceled(true);
        lastSort = Minecraft.getSystemTime();
    }

    private boolean canSortNow() {
        return Minecraft.getSystemTime() - lastSort > SORT_COOLDOWN_MS;
    }

    private boolean isSortKeyPressed() {
        int keyCode = BSKeybinds.sortKeyInGUI.getKeyCode();
        if (keyCode == 0) return false;
        if (keyCode > 0) {
            return BSKeybinds.sortKeyInGUI.isPressed() || Keyboard.isKeyDown(keyCode);
        }
        return Mouse.isButtonDown(100 + keyCode);
    }

    public static void sortBackpack(BackPackContainer container) {
        if (container == null) return;
        BackpackSH syncHandler = findBackpackSyncHandler(container);
        if (syncHandler == null) return;

        BackpackBogoSorterClientCompat.writeSortRequest(syncHandler, container);
        SortHandler.playSortSound();
    }

    private static BackpackSH findBackpackSyncHandler(BackPackContainer container) {
        if (container.getSyncManager() == null || container.getSyncManager()
            .getMainPSM() == null) {
            return null;
        }
        PanelSyncManager panelSyncManager = container.getSyncManager()
            .getMainPSM();
        var syncHandler = panelSyncManager.findSyncHandlerNullable("backpack_wrapper", 0);
        if (syncHandler instanceof BackpackSH backpackSH) {
            return backpackSH;
        }
        return null;
    }

    private static void writeSortRequest(BackpackSH syncHandler, BackPackContainer container) {
        List<SortRule<ItemStack>> itemRules = SortRulesConfig.sortRules;
        var nbtRules = SortRulesConfig.nbtSortRules;
        boolean needsName = itemRules.contains(BogoSortAPI.INSTANCE.getItemSortRule("display_name"));
        boolean needsColor = itemRules.contains(BogoSortAPI.INSTANCE.getItemSortRule("color"));

        List<BackpackBogoSorterClientSortData> clientData = collectClientSortData(container, needsName, needsColor);

        syncHandler.syncToServer(BackpackSH.getId(BackpackSHRegisters.UPDATE_BOGO_SORT_INV), buf -> {
            buf.writeVarIntToBuffer(itemRules.size());
            for (SortRule<ItemStack> rule : itemRules) {
                buf.writeVarIntToBuffer(rule.getSyncId());
                buf.writeBoolean(rule.isInverted());
            }

            buf.writeVarIntToBuffer(nbtRules.size());
            for (var rule : nbtRules) {
                buf.writeVarIntToBuffer(rule.getSyncId());
                buf.writeBoolean(rule.isInverted());
            }

            buf.writeVarIntToBuffer(clientData.size());
            for (BackpackBogoSorterClientSortData data : clientData) {
                try {
                    data.write(buf);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static List<BackpackBogoSorterClientSortData> collectClientSortData(BackPackContainer container,
        boolean needsName, boolean needsColor) {
        if (!needsName && !needsColor) return Collections.emptyList();
        List<BackpackBogoSorterClientSortData> result = new ArrayList<>(container.inventorySlots.size());

        for (Object entry : container.inventorySlots) {
            if (!(entry instanceof ModularBackpackSlot slot)) continue;
            ItemStack stack = slot.getStack();
            if (stack == null || stack.stackSize <= 0) continue;
            int slotIndex = slot.getSlotIndex();
            if (container.wrapper.isSlotMemorized(slotIndex) || container.wrapper.isSlotLocked(slotIndex)) continue;

            result.add(
                new BackpackBogoSorterClientSortData(
                    slotIndex,
                    needsName ? stack.getDisplayName() : "",
                    needsColor ? ItemColorHelper.getItemColorHue(stack) : 0));
        }
        return result;
    }

    public static void openConfigGui() {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        BogoSortAPI.INSTANCE.openConfigGui(currentScreen);
    }

    public record BackpackBogoSorterClientSortData(int slotIndex, String name, int color) {

        public void write(PacketBuffer buf) throws IOException {
            buf.writeVarIntToBuffer(slotIndex);
            buf.writeStringToBuffer(name);
            buf.writeVarIntToBuffer(color);
        }
    }
}
