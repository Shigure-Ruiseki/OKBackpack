package ruiseki.okbackpack.compat.jfmuy;

import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.JFMUYPlugin;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.okbackpack.common.item.crafting.ItemCraftingUpgradeConfig;

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
