package ruiseki.backpack.client.gui.slot;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.widget.StoragePanel;

public class StorageSlot extends ItemSlot {

    private final IStorageWrapper wrapper;
    private final StoragePanel panel;

    public StorageSlot(IStorageWrapper wrapper, StoragePanel panel) {
        this.wrapper = wrapper;
        this.panel = panel;
    }

    @Override
    public void buildTooltip(ItemStack stack, RichTooltip tooltip) {
        if (stack == null) {
            super.buildTooltip(stack, tooltip);
            return;
        }

        super.buildTooltip(stack, tooltip);

        NumberFormat.Params params = NumberFormat.DEFAULT.copyToBuilder()
            .build();
        params.format.setMaximumFractionDigits(2);
        params.format.setMinimumFractionDigits(0);

        String count = NumberFormat.format(stack.stackSize, params);
        String limit = NumberFormat.format(
            wrapper.getInventoryHandler()
                .getStackLimit(getSlot().getSlotIndex(), stack),
            params);

        tooltip.addLine(IKey.lang("gui.storage.stack_size_extra", count, limit));
    }
}
