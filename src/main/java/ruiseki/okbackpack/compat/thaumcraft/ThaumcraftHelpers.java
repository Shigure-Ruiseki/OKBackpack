package ruiseki.okbackpack.compat.thaumcraft;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IArcaneCraftingUpgrade;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.tiles.TileMagicWorkbench;

public class ThaumcraftHelpers {

    public static boolean isWand(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemWandCasting;
    }

    private static TileMagicWorkbench createWorkbenchProxy(IInventory inventory) {
        TileMagicWorkbench proxy = new TileMagicWorkbench();
        int size = Math.min(inventory.getSizeInventory(), proxy.stackList.length);
        for (int i = 0; i < size; i++) {
            proxy.stackList[i] = inventory.getStackInSlot(i);
        }
        return proxy;
    }

    public static ItemStack findMatchingArcaneRecipe(IInventory inventory, EntityPlayer player) {
        TileMagicWorkbench proxy = createWorkbenchProxy(inventory);
        return ThaumcraftCraftingManager.findMatchingArcaneRecipe(proxy, player);
    }

    public static Map<String, Integer> findMatchingArcaneRecipeAspects(IInventory inventory, EntityPlayer player) {
        TileMagicWorkbench proxy = createWorkbenchProxy(inventory);
        AspectList list = ThaumcraftCraftingManager.findMatchingArcaneRecipeAspects(proxy, player);
        Map<String, Integer> result = new LinkedHashMap<>();
        if (list != null && list.size() > 0) {
            for (Aspect a : list.getAspects()) {
                if (a != null) {
                    result.put(a.getTag(), list.getAmount(a));
                }
            }
        }
        return result;
    }

    public static boolean consumeAllVisCrafting(ItemStack wandStack, EntityPlayer player,
        Map<String, Integer> aspects) {
        if (wandStack == null || !(wandStack.getItem() instanceof ItemWandCasting wand)) return false;
        AspectList list = new AspectList();
        for (Map.Entry<String, Integer> entry : aspects.entrySet()) {
            Aspect a = Aspect.getAspect(entry.getKey());
            if (a != null && entry.getValue() > 0) {
                list.add(a, entry.getValue());
            }
        }
        if (list.size() == 0) return true;
        return wand.consumeAllVisCrafting(wandStack, player, list, true);
    }

    public static int getWandVis(ItemStack wandStack, String aspectTag) {
        if (wandStack == null || !(wandStack.getItem() instanceof ItemWandCasting wand)) return 0;
        Aspect aspect = Aspect.getAspect(aspectTag);
        if (aspect == null) return 0;
        return wand.getVis(wandStack, aspect);
    }

    public static int getWandMaxVis(ItemStack wandStack) {
        if (wandStack == null || !(wandStack.getItem() instanceof ItemWandCasting wand)) return 0;
        return wand.getMaxVis(wandStack);
    }

    public static int addRealVis(ItemStack wandStack, String aspectTag, int amount) {
        if (wandStack == null || !(wandStack.getItem() instanceof ItemWandCasting wand)) return 0;
        Aspect aspect = Aspect.getAspect(aspectTag);
        if (aspect == null) return 0;
        return wand.addRealVis(wandStack, aspect, amount, true);
    }

    public static Map<String, Integer> getAspectsWithRoom(ItemStack wandStack) {
        if (wandStack == null || !(wandStack.getItem() instanceof ItemWandCasting wand)) {
            return new LinkedHashMap<>();
        }
        AspectList list = wand.getAspectsWithRoom(wandStack);
        Map<String, Integer> result = new LinkedHashMap<>();
        if (list != null && list.size() > 0) {
            for (Aspect a : list.getAspects()) {
                if (a != null) {
                    int maxVis = wand.getMaxVis(wandStack);
                    int currentVis = wand.getVis(wandStack, a);
                    result.put(a.getTag(), maxVis - currentVis);
                }
            }
        }
        return result;
    }

    public static int getAspectColor(String aspectTag) {
        Aspect aspect = Aspect.getAspect(aspectTag);
        return aspect != null ? aspect.getColor() : 0xFFFFFF;
    }

    public static void writeAspectMap(PacketBuffer buf, Map<String, Integer> aspects) {
        try {
            buf.writeInt(aspects.size());
            for (Map.Entry<String, Integer> entry : aspects.entrySet()) {
                buf.writeStringToBuffer(entry.getKey());
                buf.writeInt(entry.getValue());
            }
        } catch (IOException ignored) {}
    }

    public static Map<String, Integer> readAspectMap(PacketBuffer buf) {
        int size = buf.readInt();
        Map<String, Integer> aspects = new LinkedHashMap<>();
        try {
            for (int i = 0; i < size; i++) {
                String tag = buf.readStringFromBuffer(100);
                int amount = buf.readInt();
                aspects.put(tag, amount);
            }
        } catch (IOException ignored) {}
        return aspects;
    }

