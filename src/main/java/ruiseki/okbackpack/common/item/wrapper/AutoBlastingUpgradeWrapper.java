package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;

import ganymedes01.etfuturum.recipes.BlastFurnaceRecipes;
import ruiseki.okbackpack.api.IStorageWrapper;

public class AutoBlastingUpgradeWrapper extends AdvancedSmeltingUpgradeWrapperBase {

    public AutoBlastingUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.auto_blasting_settings";
    }

    @Override
    public int getTotalCookTime() {
        return 100;
    }

    @Override
    public ItemStack getSmeltingResult(ItemStack input) {
        if (input == null) return null;
        return BlastFurnaceRecipes.smelting()
            .getSmeltingResult(input);
    }
}
