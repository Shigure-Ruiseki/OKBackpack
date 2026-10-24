package ruiseki.okstoragecore.client.gui.widget.filter.oredict;

import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import ruiseki.okbackpack.client.gui.drawble.Outline;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSH;
import ruiseki.okstoragecore.client.gui.widget.filter.FilterLogicControlBase;
import ruiseki.okstoragecore.upgrades.FilterLogic;
import ruiseki.okstoragecore.upgrades.IFilteredUpgrade;

public class OreDictControl extends ParentWidget<OreDictControl> {

    protected final FilterLogicControlBase controller;

    private int selectedIndex = -1;
    private ListWidget<Entry, ?> oreDictList;

    private int addScrollIndex = 0;
    private int removeScrollIndex = -1;

    public OreDictControl(FilterLogicControlBase controller) {
        this.controller = controller;
        // init sync handler
        this.syncHandler("upgrades", this.getSlotIndex());

        this.child(this.addComponent());
    }

    protected Flow addComponent() {
        return Flow.column()
            .child(this.addListOreDict())
            .child(this.addControlRow());
    }

    protected TextFieldWidget addSearchField() {
        return new TextFieldWidget();
    }

    protected ListWidget<?, ?> addListOreDict() {
        ListWidget<Entry, ?> list = new ListWidget<>();
        list.size(88, 49);
        list.collapseDisabledChild();
        this.oreDictList = list;
        this.refreshOreDictList();
        return list;
    }

    protected void refreshOreDictList() {
        if (this.oreDictList == null) return;
        this.oreDictList.removeAll();
        var entries = getFilterLogic().getOreDictEntries();
        if (this.selectedIndex >= entries.size()) {
            this.selectedIndex = -1;
        }
        for (int i = 0; i < entries.size(); i++) {
            final int index = i;
            this.oreDictList.child(
                new Entry(
                    entries.get(i),
                    88,
                    () -> this.selectedIndex == index,
                    () -> { this.selectedIndex = this.selectedIndex == index ? -1 : index; }));
        }
        this.oreDictList.scheduleResize();
    }

    protected Flow addControlRow() {
        return Flow.row()
            .coverChildren()
            .child(this.addFilterSlot())
            .child(this.addAddButton())
            .child(this.addRemoveButton())
            .child(this.addMatchTypeButton());
    }

    protected ItemSlot addFilterSlot() {
        return ItemSlot.create(true);
    }

    protected ButtonWidget<?> addAddButton() {
        return new ButtonWidget<>().size(18)
            .overlay(OSCTextures.ADD)
            .onMouseScrolled((direction, amount) -> {
                this.markTooltipDirty();
                return true;
            })
            .onMousePressed(button -> {
                this.markTooltipDirty();
                return true;
            })
            .tooltipAutoUpdate(true)
            .tooltipDynamic(tooltip -> {

            });
    }

    protected ButtonWidget<?> addRemoveButton() {
        return new ButtonWidget<>().size(18)
            .overlay(OSCTextures.ADD)
            .onMouseScrolled((direction, amount) -> {
                this.markTooltipDirty();
                return true;
            })
            .onMousePressed(button -> {
                this.markTooltipDirty();
                return true;
            })
            .tooltipAutoUpdate(true)
            .tooltipDynamic(tooltip -> {

            });
    }

    protected ButtonWidget<?> addMatchTypeButton() {
        return new ButtonWidget<>();
    }

    public static class Entry extends TextWidget<Entry> implements Interactable {

        private final String text;
        private final BooleanSupplier selected;
        private final Runnable onSelect;

        public Entry(String text, int width, BooleanSupplier selected, Runnable onSelect) {
            super(text);

            this.text = text;
            this.selected = selected;
            this.onSelect = onSelect;

            size(width, 12);
            overlay(new Outline(Color.WHITE.main));
            color(Color.GREY.main);
            shadow(true);
        }

        @Override
        public @NotNull Result onMousePressed(int mouseButton) {
            if (mouseButton != 0) {
                return Result.IGNORE;
            }

            onSelect.run();
            return Result.SUCCESS;
        }

        @Override
        public void drawOverlay(ModularGuiContext context, WidgetThemeEntry widgetTheme) {
            if (!selected.getAsBoolean() && !isHovering()) {
                return;
            }
            super.drawOverlay(context, widgetTheme);
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public String getSelectedEntry() {
        var entries = getFilterLogic().getOreDictEntries();

        if (selectedIndex < 0 || selectedIndex >= entries.size()) {
            return null;
        }

        return entries.get(selectedIndex);
    }

    protected void syncToServer() {

    }

    @Override
    public @NotNull UpgradeSlotSH getSyncHandler() {
        return (UpgradeSlotSH) super.getSyncHandler();
    }

    public FilterLogicControlBase getController() {
        return controller;
    }

    public IFilteredUpgrade getFilteredWrapper() {
        return this.getController()
            .getFilteredWrapper();
    }

    public int getSlotIndex() {
        return this.getController()
            .getSlotIndex();
    }

    public int getSlotCount() {
        return this.getController()
            .getSlotCount();
    }

    public BooleanSupplier getSlotsDisabled() {
        return this.getController()
            .getSlotsDisabled();
    }

    public FilterLogicControlBase.MatchButton[] getShowMatchButtons() {
        return this.getController()
            .getShowMatchButtons();
    }

    public String getSyncKey() {
        return this.getController()
            .getSyncKey();
    }

    public FilterLogic getFilterLogic() {
        return this.getController()
            .getFilterLogic();
    }
}
