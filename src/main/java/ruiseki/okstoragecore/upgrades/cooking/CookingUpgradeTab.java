package ruiseki.okstoragecore.upgrades.cooking;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okcore.recipe.type.cooking.AbstractCookingRecipe;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;

public class CookingUpgradeTab<W extends CookingUpgradeWrapper<W, U, R>, U extends ItemUpgradeBase<W> & ICookingUpgradeItem, R extends AbstractCookingRecipe>
    extends ExpandedUpgradeTab<W> {

    public CookingUpgradeTab(int slotIndex, W wrapper, ItemStack stack, StoragePanel storagePanel, String titleKey) {
        super(slotIndex, wrapper, stack, storagePanel, titleKey);
    }

    @Override
    protected Flow addUpgradeColumn() {
        return super.addUpgradeColumn().child(new CookingLogicControl(this.getSlotIndex()));
    }
}
