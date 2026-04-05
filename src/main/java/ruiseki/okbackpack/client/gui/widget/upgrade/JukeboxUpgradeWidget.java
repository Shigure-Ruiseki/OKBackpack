package ruiseki.okbackpack.client.gui.widget.upgrade;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.common.item.jukebox.JukeboxUpgradeWrapper;

public class JukeboxUpgradeWidget extends ExpandedUpgradeTabWidget<JukeboxUpgradeWrapper> {

    private final JukeboxUpgradeWrapper wrapper;

    public JukeboxUpgradeWidget(int slotIndex, JukeboxUpgradeWrapper wrapper, ItemStack stack, IStoragePanel<?> panel,
        String titleKey) {
        super(slotIndex, 3, stack, titleKey, 75);
        this.wrapper = wrapper;

        ItemSlot recordSlot = new ItemSlot() {

            @Override
            public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
                if (wrapper.isPlaying() && wrapper.getCurrentSlotIndex() == 0) {
                    IDrawable.EMPTY.draw(context, 0, 0, 18, 18, widgetTheme.getTheme());
                    GuiDraw.drawRect(1, 1, 16, 16, 0x4400FF00);
                }
                super.draw(context, widgetTheme);
            }
        };
        recordSlot.syncHandler("jukebox_handler_" + slotIndex, 0)
            .pos(0, 0)
            .name("record_slot_0");

        SlotGroupWidget slotGroup = new SlotGroupWidget().name("jukebox_slots")
            .coverChildren();
        slotGroup.child(recordSlot);

        ButtonWidget<?> stopButton = new ButtonWidget<>().overlay(OKBGuiTextures.JUKEBOX_STOP_ICON)
            .size(18, 18)
            .tooltipDynamic(tooltip -> tooltip.addLine(IKey.lang("gui.backpack.jukebox_stop")))
            .onMousePressed(button -> {
                if (button == 0) {
                    Interactable.playButtonClickSound();
                    wrapper.stop();
                    if (getSlotSyncHandler() != null) {
                        getSlotSyncHandler()
                            .syncToServer(UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_JUKEBOX_STOP), buf -> {});
                    }
                    return true;
                }
                return false;
            });

        ButtonWidget<?> playButton = new ButtonWidget<>().overlay(OKBGuiTextures.JUKEBOX_PLAY_ICON)
            .size(18, 18)
            .tooltipDynamic(tooltip -> tooltip.addLine(IKey.lang("gui.backpack.jukebox_play")))
            .onMousePressed(button -> {
                if (button == 0) {
                    Interactable.playButtonClickSound();
                    wrapper.play();
                    if (getSlotSyncHandler() != null) {
                        getSlotSyncHandler()
                            .syncToServer(UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_JUKEBOX_PLAY), buf -> {});
                    }
                    return true;
                }
                return false;
            });

        Row buttonRow = (Row) new Row().coverChildrenHeight()
            .leftRel(0.5f)
            .childPadding(2)
            .child(stopButton)
            .child(playButton);

        Column column = (Column) new Column().pos(8, 28)
            .coverChildren()
            .childPadding(2)
            .child(slotGroup)
            .child(buttonRow);

        child(column);
    }

    @Override
    protected JukeboxUpgradeWrapper getWrapper() {
        return wrapper;
    }
}
