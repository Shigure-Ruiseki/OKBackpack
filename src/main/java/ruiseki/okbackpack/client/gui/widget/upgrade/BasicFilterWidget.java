package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import lombok.Getter;
import ruiseki.okbackpack.api.wrapper.IBasicFilterable;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;

public class BasicFilterWidget extends ParentWidget<BasicFilterWidget> {

    private static final List<CyclicVariantButtonWidget.Variant> FILTER_TYPE_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.whitelist"), OKBGuiTextures.CHECK_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.blacklist"), OKBGuiTextures.CROSS_ICON));

    @Getter
    private final CyclicVariantButtonWidget filterTypeButton;
    @Getter
    private final List<ItemSlot> filterSlots;
    @Getter
    private UpgradeSlotSH slotSyncHandler = null;

    public BasicFilterWidget(IBasicFilterable filterableWrapper, int slotIndex, String syncKey) {

        this.syncHandler("upgrades", slotIndex);

        this.filterTypeButton = new CyclicVariantButtonWidget(
            FILTER_TYPE_VARIANTS,
            filterableWrapper.getFilterType()
                .ordinal(),
            index -> {
                filterableWrapper.setFilterType(IBasicFilterable.FilterType.values()[index]);
                if (slotSyncHandler != null) {
                    slotSyncHandler.syncToServer(
                        UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_BASIC_FILTERABLE),
                        buf -> { NetworkUtils.writeEnumValue(buf, filterableWrapper.getFilterType()); });
                }
            }).size(20, 20);

        SlotGroupWidget slotGroup = new SlotGroupWidget().name(syncKey + "s");
        slotGroup.coverChildren()
            .top(26);

        this.filterSlots = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ItemSlot slot = ItemSlot.create(true);
            slot.name(syncKey + "_" + slotIndex)
                .syncHandler(syncKey + "_" + slotIndex, i)
                .pos(i % 3 * 18, i / 3 * 18);

            this.filterSlots.add(slot);
            slotGroup.child(slot);
        }

        child(filterTypeButton).child(slotGroup);
    }

    public BasicFilterWidget(IBasicFilterable filterableWrapper, int slotIndex) {
        this(filterableWrapper, slotIndex, "common_filter");
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        if (syncOrValue instanceof UpgradeSlotSH) {
            this.slotSyncHandler = (UpgradeSlotSH) syncOrValue;
        }
        return this.slotSyncHandler != null;
    }
}
