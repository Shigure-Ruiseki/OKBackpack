package ruiseki.okbackpack.compat.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.client.gui.container.BackpackGuiContainer;
import ruiseki.okbackpack.common.init.ModItems;
import ruiseki.okbackpack.compat.Mods;

public class NEIConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        API.registerGuiOverlay(BackpackGuiContainer.class, "crafting", new BackpackPositioner());
        API.registerGuiOverlayHandler(BackpackGuiContainer.class, new BackpackOverlay(), "crafting");
        API.addRecipeCatalyst(ModItems.CRAFTING_UPGRADE.newItemStack(), "crafting");
        if (Mods.Thaumcraft.isLoaded()) {
            API.addRecipeCatalyst(ModItems.ARCANE_CRAFTING_UPGRADE.newItemStack(), "crafting");
            API.addRecipeCatalyst(ModItems.ARCANE_CRAFTING_UPGRADE.newItemStack(), "thaumcraft.wands");
            API.addRecipeCatalyst(ModItems.ARCANE_CRAFTING_UPGRADE.newItemStack(), "thaumcraft.arcane.shaped");
            API.addRecipeCatalyst(ModItems.ARCANE_CRAFTING_UPGRADE.newItemStack(), "thaumcraft.arcane.shapeless");
        }
    }

    @Override
    public String getName() {
        return Reference.MOD_NAME;
    }

    @Override
    public String getVersion() {
        return Reference.VERSION;
    }
}
