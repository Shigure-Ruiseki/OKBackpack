package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget.Variant;
import ruiseki.okbackpack.common.item.pump.PumpUpgradeWrapper;

public class PumpUpgradeWidget<W extends PumpUpgradeWrapper> extends ExpandedUpgradeTabWidget<W> {

    protected final W wrapper;

    protected static final List<Variant> DIRECTION_VARIANTS = Arrays.asList(
        new Variant(IKey.lang("gui.backpack.pump.input"), OKBGuiTextures.PUMP_INPUT_ICON),
        new Variant(IKey.lang("gui.backpack.pump.output"), OKBGuiTextures.PUMP_OUTPUT_ICON));

    public PumpUpgradeWidget(int slotIndex, W wrapper, ItemStack stack, IStoragePanel<?> panel, String titleKey) {
        this(slotIndex, wrapper, stack, panel, titleKey, 2, 60);
    }

    public PumpUpgradeWidget(int slotIndex, W wrapper, ItemStack stack, IStoragePanel<?> panel, String titleKey,
        int coveredTabSize, int width) {
        super(slotIndex, coveredTabSize, stack, panel, titleKey, width);
        this.wrapper = wrapper;

        Flow buttonRow = Flow.row()
            .coverChildren()
            .pos(8, 28)
            .childPadding(0);
        buttonRow.child(buildDirectionButton());
        addBaseButtons(buttonRow);
        child(buttonRow);
    }

    /**
     * Subclass hook – the basic pump only shows the direction button. The advanced pump appends
     * its hand / world / fluid-handler toggle buttons through this hook so the row layout stays
     * consistent (padding 0, same row).
     */
    protected void addBaseButtons(Flow row) {}

    @Override
    protected W getWrapper() {
        return wrapper;
    }

    protected CyclicVariantButtonWidget buildDirectionButton() {
        return new CyclicVariantButtonWidget(DIRECTION_VARIANTS, wrapper.isInput() ? 0 : 1, index -> {
            wrapper.setInput(index == 0);
            syncBoolean(UpgradeSlotSHRegisters.UPDATE_PUMP_INPUT, wrapper.isInput());
        });
    }

    protected void syncBoolean(String registerKey, boolean value) {
        if (getSlotSyncHandler() != null) {
            getSlotSyncHandler().syncToServer(UpgradeSlotSH.getId(registerKey), buf -> buf.writeBoolean(value));
        }
    }
}
