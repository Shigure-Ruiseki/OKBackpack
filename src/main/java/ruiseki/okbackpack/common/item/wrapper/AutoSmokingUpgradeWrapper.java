package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;

import ganymedes01.etfuturum.recipes.SmokerRecipes;
import ruiseki.okbackpack.api.IStorageWrapper;

public class AutoSmokingUpgradeWrapper extends AdvancedSmeltingUpgradeWrapperBase {

    public AutoSmokingUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.auto_smoking_settings";
    }

    @Override
    public int getTotalCookTime() {
        return 100;
    }

    @Override
    public ItemStack getSmeltingResult(ItemStack input) {
        if (input == null) return null;
        return SmokerRecipes.smelting()
            .getSmeltingResult(input);
    }
}
