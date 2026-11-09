package ruiseki.okbackpack.compat.jfmuy;

import net.minecraft.item.ItemStack;

import ruiseki.backpack.init.item.crafting.ItemCraftingUpgradeConfig;
import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.JFMUYPlugin;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;

@JFMUYPlugin
public class OKBackpackPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        registry.addRecipeCatalyst(
            new ItemStack(ItemCraftingUpgradeConfig._instance.getInstance()),
            VanillaRecipeCategoryUid.CRAFTING);
        registry.getRecipeTransferRegistry()
            .addRecipeTransferHandler(new BackpackCraftingTransferInfo());
    }
}
