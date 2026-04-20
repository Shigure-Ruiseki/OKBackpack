package ruiseki.okbackpack.client.gui.slot;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import lombok.Setter;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IArcaneCraftingUpgrade;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okbackpack.compat.thaumcraft.ThaumcraftHelpers;
import ruiseki.okcore.item.IItemHandler;

public class IndexedModularArcaneSlot extends IndexedModularCraftingSlot {

    @Setter
    private Runnable onCraftCallback;

    public IndexedModularArcaneSlot(int upgradeSlotIndex, IStorageWrapper wrapper, IItemHandler inv, int invIndex) {
        super(upgradeSlotIndex, wrapper, inv, invIndex);
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        if (!Mods.Thaumcraft.isModLoaded()) return super.canTakeStack(player);

        IArcaneCraftingUpgrade arcane = wrapper != null ? wrapper.gatherCapabilityUpgrades(IArcaneCraftingUpgrade.class)
            .get(upgradeSlotIndex) : null;
        if (arcane == null) return super.canTakeStack(player);

        String missingResearch = arcane.getMissingResearch();
        if (missingResearch != null && !missingResearch.isEmpty()) return false;

        Map<String, Integer> requiredAspects = arcane.getRequiredAspects();
        if (requiredAspects == null || requiredAspects.isEmpty()) return super.canTakeStack(player);

        ItemStack wandStack = arcane.getStorage()
            .getStackInSlot(IArcaneCraftingUpgrade.WAND_SLOT_INDEX);
        if (wandStack == null) return false;

        for (Map.Entry<String, Integer> entry : requiredAspects.entrySet()) {
            if (entry.getValue() <= 0) continue;
            int current = ThaumcraftHelpers.getWandVis(wandStack, entry.getKey());
            if (current < entry.getValue() * 100) return false;
        }

        return super.canTakeStack(player);
    }

    @Override
    public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
        if (craftMatrix == null) return;

        if (Mods.Thaumcraft.isModLoaded()) {
            consumeArcaneVis(player);
        }

        super.onPickupFromSlot(player, stack);

        if (onCraftCallback != null) {
            onCraftCallback.run();
        }
    }

    private void consumeArcaneVis(EntityPlayer player) {
        IArcaneCraftingUpgrade arcane = wrapper != null ? wrapper.gatherCapabilityUpgrades(IArcaneCraftingUpgrade.class)
            .get(upgradeSlotIndex) : null;
        if (arcane == null) return;

        Map<String, Integer> requiredAspects = arcane.getRequiredAspects();
        if (requiredAspects == null || requiredAspects.isEmpty()) return;

        ItemStack wandStack = arcane.getStorage()
            .getStackInSlot(IArcaneCraftingUpgrade.WAND_SLOT_INDEX);
        if (wandStack == null) return;

        ThaumcraftHelpers.consumeAllVisCrafting(wandStack, player, requiredAspects);
    }

}
