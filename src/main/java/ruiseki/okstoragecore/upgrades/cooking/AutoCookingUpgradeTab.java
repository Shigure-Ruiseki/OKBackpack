package ruiseki.okstoragecore.upgrades.cooking;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okcore.recipe.type.cooking.AbstractCookingRecipe;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.filter.FilterLogicControl;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;

public class AutoCookingUpgradeTab<W extends AutoCookingUpgradeWrapper<W, U, R>, U extends ItemUpgradeBase<W> & IAutoCookingUpgradeItem, R extends AbstractCookingRecipe>
    extends ExpandedUpgradeTab<W> {

    public AutoCookingUpgradeTab(int slotIndex, W wrapper, ItemStack stack, StoragePanel storagePanel,
        String titleKey) {
        super(slotIndex, wrapper, stack, storagePanel, titleKey);
    }

    @Override
    protected Flow addUpgradeColumn() {
        return super.addUpgradeColumn().child(
            new FilterLogicControl.Advanced(
                this.getSlotIndex(),
                this.getWrapper()
                    .getInputFilterLogic(),
                "adv_filter"))
            .child(new CookingLogicControl(this.getSlotIndex()))
            .child(
                new FilterLogicControl(
                    this.getSlotIndex(),
                    this.getWrapper()
                        .getInputFilterLogic(),
                    "fuel_filter",
                    wrapper.getFuelFilterLogic()
                        .getFilterHandler()
                        .getSlots()));
    }
}
