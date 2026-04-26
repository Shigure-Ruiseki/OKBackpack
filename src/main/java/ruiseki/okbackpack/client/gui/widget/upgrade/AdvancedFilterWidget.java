package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.oredict.OreDictionary;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import lombok.Getter;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.wrapper.IAdvancedFilterable;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.drawble.Outline;
import ruiseki.okbackpack.client.gui.slot.DisableablePhantomItemSlot;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;

public class AdvancedFilterWidget extends ParentWidget<AdvancedFilterWidget> {

    private static final CyclicVariantButtonWidget.Variant[] FILTER_TYPE_VARIANTS = new CyclicVariantButtonWidget.Variant[] {
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.whitelist"), OKBGuiTextures.CHECK_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.blacklist"), OKBGuiTextures.CROSS_ICON) };

    private static final CyclicVariantButtonWidget.Variant[] MATCH_TYPE_VARIANTS = new CyclicVariantButtonWidget.Variant[] {
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.match_item"), OKBGuiTextures.BY_ITEM_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.match_mod_id"), OKBGuiTextures.BY_MOD_ID_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.match_ore_dict"),
            OKBGuiTextures.MATCH_ORE_DICT_ICON) };

    private static final CyclicVariantButtonWidget.Variant[] IGNORE_DURABILITY_VARIANTS = new CyclicVariantButtonWidget.Variant[] {
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.match_durability"),
            OKBGuiTextures.MATCH_DURABILITY_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.ignore_durability"),
            OKBGuiTextures.IGNORE_DURABILITY_ICON) };

    private static final CyclicVariantButtonWidget.Variant[] IGNORE_NBT_VARIANTS = new CyclicVariantButtonWidget.Variant[] {
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.match_nbt"), OKBGuiTextures.MATCH_NBT_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.ignore_nbt"), OKBGuiTextures.IGNORE_NBT_ICON) };

    private static final CyclicVariantButtonWidget.Variant[] MATCH_ALL_ORE_DICT_VARIANTS = new CyclicVariantButtonWidget.Variant[] {
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.match_any_ore_dict"),
            OKBGuiTextures.ONE_IN_FOUR_SLOT_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.match_all_ore_dict"),
            OKBGuiTextures.ALL_FOUR_SLOT_ICON) };

    @Getter
    private CyclicVariantButtonWidget filterTypeButton;
    @Getter
    private final CyclicVariantButtonWidget matchTypeButton;
    @Getter
    private final CyclicVariantButtonWidget ignoreDurabilityButton;
    @Getter
    private final CyclicVariantButtonWidget ignoreNBTButton;
    @Getter
    private final CyclicVariantButtonWidget matchAllOreDictButton;

    @Getter
    private final Flow itemBasedConfigurationGroup;
    @Getter
    private final Flow oreDictBasedConfigurationGroup;
    @Getter
    private final List<ItemSlot> filterSlots;

    private final OreDictRegexListWidget oreDictList;

    @Getter
    private UpgradeSlotSH slotSyncHandler = null;

    private final IAdvancedFilterable filterableWrapper;
    private final Flow buttonRow;

    private BooleanSupplier slotsDisabled = () -> false;

    // State for ore dict add/remove scroll selection
    private int addScrollIndex = 0;
    private int removeScrollIndex = -1;

    public AdvancedFilterWidget(int slotIndex, IAdvancedFilterable filterableWrapper, String syncKey) {
        this(slotIndex, filterableWrapper, syncKey, 16);
    }

    public AdvancedFilterWidget(int slotIndex, IAdvancedFilterable filterableWrapper, String syncKey,
        int filterSlotCount) {
        this.filterableWrapper = filterableWrapper;

        // init sync handler
        syncHandler("upgrades", slotIndex);

        // Buttons
        this.filterTypeButton = new CyclicVariantButtonWidget(
            Arrays.asList(FILTER_TYPE_VARIANTS),
            filterableWrapper.getFilterType()
                .ordinal(),
            index -> {
                filterableWrapper.setFilterType(IAdvancedFilterable.FilterType.values()[index]);
                updateWrapper();
            });

        this.matchTypeButton = new CyclicVariantButtonWidget(
            Arrays.asList(MATCH_TYPE_VARIANTS),
            filterableWrapper.getMatchType()
                .ordinal(),
            index -> {
                filterableWrapper.setMatchType(IAdvancedFilterable.MatchType.values()[index]);
                updateWrapper();
            });

        boolean inEffect = filterableWrapper.getMatchType() == IAdvancedFilterable.MatchType.ITEM;

        this.ignoreDurabilityButton = new CyclicVariantButtonWidget(
            Arrays.asList(IGNORE_DURABILITY_VARIANTS),
            filterableWrapper.isIgnoreDurability() ? 1 : 0,
            index -> {
                filterableWrapper.setIgnoreDurability(index == 1);
                updateWrapper();
            });
        this.ignoreDurabilityButton.setInEffect(inEffect);

        this.ignoreNBTButton = new CyclicVariantButtonWidget(
            Arrays.asList(IGNORE_NBT_VARIANTS),
            filterableWrapper.isIgnoreNBT() ? 1 : 0,
            index -> {
                filterableWrapper.setIgnoreNBT(index == 1);
                updateWrapper();
            });
        this.ignoreNBTButton.setInEffect(inEffect);

        this.matchAllOreDictButton = new CyclicVariantButtonWidget(
            Arrays.asList(MATCH_ALL_ORE_DICT_VARIANTS),
            filterableWrapper.isMatchAllOreDicts() ? 1 : 0,
            index -> {
                filterableWrapper.setMatchAllOreDicts(index == 1);
                updateWrapper();
            }).size(18);

        // Add buttons to rows
        this.buttonRow = Flow.row()
            .leftRel(0.5f)
            .size(88, 20)
            .childPadding(2);

        Flow itemBasedConfigButtonRow = Flow.row()
            .childPadding(2)
            .size(44, 20)
            .left(44);
        itemBasedConfigButtonRow.child(ignoreDurabilityButton)
            .child(ignoreNBTButton);
        itemBasedConfigButtonRow
            .setEnabledIf(flow -> filterableWrapper.getMatchType() == IAdvancedFilterable.MatchType.ITEM);

        buttonRow.child(filterTypeButton)
            .child(matchTypeButton)
            .child(itemBasedConfigButtonRow);

        // Slots
        SlotGroupWidget slotGroup = new SlotGroupWidget().coverChildren()
            .leftRel(0.5f);

        this.filterSlots = new ArrayList<>();
        for (int i = 0; i < filterSlotCount; i++) {
            ItemSlot slot = new DisableablePhantomItemSlot(() -> this.slotsDisabled.getAsBoolean());
            slot.name(syncKey + "_" + slotIndex)
                .syncHandler(syncKey + "_" + slotIndex, i)
                .pos(i % 4 * 18, i / 4 * 18);

            this.filterSlots.add(slot);
            slotGroup.child(slot);
        }

        int filterSlotRows = (filterSlotCount + 3) / 4;
        int itemGroupHeight = filterSlotRows * 18 + 13;

        this.itemBasedConfigurationGroup = Flow.column()
            .size(88, itemGroupHeight)
            .leftRel(0.5f)
            .top(24)
            .child(slotGroup)
            .setEnabledIf(flow -> filterableWrapper.getMatchType() != IAdvancedFilterable.MatchType.ORE_DICT);

        // OreDict widgets
        this.oreDictList = new OreDictRegexListWidget(filterableWrapper, 88, 49);

        // OreDict Slot - reject items without any ore dict
        ItemSlot oreDictSlot = ItemSlot.create(true);
        oreDictSlot.name("ore_dict_" + slotIndex)
            .syncHandler("ore_dict_" + slotIndex, 0)
            .size(18, 18);

        // + button with scroll and tooltip
        ScrollableButton addOreDictEntryButton = new ScrollableButton() {

            @Override
            public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
                List<String> available = getAvailableOreDicts(oreDictSlot);
                if (available.isEmpty()) return true;
                if (scrollDirection == UpOrDown.UP) {
                    addScrollIndex = (addScrollIndex - 1 + available.size()) % available.size();
                } else {
                    addScrollIndex = (addScrollIndex + 1) % available.size();
                }
                markTooltipDirty();
                return true;
            }
        };
        addOreDictEntryButton.size(18, 18);
        addOreDictEntryButton.overlay(OKBGuiTextures.ADD_ICON);
        addOreDictEntryButton.tooltipAutoUpdate(true);
        addOreDictEntryButton.tooltipDynamic(tooltip -> {
            tooltip.addLine(IKey.lang("gui.backpack.add_ore_dict"));
            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);

            ItemStack stack = oreDictSlot.isSynced() ? oreDictSlot.getSlot()
                .getStack() : null;
            if (stack == null) {
                tooltip.addLine(
                    IKey.lang("gui.backpack.add_ore_dict.no_item")
                        .style(IKey.GRAY, IKey.ITALIC));
                return;
            }

            List<String> available = getAvailableOreDicts(oreDictSlot);
            if (available.isEmpty()) {
                tooltip.addLine(
                    IKey.lang("gui.backpack.add_ore_dict.no_additional")
                        .style(IKey.YELLOW, IKey.ITALIC));
                return;
            }

            if (addScrollIndex >= available.size()) addScrollIndex = 0;
            for (int i = 0; i < available.size(); i++) {
                String entry = available.get(i);
                if (i == addScrollIndex) {
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
                IKey.lang("gui.backpack.add_ore_dict.controls")
                    .style(IKey.GRAY, IKey.ITALIC));
        });
        addOreDictEntryButton.onMousePressed(mouseButton -> {
            List<String> available = getAvailableOreDicts(oreDictSlot);
            if (available.isEmpty()) return false;

            if (addScrollIndex >= available.size()) addScrollIndex = 0;
            String selected = available.get(addScrollIndex);

            List<String> list = new ArrayList<>(filterableWrapper.getOreDictEntries());
            if (!list.contains(selected)) {
                list.add(selected);
                oreDictList.child(new OreDictEntryWidget(this, selected, 88));
                filterableWrapper.setOreDictEntries(list);
                updateWrapper();
                oreDictList.scheduleResize();
            }

            addOreDictEntryButton.markTooltipDirty();
            return true;
        });

        // - button with scroll and tooltip
        ScrollableButton removeOreDictEntryButton = new ScrollableButton() {

            @Override
            public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
                List<String> entries = filterableWrapper.getOreDictEntries();
                if (entries.isEmpty()) return true;
                if (removeScrollIndex < 0) {
                    removeScrollIndex = scrollDirection == UpOrDown.DOWN ? 0 : entries.size() - 1;
                } else if (scrollDirection == UpOrDown.UP) {
                    removeScrollIndex = (removeScrollIndex - 1 + entries.size()) % entries.size();
                } else {
                    removeScrollIndex = (removeScrollIndex + 1) % entries.size();
                }
                markTooltipDirty();
                return true;
            }
        };
        removeOreDictEntryButton.size(18, 18);
        removeOreDictEntryButton.overlay(OKBGuiTextures.REMOVE_ICON);
        removeOreDictEntryButton.tooltipAutoUpdate(true);
        removeOreDictEntryButton.tooltipDynamic(tooltip -> {
            tooltip.addLine(IKey.lang("gui.backpack.remove_ore_dict"));
            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);

            List<String> entries = filterableWrapper.getOreDictEntries();
            if (entries.isEmpty()) {
                tooltip.addLine(
                    IKey.lang("gui.backpack.remove_ore_dict.empty")
                        .style(EnumChatFormatting.RED));
                return;
            }

            for (int i = 0; i < entries.size(); i++) {
                String entry = entries.get(i);
                if (i == removeScrollIndex) {
                    tooltip.addLine(
                        IKey.str("-> " + entry)
                            .style(EnumChatFormatting.RED));
                } else {
                    tooltip.addLine(
                        IKey.str("> " + entry)
                            .style(EnumChatFormatting.GRAY));
                }
            }
            tooltip.addLine(
                IKey.lang("gui.backpack.remove_ore_dict.controls")
                    .style(IKey.GRAY, IKey.ITALIC));
        });
        removeOreDictEntryButton.onMousePressed(mouseButton -> {
            List<String> entries = filterableWrapper.getOreDictEntries();
            if (entries.isEmpty() || removeScrollIndex < 0) return false;

            if (removeScrollIndex >= entries.size()) removeScrollIndex = 0;
            String selected = entries.get(removeScrollIndex);

            List<String> list = new ArrayList<>(entries);
            list.remove(selected);
            filterableWrapper.setOreDictEntries(list);

            // Remove matching child widget from list
            for (IWidget child : new ArrayList<>(oreDictList.getChildren())) {
                if (child instanceof OreDictEntryWidget entryWidget && entryWidget.getText()
                    .equals(selected)) {
                    oreDictList.removeChild(entryWidget);
                    break;
                }
            }

            updateWrapper();
            oreDictList.scheduleResize();

            if (list.isEmpty()) {
                removeScrollIndex = -1;
            } else if (removeScrollIndex >= list.size()) {
                removeScrollIndex = list.size() - 1;
            }

            removeOreDictEntryButton.markTooltipDirty();
            return true;
        });

        for (String entry : filterableWrapper.getOreDictEntries()) {
            this.oreDictList.child(new OreDictEntryWidget(this, entry, 88));
        }

        // OreDict button row: slot + add + remove + matchAll, size 18, padding 0
        Flow oreDictBasedConfigButtonRow = Flow.row()
            .bottom(0)
            .left(0)
            .height(18)
            .coverChildrenWidth()
            .childPadding(0)
            .child(oreDictSlot)
            .child(addOreDictEntryButton)
            .child(removeOreDictEntryButton)
            .child(matchAllOreDictButton)
            .setEnabledIf(flow -> filterableWrapper.getMatchType() == IAdvancedFilterable.MatchType.ORE_DICT);

        this.oreDictBasedConfigurationGroup = Flow.column()
            .size(88, 69)
            .top(24)
            .left(0)
            .child(oreDictList)
            .child(oreDictBasedConfigButtonRow)
            .setEnabledIf(flow -> filterableWrapper.getMatchType() == IAdvancedFilterable.MatchType.ORE_DICT);

        // Add all children
        child(buttonRow).child(this.itemBasedConfigurationGroup)
            .child(this.oreDictBasedConfigurationGroup);
    }

    private List<String> getAvailableOreDicts(ItemSlot oreDictSlot) {
        ItemStack stack = oreDictSlot.isSynced() ? oreDictSlot.getSlot()
            .getStack() : null;
        if (stack == null) return List.of();

        int[] ids = OreDictionary.getOreIDs(stack);
        if (ids.length == 0) return List.of();

        List<String> existing = filterableWrapper.getOreDictEntries();
        List<String> available = new ArrayList<>();
        for (int id : ids) {
            String oreName = OreDictionary.getOreName(id);
            if (!existing.contains(oreName)) {
                available.add(oreName);
            }
        }
        return available;
    }

    public void replaceFilterTypeButton(CyclicVariantButtonWidget newButton) {
        buttonRow.getChildren()
            .remove(this.filterTypeButton);
        this.filterTypeButton = newButton;
        buttonRow.getChildren()
            .add(0, newButton);
    }

    public void setSlotsDisabled(BooleanSupplier disabled) {
        this.slotsDisabled = disabled;
    }

    private void updateWrapper() {
        if (slotSyncHandler != null) {
            slotSyncHandler
                .syncToServer(UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_ADVANCED_FILTERABLE), writer -> {
                    NetworkUtils.writeEnumValue(writer, filterableWrapper.getFilterType());
                    NetworkUtils.writeEnumValue(writer, filterableWrapper.getMatchType());
                    writer.writeBoolean(filterableWrapper.isIgnoreDurability());
                    writer.writeBoolean(filterableWrapper.isIgnoreNBT());
                    writer.writeBoolean(filterableWrapper.isMatchAllOreDicts());

                    List<String> oreList = filterableWrapper.getOreDictEntries();
                    writer.writeInt(oreList.size());
                    for (String entry : oreList) {
                        writer.writeStringToBuffer(entry);
                    }
                });
        }
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        if (syncOrValue instanceof UpgradeSlotSH) {
            slotSyncHandler = (UpgradeSlotSH) syncOrValue;
        }
        return slotSyncHandler != null;
    }

    public static class OreDictRegexListWidget extends ListWidget<OreDictEntryWidget, OreDictRegexListWidget> {

        private static final UITexture BACKGROUND_TILE_TEXTURE = UITexture.builder()
            .location(Reference.MOD_ID, "gui/gui_controls")
            .imageSize(256, 256)
            .xy(29, 146, 66, 56)
            .adaptable(1)
            .tiled()
            .build();

        private final IAdvancedFilterable filterableWrapper;

        public OreDictRegexListWidget(IAdvancedFilterable filterableWrapper, int width, int height) {
            this.filterableWrapper = filterableWrapper;
            background(BACKGROUND_TILE_TEXTURE);
            size(width, height);
            left(0);

            tooltipAutoUpdate(true);
            tooltipDynamic(tooltip -> {
                tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
                List<String> entries = filterableWrapper.getOreDictEntries();
                if (entries.isEmpty()) return;
                tooltip.addLine(IKey.lang("gui.backpack.ore_dict_list.title"));
                for (String entry : entries) {
                    tooltip.addLine(
                        IKey.str("> " + entry)
                            .style(EnumChatFormatting.GRAY));
                }
            });
        }

        public void removeChild(OreDictEntryWidget widget) {
            remove(widget);
        }
    }

    public static class OreDictEntryWidget extends TextWidget<OreDictEntryWidget> implements Interactable {

        private static final int PAUSE_TIME = 60;

        private final AdvancedFilterWidget parent;
        @Getter
        private final String text;

        private TextRenderer.Line line = new TextRenderer.Line("", 0f);
        private long time = 0;
        private int scroll = 0;
        private boolean hovering = false;
        private int pauseTimer = 0;

        private boolean isSelected() {
            return parent.removeScrollIndex == parent.filterableWrapper.getOreDictEntries()
                .indexOf(text);
        }

        public OreDictEntryWidget(AdvancedFilterWidget parent, String text, int width) {
            super(IKey.str(" " + text));
            this.parent = parent;
            this.text = text;

            size(width, 12);
            overlay(new Outline(Color.WHITE.main));
            color(Color.GREY.main);
            shadow(true);

            tooltipBuilder(richTooltip -> {
                tooltip().pos(RichTooltip.Pos.NEXT_TO_MOUSE);

                if (line.getWidth() > getArea().width) {
                    tooltip().addLine(getKey());
                }
                ItemStack stack = getPanel().getContext()
                    .getMC().thePlayer.inventory.getItemStack();
                if (stack != null) {
                    boolean testMatched = Arrays.stream(OreDictionary.getOreIDs(stack))
                        .mapToObj(OreDictionary::getOreName)
                        .anyMatch(
                            name -> Pattern.compile(text)
                                .matcher(name)
                                .matches());

                    if (testMatched) {
                        tooltip().addLine(OKBGuiTextures.CHECK_ICON);
                    }
                }
            });
        }

        @Override
        public void onMouseStartHover() {
            super.onMouseStartHover();
            hovering = true;
            markTooltipDirty();
            if (!isSelected()) {
                overlay(new Outline(Color.GREY.main));
            }
        }

        @Override
        public void onMouseEndHover() {
            super.onMouseEndHover();
            hovering = false;
            scroll = 0;
            time = 0;
            markTooltipDirty();
            overlay(new Outline(Color.WHITE.main));
        }

        @Override
        public void onUpdate() {
            super.onUpdate();
            if (pauseTimer > 0) {
                if (++pauseTimer == PAUSE_TIME) {
                    pauseTimer = (scroll == 0 ? 0 : 1);
                    scroll = 0;
                }
                return;
            }

            if (hovering && ++time % 2 == 0 && ++scroll == line.upperWidth() - getArea().width - 1) {
                pauseTimer = 1;
            }
        }

        @Override
        public @NotNull Result onMousePressed(int mouseButton) {
            int myIndex = parent.filterableWrapper.getOreDictEntries()
                .indexOf(text);
            if (parent.removeScrollIndex == myIndex) {
                parent.removeScrollIndex = -1;
            } else {
                parent.removeScrollIndex = myIndex;
            }
            return Result.SUCCESS;
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry widgetTheme) {
            checkString();
            TextRenderer renderer = TextRenderer.SHARED;
            renderer.setColor(getColor().getAsInt());
            renderer.setAlignment(getAlignment(), getArea().w() + 1f, getArea().h());
            renderer.setShadow(isShadow());
            renderer.setPos(
                getArea().getPadding()
                    .getLeft(),
                getArea().getPadding()
                    .getTop() + 2);
            renderer.setScale(getScale());
            renderer.setSimulate(false);
            renderer.drawCut(line);
        }

        @Override
        public void drawOverlay(ModularGuiContext context, WidgetThemeEntry widgetTheme) {
            IDrawable overlay = getHoverOverlay();
            if (overlay == null) return;
            if (!isSelected() && !hovering) {
                return;
            }
            overlay.drawAtZero(context, getArea().width + 2, getArea().height + 2, widgetTheme.getTheme());
        }

        @Override
        protected String checkString() {
            String s = getKey().get();
            if (!s.equals(line.getText())) {
                TextRenderer.SHARED.setScale(getScale());
                line = TextRenderer.SHARED.line(s);
                scroll = 0;
                markTooltipDirty();
            }
            return s;
        }
    }

    public static abstract class ScrollableButton extends ButtonWidget<ScrollableButton> {

        public abstract boolean onMouseScroll(UpOrDown scrollDirection, int amount);
    }
}