    public static boolean isResearchComplete(EntityPlayer player, String researchKey) {
        if (researchKey == null || researchKey.isEmpty()) return true;
        return ThaumcraftApiHelper.isResearchComplete(player.getCommandSenderName(), researchKey);
    }

    public static String getResearchDisplayName(String researchKey) {
        if (researchKey == null || researchKey.isEmpty()) return "";
        ResearchItem item = ResearchCategories.getResearch(researchKey);
        return item != null ? item.getName() : researchKey;
    }

    public static IArcaneRecipe findArcaneRecipeIgnoringResearch(IInventory inventory, EntityPlayer player) {
        TileMagicWorkbench proxy = createWorkbenchProxy(inventory);
        for (Object obj : ThaumcraftApi.getCraftingRecipes()) {
            if (!(obj instanceof IArcaneRecipe recipe)) continue;

            if (matchesItemsOnly(recipe, proxy, player)) {
                return recipe;
            }
        }
        return null;
    }

    public static Map<String, Integer> getArcaneRecipeAspects(IArcaneRecipe recipe) {
        Map<String, Integer> result = new LinkedHashMap<>();
        AspectList list = recipe.getAspects();
        if (list != null && list.size() > 0) {
            for (Aspect a : list.getAspects()) {
                if (a != null) {
                    result.put(a.getTag(), list.getAmount(a));
                }
            }
        }
        return result;
    }

    private static boolean matchesItemsOnly(IArcaneRecipe recipe, TileMagicWorkbench proxy, EntityPlayer player) {
        try {
            return recipe.matches(proxy, player.worldObj, player);
        } catch (Exception ignored) {
            return false;
        }
    }

    public static AspectList getWandAspects(IStorageWrapper wrapper) {
        AspectList list = new AspectList();
        Map<Integer, IArcaneCraftingUpgrade> upgrades = wrapper.gatherCapabilityUpgrades(IArcaneCraftingUpgrade.class);
        for (IArcaneCraftingUpgrade upgrade : upgrades.values()) {
            ItemStack wand = upgrade.getStorage()
                .getStackInSlot(IArcaneCraftingUpgrade.WAND_SLOT_INDEX);
            if (isWand(wand)) {
                ItemWandCasting wandItem = (ItemWandCasting) wand.getItem();
                for (Aspect a : Aspect.getPrimalAspects()) {
                    int vis = wandItem.getVis(wand, a);
                    if (vis > 0) list.add(a, vis);
                }
            }
        }
        return list;
    }

    public static boolean doesWandAcceptAspect(IStorageWrapper wrapper, Aspect aspect) {
        if (aspect == null || !aspect.isPrimal()) return false;
        Map<Integer, IArcaneCraftingUpgrade> upgrades = wrapper.gatherCapabilityUpgrades(IArcaneCraftingUpgrade.class);
        for (IArcaneCraftingUpgrade upgrade : upgrades.values()) {
            ItemStack wand = upgrade.getStorage()
                .getStackInSlot(IArcaneCraftingUpgrade.WAND_SLOT_INDEX);
            if (isWand(wand)) {
                ItemWandCasting wandItem = (ItemWandCasting) wand.getItem();
                if (wandItem.getVis(wand, aspect) < wandItem.getMaxVis(wand)) return true;
            }
        }
        return false;
    }

    public static int addAspectToWands(IStorageWrapper wrapper, Aspect aspect, int amount) {
        if (aspect == null || !aspect.isPrimal()) return amount;
        int remaining = amount;
        Map<Integer, IArcaneCraftingUpgrade> upgrades = wrapper.gatherCapabilityUpgrades(IArcaneCraftingUpgrade.class);
        for (IArcaneCraftingUpgrade upgrade : upgrades.values()) {
            ItemStack wand = upgrade.getStorage()
                .getStackInSlot(IArcaneCraftingUpgrade.WAND_SLOT_INDEX);
            if (isWand(wand)) {
                ItemWandCasting wandItem = (ItemWandCasting) wand.getItem();
                int added = wandItem.addRealVis(wand, aspect, remaining, true);
                remaining -= added;
                if (remaining <= 0) return 0;
            }
        }
        return remaining;
    }

    public static int getWandAspectAmount(IStorageWrapper wrapper, Aspect aspect) {
        if (aspect == null) return 0;
        int total = 0;
        Map<Integer, IArcaneCraftingUpgrade> upgrades = wrapper.gatherCapabilityUpgrades(IArcaneCraftingUpgrade.class);
        for (IArcaneCraftingUpgrade upgrade : upgrades.values()) {
            ItemStack wand = upgrade.getStorage()
                .getStackInSlot(IArcaneCraftingUpgrade.WAND_SLOT_INDEX);
            if (isWand(wand)) {
                ItemWandCasting wandItem = (ItemWandCasting) wand.getItem();
                total += wandItem.getVis(wand, aspect);
            }
        }
        return total;
    }
}
