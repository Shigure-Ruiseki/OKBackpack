package ruiseki.okbackpack.compat.jfmuy;

import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.JFMUYPlugin;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.okbackpack.common.init.OKBackpackItems;

@JFMUYPlugin
public class OKBackpackPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        if (OKBackpackItems.CRAFTING_UPGRADE.isPresent()) {
            registry.addRecipeCatalyst(
                new ItemStack(OKBackpackItems.CRAFTING_UPGRADE.get()),
                VanillaRecipeCategoryUid.CRAFTING);
        }
        registry.getRecipeTransferRegistry()
            .addRecipeTransferHandler(new BackpackCraftingTransferInfo());
    }
}
