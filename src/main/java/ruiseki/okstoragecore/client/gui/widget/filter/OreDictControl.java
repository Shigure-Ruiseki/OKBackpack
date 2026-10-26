package ruiseki.okstoragecore.client.gui.widget.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.oredict.OreDictionary;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;

import ruiseki.okbackpack.Reference;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSH;
import ruiseki.okstoragecore.upgrades.FilterLogic;
import ruiseki.okstoragecore.upgrades.IFilteredUpgrade;

public class OreDictControl extends ParentWidget<OreDictControl> {

    private static final UITexture BACKGROUND_TILE_TEXTURE = UITexture.builder()
        .location(Reference.MOD_ID, "gui/gui_controls")
        .imageSize(256, 256)
        .xy(29, 146, 66, 56)
        .adaptable(1)
        .tiled()
        .build();

    protected final FilterLogicControlBase controller;

    private ListWidget<Entry, ?> oreDictList;
    private ItemSlot filterSlot;

    private int addScrollIndex = 0;
    private int removeScrollIndex = -1;

    public OreDictControl(FilterLogicControlBase controller) {
        this.controller = controller;
        this.coverChildren();
        this.syncHandler("upgrades", this.getSlotIndex());
        this.child(this.addContents());
    }

    protected Flow addContents() {
        return Flow.column()
            .coverChildren()
            .child(this.addListOreDict())
            .child(this.addControlRow());
    }

    protected ListWidget<?, ?> addListOreDict() {
        ListWidget<Entry, ?> list = new ListWidget<>();
        list.size(88, 49);
        list.left(0);
        list.collapseDisabledChild();
        list.background(BACKGROUND_TILE_TEXTURE);
        list.tooltipAutoUpdate(true);
        list.tooltipDynamic(tooltip -> {
            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
            List<String> entries = this.getFilterLogic()
                .getOreDictEntries();
            if (entries.isEmpty()) return;
            tooltip.addLine(IKey.lang("gui.okstoragecore.upgrades.buttons.ore_dict_list.title"));
            for (String entry : entries) {
                tooltip.addLine(
                    IKey.str("> " + entry)
                        .style(EnumChatFormatting.GRAY));
            }
        });
        this.oreDictList = list;
        this.refreshOreDictList();
        return list;
    }

    protected void refreshOreDictList() {
        if (this.oreDictList == null) return;
        this.oreDictList.removeAll();
        var entries = getFilterLogic().getOreDictEntries();
        for (int i = 0; i < entries.size(); i++) {
            this.oreDictList.child(new Entry(entries.get(i), 88));
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
        this.filterSlot = new PhantomItemSlot().syncHandler("adv_filter_oredict_" + this.getSlotIndex(), 0);
        return this.filterSlot;
    }

    protected ButtonWidget<?> addAddButton() {
        return new ButtonWidget<>().size(18)
            .overlay(OSCTextures.ADD)
            .onMouseScrolled((direction, amount) -> {
                List<String> available = this.getAvailableOreDicts();
                if (available.isEmpty()) return true;
                if (direction == UpOrDown.UP) {
                    this.addScrollIndex = (this.addScrollIndex - 1 + available.size()) % available.size();
                } else {
                    this.addScrollIndex = (this.addScrollIndex + 1) % available.size();
                }
                this.markTooltipDirty();
                return true;
            })
            .onMousePressed(button -> {
                if (button != 0) return false;
                List<String> available = this.getAvailableOreDicts();
                if (available.isEmpty()) return false;
                if (this.addScrollIndex < 0 || this.addScrollIndex >= available.size()) {
                    this.addScrollIndex = 0;
                }
                String selected = available.get(this.addScrollIndex);
                this.getFilterLogic()
                    .addOreDictEntry(selected);
                this.refreshOreDictList();
                this.addScrollIndex = 0;
                this.markTooltipDirty();
                return true;
            })
            .tooltipAutoUpdate(true)
            .tooltipDynamic(tooltip -> {
                tooltip.addLine(IKey.lang("gui.okstoragecore.upgrades.buttons.add_ore_dict"));
                tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);

                ItemStack stack = this.filterSlot.isSynced() ? this.filterSlot.getSlot()
                    .getStack() : ItemHelpers.EMPTY;
                if (ItemHelpers.isEmpty(stack)) {
                    tooltip.addLine(
                        IKey.lang("gui.okstoragecore.upgrades.buttons.add_ore_dict.no_item")
                            .style(IKey.GRAY, IKey.ITALIC));
                    return;
                }

                List<String> available = this.getAvailableOreDicts();
                if (available.isEmpty()) {
                    tooltip.addLine(
                        IKey.lang("gui.okstoragecore.upgrades.buttons.add_ore_dict.no_additional")
                            .style(IKey.YELLOW, IKey.ITALIC));
                    return;
                }

                if (this.addScrollIndex >= available.size()) this.addScrollIndex = 0;
                for (int i = 0; i < available.size(); i++) {
                    String entry = available.get(i);
                    if (i == this.addScrollIndex) {
                        tooltip.addLine(
                            IKey.str("-> " + entry)
                                .style(EnumChatFormatting.GREEN));
                    } else {
                        tooltip.addLine(
                            IKey.str("> " + entry)
                                .style(EnumChatFormatting.GRAY));
                    }
                }
                tooltip.addLine(
                    IKey.lang("gui.okstoragecore.upgrades.buttons.add_ore_dict.controls")
                        .style(IKey.GRAY, IKey.ITALIC));
            });
    }

