package ruiseki.okbackpack.common.item.smelter;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import ruiseki.okbackpack.api.IStorageWrapper;

public class AutoSmeltingUpgradeWrapper extends AdvancedSmeltingUpgradeWrapperBase {

    public AutoSmeltingUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.auto_smelting_settings";
    }

    @Override
    public ItemStack getSmeltingResult(ItemStack input) {
        if (input == null) return null;
        return FurnaceRecipes.smelting()
            .getSmeltingResult(input);
    }
}
