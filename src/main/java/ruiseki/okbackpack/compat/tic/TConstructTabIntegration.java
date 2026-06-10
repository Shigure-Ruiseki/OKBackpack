package ruiseki.okbackpack.compat.tic;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import tconstruct.client.tabs.AbstractTab;
import tconstruct.client.tabs.InventoryTabVanilla;
import tconstruct.client.tabs.TabRegistry;

public class TConstructTabIntegration {

    private static boolean orderingHookRegistered;

    private TConstructTabIntegration() {}

    public static void registerTab() {
        List<AbstractTab> tabs = TabRegistry.getTabList();
        OKBackpackInventoryTab tab = getExistingTab(tabs);
        if (tab == null) {
            tab = new OKBackpackInventoryTab();
            tabs.add(tab);
        }
        moveAfterVanillaInventoryTab(tabs, tab);
    }

    public static void registerOrderingHook() {
        if (orderingHookRegistered) {
            return;
        }

        orderingHookRegistered = true;
        MinecraftForge.EVENT_BUS.register(new TConstructTabOrderingHook());
    }

    public static void updateTabValues(int cornerX, int cornerY, Class<?> selectedButton) {
        TabRegistry.updateTabValues(cornerX, cornerY, selectedButton);
    }

    public static void addTabsToList(List<GuiButton> buttonList) {
        TabRegistry.addTabsToList(buttonList);
    }

    private static OKBackpackInventoryTab getExistingTab(List<AbstractTab> tabs) {
        for (AbstractTab tab : tabs) {
            if (tab instanceof OKBackpackInventoryTab okBackpackTab) {
                return okBackpackTab;
            }
        }
        return null;
    }

    private static void moveAfterVanillaInventoryTab(List<AbstractTab> tabs, OKBackpackInventoryTab tab) {
        tabs.remove(tab);

        int vanillaIndex = findVanillaInventoryTabIndex(tabs);
        if (vanillaIndex < 0) {
            tabs.add(tab);
            return;
        }

        tabs.add(vanillaIndex + 1, tab);
    }

    private static int findVanillaInventoryTabIndex(List<AbstractTab> tabs) {
        for (int index = 0; index < tabs.size(); index++) {
            if (tabs.get(index) instanceof InventoryTabVanilla) {
                return index;
            }
        }
        return -1;
    }

    public static class TConstructTabOrderingHook {

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void onInventoryGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
            if (event.gui instanceof GuiInventory) {
                registerTab();
            }
        }
    }
}
