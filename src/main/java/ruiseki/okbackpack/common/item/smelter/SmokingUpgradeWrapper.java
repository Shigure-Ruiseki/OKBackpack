package ruiseki.okbackpack.common.item.smelter;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ganymedes01.etfuturum.recipes.SmokerRecipes;
import ruiseki.okbackpack.api.IStorageWrapper;

public class SmokingUpgradeWrapper extends SmeltingUpgradeWrapperBase {

    public SmokingUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.smoking_settings";
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
