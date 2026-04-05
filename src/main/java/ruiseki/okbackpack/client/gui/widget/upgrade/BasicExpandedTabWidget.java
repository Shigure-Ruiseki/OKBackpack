package ruiseki.okbackpack.client.gui.widget.upgrade;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;

import ruiseki.okbackpack.api.wrapper.IBasicFilterable;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;

public class BasicExpandedTabWidget<T extends UpgradeWrapperBase & IBasicFilterable>
    extends ExpandedUpgradeTabWidget<T> {

    protected final T wrapper;
    protected final Row startingRow;
    protected final BasicFilterWidget filterWidget;

    public BasicExpandedTabWidget(int slotIndex, T wrapper, ItemStack delegatedIconStack, String titleKey,
        String filterSyncKey, int coveredTabSize, int width) {
        super(slotIndex, coveredTabSize, delegatedIconStack, titleKey, width);

        this.wrapper = wrapper;

        this.startingRow = (Row) new Row().height(0)
            .name("starting_row");

        this.filterWidget = new BasicFilterWidget(wrapper, slotIndex, filterSyncKey).width(64)
            .coverChildrenHeight()
            .name("filter_widget");

        Column column = (Column) new Column().pos(8, 28)
            .coverChildren()
            .childPadding(2)
            .child(startingRow)
            .child(filterWidget);

        child(column);
    }

    public BasicExpandedTabWidget(int slotIndex, T wrapper, ItemStack delegatedIconStack, String titleKey) {
        this(slotIndex, wrapper, delegatedIconStack, titleKey, "common_filter", 4, 75);
    }

    public BasicExpandedTabWidget(int slotIndex, T wrapper, ItemStack delegatedIconStack, String titleKey,
        String filterSyncKey) {
        this(slotIndex, wrapper, delegatedIconStack, titleKey, filterSyncKey, 4, 75);
    }

    @Override
    public T getWrapper() {
        return wrapper;
    }
}
