package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget.Variant;
import ruiseki.okbackpack.client.gui.widget.updateGroup.AdvancedPumpFilterSlotGroupFactory;
import ruiseki.okbackpack.common.item.pump.AdvancedPumpUpgradeWrapper;
import ruiseki.okbackpack.common.item.pump.FluidFilterType;

public class AdvancedPumpUpgradeWidget extends PumpUpgradeWidget<AdvancedPumpUpgradeWrapper> {

    private static final List<Variant> HAND_VARIANTS = Arrays.asList(
        new Variant(IKey.lang("gui.backpack.pump.interact_with_hand"), OKBGuiTextures.PUMP_INTERACT_HAND_ICON),
        new Variant(
            IKey.lang("gui.backpack.pump.do_not_interact_with_hand"),
            OKBGuiTextures.PUMP_NO_INTERACT_HAND_ICON));

    private static final List<Variant> WORLD_VARIANTS = Arrays.asList(
        new Variant(IKey.lang("gui.backpack.pump.interact_with_world"), OKBGuiTextures.PUMP_INTERACT_WORLD_ICON),
        new Variant(
            IKey.lang("gui.backpack.pump.do_not_interact_with_world"),
            OKBGuiTextures.PUMP_NO_INTERACT_WORLD_ICON));

    private static final List<Variant> HANDLERS_VARIANTS = Arrays.asList(
        new Variant(
            IKey.lang("gui.backpack.pump.interact_with_tanks_and_pipes"),
            OKBGuiTextures.PUMP_INTERACT_TANKS_ICON),
        new Variant(
            IKey.lang("gui.backpack.pump.do_not_interact_with_tanks_and_pipes"),
            OKBGuiTextures.PUMP_NO_INTERACT_TANKS_ICON));

    private static final List<Variant> FILTER_TYPE_VARIANTS = Arrays.asList(
        new Variant(IKey.lang("gui.backpack.pump.fluid_filter_allow"), OKBGuiTextures.CHECK_ICON),
        new Variant(IKey.lang("gui.backpack.pump.fluid_filter_block"), OKBGuiTextures.CROSS_ICON));

    public AdvancedPumpUpgradeWidget(int slotIndex, AdvancedPumpUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        super(slotIndex, wrapper, stack, panel, titleKey, 5, 95);
        height(100);

        Flow filterRow = Flow.row()
            .coverChildren()
            .pos(8, 51)
            .childPadding(0);
        filterRow.child(buildFilterTypeButton());

        FluidSlotSyncHandler[] handlers = panel.getUpgradeSlotUpdateGroup(slotIndex)
            .get(AdvancedPumpFilterSlotGroupFactory.GROUP_HANDLERS_KEY);
        if (handlers != null) {
            for (int i = 0; i < AdvancedPumpUpgradeWrapper.FILTER_SLOTS; i++) {
                FluidSlot slot = new FluidSlot();
                slot.syncHandler(handlers[i]);
                slot.pos(8 + i * 18, 72);
                child(slot);
            }
        }
        child(filterRow);
    }

    @Override
    protected void addBaseButtons(Flow row) {
        row.child(buildHandButton());
        row.child(buildWorldButton());
        row.child(buildHandlersButton());
    }

    protected CyclicVariantButtonWidget buildHandButton() {
        return new CyclicVariantButtonWidget(HAND_VARIANTS, wrapper.shouldInteractWithHand() ? 0 : 1, index -> {
            wrapper.setInteractWithHand(index == 0);
            syncBoolean(UpgradeSlotSHRegisters.UPDATE_PUMP_HAND, wrapper.shouldInteractWithHand());
        });
    }

    protected CyclicVariantButtonWidget buildWorldButton() {
        return new CyclicVariantButtonWidget(WORLD_VARIANTS, wrapper.shouldInteractWithWorld() ? 0 : 1, index -> {
            wrapper.setInteractWithWorld(index == 0);
            syncBoolean(UpgradeSlotSHRegisters.UPDATE_PUMP_WORLD, wrapper.shouldInteractWithWorld());
        });
    }

    protected CyclicVariantButtonWidget buildHandlersButton() {
        return new CyclicVariantButtonWidget(
            HANDLERS_VARIANTS,
            wrapper.shouldInteractWithFluidHandlers() ? 0 : 1,
            index -> {
                wrapper.setInteractWithFluidHandlers(index == 0);
                syncBoolean(UpgradeSlotSHRegisters.UPDATE_PUMP_HANDLERS, wrapper.shouldInteractWithFluidHandlers());
            });
    }

    protected CyclicVariantButtonWidget buildFilterTypeButton() {
        return new CyclicVariantButtonWidget(
            FILTER_TYPE_VARIANTS,
            wrapper.getFilterType()
                .ordinal(),
            index -> {
                FluidFilterType type = FluidFilterType.values()[index];
                wrapper.setFilterType(type);
                if (getSlotSyncHandler() != null) {
                    getSlotSyncHandler().syncToServer(
                        UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_PUMP_FILTER_TYPE),
                        buf -> NetworkUtils.writeEnumValue(buf, type));
                }
            });
    }
}
