package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import ruiseki.okbackpack.api.IStorageWrapper;

public class SmeltingUpgradeWrapper extends SmeltingUpgradeWrapperBase {

    public SmeltingUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.smelting_settings";
    }

    @Override
    public ItemStack getSmeltingResult(ItemStack input) {
        if (input == null) return null;
        return FurnaceRecipes.smelting()
            .getSmeltingResult(input);
    }
}
