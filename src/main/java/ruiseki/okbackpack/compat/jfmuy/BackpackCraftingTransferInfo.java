package ruiseki.okbackpack.compat.jfmuy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.inventory.Slot;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferInfo;
import ruiseki.okbackpack.client.gui.container.BackPackContainer;
import ruiseki.okbackpack.client.gui.slot.CraftingSlotInfo;
import ruiseki.okbackpack.client.gui.slot.LockedPlayerSlot;
import ruiseki.okbackpack.client.gui.slot.ModularBackpackSlot;
import ruiseki.okbackpack.common.block.BackpackPanel;

public class BackpackCraftingTransferInfo implements IRecipeTransferInfo<BackPackContainer> {

    @Override
    public Class<BackPackContainer> getContainerClass() {
        return BackPackContainer.class;
    }

    @Override
    public String getRecipeCategoryUid() {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Override
    public boolean canHandle(BackPackContainer container) {
        return getCraftingInterfaceIndex(container) != null;
    }

    private BackpackPanel getPanel(BackPackContainer container) {
        if (container == null || !container.isInitialized()) return null;

        ModularScreen screen = container.getScreen();
        if (screen == null) return null;

        ModularPanel mainPanel = screen.getPanelManager()
            .getMainPanel();
        if (mainPanel instanceof BackpackPanel) return (BackpackPanel) mainPanel;

        return null;
    }

    private Integer getCraftingInterfaceIndex(BackPackContainer container) {
        BackpackPanel panel = getPanel(container);
        if (panel == null) {
            return null;
        }
        return panel.getOpenCraftingUpgradeSlot();
    }

    private CraftingSlotInfo getCraftingInterfaceInfo(BackPackContainer container) {
        BackpackPanel panel = getPanel(container);
        if (panel == null) return null;
        return panel.getCraftingInfo(panel.getOpenCraftingUpgradeSlot(), "crafting_info");
    }

    @Override
    public List<Slot> getRecipeSlots(BackPackContainer container) {
        CraftingSlotInfo info = getCraftingInterfaceInfo(container);
        if (info != null && info.getCraftingMatrixSlots() != null) {
            return List.of(info.getCraftingMatrixSlots());
        }
        return Collections.emptyList();
    }

    @Override
    public List<Slot> getInventorySlots(BackPackContainer container) {
        List<Slot> list = new ArrayList<>();
        if (container == null || container.inventorySlots == null) {
            return list;
        }

        for (Object obj : container.inventorySlots) {
            if (!(obj instanceof Slot slot)) continue;

            if (!(slot instanceof ModularSlot modularSlot) || slot instanceof LockedPlayerSlot) continue;

            if (slot instanceof ModularBackpackSlot || "player_inventory".equals(modularSlot.getSlotGroupName())) {
                list.add(slot);
            }
        }
        return list;
    }

    @Override
    public int getOutputSlot(BackPackContainer container) {
        CraftingSlotInfo info = getCraftingInterfaceInfo(container);
        if (info != null) {
            return info.getCraftingOutputSlot()
                .getSlotIndex();
        }
        return -1;
    }
}
