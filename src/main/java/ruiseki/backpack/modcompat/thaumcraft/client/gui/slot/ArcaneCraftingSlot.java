package ruiseki.backpack.modcompat.thaumcraft.client.gui.slot;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.backpack.modcompat.thaumcraft.helper.ThaumcraftHelpers;
import ruiseki.backpack.modcompat.thaumcraft.upgrades.arcane.ArcaneCraftingUpgradeWrapper;
import ruiseki.okstoragecore.client.gui.slot.ModularCraftingSlot;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class ArcaneCraftingSlot extends ModularCraftingSlot {

    public ArcaneCraftingSlot(StoragePanel storagePanel, int upgradeIndex) {
        super(storagePanel, upgradeIndex);
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        ArcaneCraftingUpgradeWrapper arcane = (ArcaneCraftingUpgradeWrapper) this.getUpgradeWrapper();
        if (arcane == null) return super.canTakeStack(player);

        String missingResearch = arcane.getMissingResearch();
        if (missingResearch != null && !missingResearch.isEmpty()) return false;

        Map<String, Integer> requiredAspects = arcane.getRequiredAspects();
        if (requiredAspects == null || requiredAspects.isEmpty()) return super.canTakeStack(player);

        ItemStack wandStack = arcane.getStorage()
            .getStackInSlot(9);
        if (wandStack == null) return false;

        for (Map.Entry<String, Integer> entry : requiredAspects.entrySet()) {
            if (entry.getValue() <= 0) continue;
            int current = ThaumcraftHelpers.getWandVis(wandStack, entry.getKey());
            if (current < entry.getValue() * 100) return false;
        }

        return super.canTakeStack(player);
    }
}