    protected ButtonWidget<?> addRemoveButton() {
        return new ButtonWidget<>().size(18)
            .overlay(OSCTextures.REMOVE)
            .onMouseScrolled((direction, amount) -> {
                List<String> entries = this.getFilterLogic()
                    .getOreDictEntries();

                if (entries.isEmpty()) {
                    this.removeScrollIndex = -1;
                    return true;
                }

                if (this.removeScrollIndex < 0 || this.removeScrollIndex >= entries.size()) {
                    this.removeScrollIndex = direction == UpOrDown.UP ? entries.size() - 1 : 0;
                } else if (direction == UpOrDown.UP) {
                    this.removeScrollIndex = (this.removeScrollIndex - 1 + entries.size()) % entries.size();
                } else {
                    this.removeScrollIndex = (this.removeScrollIndex + 1) % entries.size();
                }

                this.markTooltipDirty();
                return true;
            })
            .onMousePressed(button -> {
                if (button != 0) return false;
                List<String> entries = this.getFilterLogic()
                    .getOreDictEntries();
                if (entries.isEmpty()) {
                    this.removeScrollIndex = -1;
                    return false;
                }

                if (this.removeScrollIndex < 0 || this.removeScrollIndex >= entries.size()) {
                    this.removeScrollIndex = 0;
                }

                int index = this.removeScrollIndex;
                if (!this.getFilterLogic()
                    .removeOreDictEntry(index)) {
                    return false;
                }

                this.refreshOreDictList();
                int remaining = this.getFilterLogic()
                    .getOreDictEntries()
                    .size();
                if (remaining == 0) {
                    this.removeScrollIndex = -1;
                } else if (this.removeScrollIndex >= remaining) {
                    this.removeScrollIndex = remaining - 1;
                }
                this.markTooltipDirty();
                return true;
            })
            .tooltipAutoUpdate(true)
            .tooltipDynamic(tooltip -> {
                tooltip.addLine(IKey.lang("gui.okstoragecore.upgrades.buttons.remove_ore_dict"));
                tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);

                List<String> entries = this.getFilterLogic()
                    .getOreDictEntries();

                if (entries.isEmpty()) {
                    tooltip.addLine(
                        IKey.lang("gui.okstoragecore.upgrades.buttons.remove_ore_dict.empty")
                            .style(EnumChatFormatting.RED));
                    return;
                }

                for (int i = 0; i < entries.size(); i++) {
                    String entry = entries.get(i);

                    tooltip.addLine(
                        IKey.str((i == this.removeScrollIndex ? "-> " : "> ") + entry)
                            .style(i == this.removeScrollIndex ? EnumChatFormatting.RED : EnumChatFormatting.GRAY));
                }

                tooltip.addLine(
                    IKey.lang("gui.okstoragecore.upgrades.buttons.remove_ore_dict.controls")
                        .style(IKey.GRAY, IKey.ITALIC));
            });
    }

    protected ButtonWidget<?> addMatchTypeButton() {
        return new ButtonWidget<>();
    }

    private List<String> getAvailableOreDicts() {
        ItemStack stack = this.filterSlot.isSynced() ? this.filterSlot.getSlot()
            .getStack() : null;
        if (stack == null) return List.of();

        int[] ids = OreDictionary.getOreIDs(stack);
        if (ids.length == 0) return List.of();

        List<String> existing = this.getFilterLogic()
            .getOreDictEntries();
        List<String> available = new ArrayList<>();
        for (int id : ids) {
            String oreName = OreDictionary.getOreName(id);
            if (!existing.contains(oreName)) {
                available.add(oreName);
            }
        }
        return available;
    }

    public static class Entry extends TextWidget<Entry> {

        public Entry(String text, int width) {
            super(text);
            this.size(width, 12);
            this.color(Color.GREY.main);
            this.shadow(true);
            this.padding(2, 0);
        }
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue instanceof UpgradeSlotSH;
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
