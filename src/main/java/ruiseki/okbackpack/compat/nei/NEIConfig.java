package ruiseki.okbackpack.compat.nei;

import net.minecraft.item.ItemStack;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.client.gui.container.BackpackGuiContainer;
import ruiseki.okbackpack.common.item.arcane.ItemArcaneCraftingUpgradeConfig;
import ruiseki.okbackpack.common.item.crafting.ItemCraftingUpgradeConfig;
import ruiseki.okbackpack.compat.Mods;

public class NEIConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        API.registerGuiOverlay(BackpackGuiContainer.class, "crafting", new BackpackPositioner());
        API.registerGuiOverlayHandler(BackpackGuiContainer.class, new BackpackOverlay(), "crafting");
        API.addRecipeCatalyst(new ItemStack(ItemCraftingUpgradeConfig._instance.getInstance()), "crafting");
        if (Mods.Thaumcraft.isModLoaded()) {
            API.addRecipeCatalyst(new ItemStack(ItemArcaneCraftingUpgradeConfig._instance.getInstance()), "crafting");
            API.addRecipeCatalyst(
                new ItemStack(ItemArcaneCraftingUpgradeConfig._instance.getInstance()),
                "thaumcraft.wands");
            API.addRecipeCatalyst(
                new ItemStack(ItemArcaneCraftingUpgradeConfig._instance.getInstance()),
                "thaumcraft.arcane.shaped");
            API.addRecipeCatalyst(
                new ItemStack(ItemArcaneCraftingUpgradeConfig._instance.getInstance()),
                "thaumcraft.arcane.shapeless");
            API.registerGuiOverlayHandler(BackpackGuiContainer.class, new BackpackOverlay(), "crafting");
            API.registerGuiOverlayHandler(BackpackGuiContainer.class, new BackpackOverlay(), "thaumcraft.wands");
            API.registerGuiOverlayHandler(
                BackpackGuiContainer.class,
                new BackpackOverlay(),
                "thaumcraft.arcane.shaped");
            API.registerGuiOverlayHandler(
                BackpackGuiContainer.class,
                new BackpackOverlay(),
                "thaumcraft.arcane.shapeless");
        }
    }

    @Override
    public String getName() {
        return Reference.MOD_NAME;
    }

    @Override
    public String getVersion() {
        return Reference.MOD_VERSION;
    }
}
