package ruiseki.backpack.modcompat.jfmuy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.inventory.Slot;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.backpack.backpack.BackpackPanel;
import ruiseki.backpack.client.gui.container.BackpackContainer;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.jfmuy.api.recipe.transfer.IRecipeTransferInfo;
import ruiseki.okstoragecore.client.gui.slot.CraftingSlotInfo;
import ruiseki.okstoragecore.client.gui.slot.ModularStorageSlot;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class StorageCraftingTransferInfo implements IRecipeTransferInfo<BackpackContainer> {

    @Override
    public Class<BackpackContainer> getContainerClass() {
        return BackpackContainer.class;
    }

    @Override
    public String getRecipeCategoryUid() {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Override
    public boolean canHandle(BackpackContainer container) {
        return getCraftingInfo(container) != null;
    }

    private BackpackPanel getPanel(BackpackContainer container) {
        return container.getStoragePanel();
    }

    private CraftingSlotInfo getCraftingInfo(BackpackContainer container) {
        return this.getPanel(container)
            .getActiveUpgradeSlotUpdate()
            .map(UpgradeSlotUpdate::getCraftingInfo)
            .orElse(null);
    }

    @Override
    public List<Slot> getRecipeSlots(BackpackContainer container) {
        CraftingSlotInfo info = getCraftingInfo(container);
        if (info != null && info.getCraftingMatrixSlots() != null) {
            return List.of(info.getCraftingMatrixSlots());
        }
        return Collections.emptyList();
    }

    @Override
    public List<Slot> getInventorySlots(BackpackContainer container) {
        List<Slot> list = new ArrayList<>();
        if (container == null || container.inventorySlots == null) {
            return list;
        }
        for (Object obj : container.inventorySlots) {
            if (!(obj instanceof Slot slot)) continue;
            if (!(slot instanceof ModularSlot modularSlot)) continue;
            if (!slot.canTakeStack(container.getPlayer())) continue;
            if (slot instanceof ModularStorageSlot || "player_inventory".equals(modularSlot.getSlotGroupName())) {
                list.add(slot);
            }
        }
        return list;
    }

    @Override
    public int getOutputSlot(BackpackContainer container) {
        CraftingSlotInfo info = this.getCraftingInfo(container);
        if (info != null) {
            return info.getCraftingOutputSlot()
                .getSlotIndex();
        }
        return -1;
    }
}
