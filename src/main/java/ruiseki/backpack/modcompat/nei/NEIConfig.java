package ruiseki.backpack.modcompat.nei;

import net.minecraft.item.ItemStack;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import ruiseki.backpack.client.gui.container.BackpackGuiContainer;
import ruiseki.backpack.init.item.crafting.ItemCraftingUpgradeConfig;
import ruiseki.backpack.modcompat.thaumcraft.item.ItemArcaneCraftingUpgradeConfig;
import ruiseki.okbackpack.Reference;
import ruiseki.okcore.enums.Mods;

public class NEIConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        if (!NEIModCompat.canBeUsed) return;

        API.registerGuiOverlay(BackpackGuiContainer.class, "crafting", StoragePositioner.INSTANCE);
        API.registerGuiOverlayHandler(BackpackGuiContainer.class, StorageOverlay.INSTANCE, "crafting");
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
            API.registerGuiOverlayHandler(BackpackGuiContainer.class, StorageOverlay.INSTANCE, "crafting");
            API.registerGuiOverlayHandler(BackpackGuiContainer.class, StorageOverlay.INSTANCE, "thaumcraft.wands");
            API.registerGuiOverlayHandler(
                BackpackGuiContainer.class,
                StorageOverlay.INSTANCE,
                "thaumcraft.arcane.shaped");
            API.registerGuiOverlayHandler(
                BackpackGuiContainer.class,
                StorageOverlay.INSTANCE,
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
