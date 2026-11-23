package ruiseki.okbackpack.modcompat.jfmuy;

import net.minecraft.item.ItemStack;

import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.api.IModRegistry;
import ruiseki.jfmuy.api.JFMUYPlugin;
import ruiseki.jfmuy.api.recipe.VanillaRecipeCategoryUid;
import ruiseki.okbackpack.init.item.crafting.ItemCraftingUpgradeConfig;

@JFMUYPlugin
public class OKBackpackPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        if (!JFMUYModCompat.canBeUsed) return;

        registry.addRecipeCatalyst(
            new ItemStack(ItemCraftingUpgradeConfig._instance.getInstance()),
            VanillaRecipeCategoryUid.CRAFTING);
        registry.getRecipeTransferRegistry()
            .addRecipeTransferHandler(new StorageCraftingTransferInfo());
    }
}
