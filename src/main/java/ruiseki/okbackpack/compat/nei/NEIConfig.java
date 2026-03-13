package ruiseki.okbackpack.compat.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.client.gui.container.BackpackGuiContainer;

public class NEIConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        API.registerGuiOverlay(BackpackGuiContainer.class, "crafting", new BackpackPositioner());
        API.registerGuiOverlayHandler(BackpackGuiContainer.class, new BackpackOverlay(), "crafting");
        // API.addRecipeCatalyst(BackpackItems.CRAFTING_UPGRADE.newItemStack(), "crafting");
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
