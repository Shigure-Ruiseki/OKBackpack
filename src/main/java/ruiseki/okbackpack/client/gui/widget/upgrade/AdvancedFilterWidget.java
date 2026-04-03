package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import org.jetbrains.annotations.NotNull;

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
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;

import lombok.Getter;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.wrapper.IAdvancedFilterable;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.drawble.Outline;
import ruiseki.okbackpack.client.gui.slot.FilterSlot;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
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

    @Getter
    private final CyclicVariantButtonWidget filterTypeButton;
    @Getter
    private final CyclicVariantButtonWidget matchTypeButton;
    @Getter
    private final CyclicVariantButtonWidget ignoreDurabilityButton;
    @Getter
    private final CyclicVariantButtonWidget ignoreNBTButton;

    @Getter
    private final Column itemBasedConfigurationGroup;
    @Getter
    private final Column oreDictBasedConfigurationGroup;
    @Getter
    private final List<FilterSlot> filterSlots;

    private final OreDictRegexListWidget oreDictList;
    private OreDictEntryWidget focusedOreDictEntry = null;

    @Getter
    private UpgradeSlotSH slotSyncHandler = null;

    private final IAdvancedFilterable filterableWrapper;

    public AdvancedFilterWidget(int slotIndex, IAdvancedFilterable filterableWrapper, String syncKey) {
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

        // Add buttons to rows
        Row buttonRow = (Row) new Row().leftRel(0.5f)
            .size(88, 20)
            .childPadding(2);

        Row itemBasedConfigButtonRow = (Row) new Row().childPadding(2)
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
        for (int i = 0; i < 16; i++) {
            FilterSlot slot = new FilterSlot();
            slot.name(syncKey + "_" + slotIndex)
                .syncHandler(syncKey + "_" + slotIndex, i)
                .pos(i % 4 * 18, i / 4 * 18);

            this.filterSlots.add(slot);
            slotGroup.child(slot);
        }

        this.itemBasedConfigurationGroup = (Column) new Column().size(88, 85)
            .leftRel(0.5f)
            .top(24)
            .child(slotGroup)
            .setEnabledIf(flow -> filterableWrapper.getMatchType() != IAdvancedFilterable.MatchType.ORE_DICT);

        // OreDict widgets
        this.oreDictList = new OreDictRegexListWidget(88, 63);

        // OreDict Slot
        FilterSlot oreDictSlot = new FilterSlot();
        oreDictSlot.name(syncKey + "_" + slotIndex)
            .syncHandler(syncKey + "_" + slotIndex, 0);

        // OreDict buttons
        ButtonWidget<?> addOreDictEntryButton = new ButtonWidget<>();
        addOreDictEntryButton.size(20, 20);
        addOreDictEntryButton.overlay(OKBGuiTextures.ADD_ICON);
        addOreDictEntryButton.onMousePressed(mouseButton -> {
            ItemStack stack = oreDictSlot.getSlot()
                .getStack();
            if (stack == null) return false;

            int[] ids = OreDictionary.getOreIDs(stack);
            if (ids.length == 0) return false;

            List<String> list = new ArrayList<>(filterableWrapper.getOreDictEntries());

            boolean changed = false;

            for (int id : ids) {
                String oreName = OreDictionary.getOreName(id);

                if (!list.contains(oreName)) {
                    list.add(oreName);
                    oreDictList.child(new OreDictEntryWidget(this, oreName, 77));
                    changed = true;
                }
            }

            if (changed) {
                filterableWrapper.setOreDictEntries(list);
                updateWrapper();
                oreDictList.scheduleResize();
            }

            return true;
        });

        ButtonWidget<?> removeOreDictEntryButton = new ButtonWidget<>();
        removeOreDictEntryButton.size(20, 20);
        removeOreDictEntryButton.overlay(OKBGuiTextures.REMOVE_ICON);
        removeOreDictEntryButton.onMousePressed(mouseButton -> {
            if (this.focusedOreDictEntry == null) return false;

            List<String> list = new ArrayList<>(filterableWrapper.getOreDictEntries());
            list.remove(focusedOreDictEntry.getText());

            filterableWrapper.setOreDictEntries(list);

            this.oreDictList.removeChild(focusedOreDictEntry);
            updateWrapper();
            this.oreDictList.scheduleResize();

            return true;
        });
        for (String entry : filterableWrapper.getOreDictEntries()) {
            this.oreDictList.child(new OreDictEntryWidget(this, entry, 88));
        }

        Row oreDictBasedConfigButtonRow = (Row) new Row().bottom(0)
            .leftRel(0.5f)
            .height(20)
            .coverChildrenWidth()
            .childPadding(2)
            .child(oreDictSlot)
            .child(addOreDictEntryButton)
            .child(removeOreDictEntryButton)
            .setEnabledIf(flow -> filterableWrapper.getMatchType() == IAdvancedFilterable.MatchType.ORE_DICT);

        this.oreDictBasedConfigurationGroup = (Column) new Column().size(88, 85)
            .top(24)
            .child(oreDictList)
            .child(oreDictBasedConfigButtonRow)
            .setEnabledIf(flow -> filterableWrapper.getMatchType() == IAdvancedFilterable.MatchType.ORE_DICT);

        // Add all children
        child(buttonRow).child(this.itemBasedConfigurationGroup)
            .child(this.oreDictBasedConfigurationGroup);
    }

    private void updateWrapper() {
        if (slotSyncHandler != null) {
            slotSyncHandler.syncToServer(UpgradeSlotSH.UPDATE_ADVANCED_FILTERABLE, writer -> {
                NetworkUtils.writeEnumValue(writer, filterableWrapper.getFilterType());
                NetworkUtils.writeEnumValue(writer, filterableWrapper.getMatchType());
                writer.writeBoolean(filterableWrapper.isIgnoreDurability());
                writer.writeBoolean(filterableWrapper.isIgnoreNBT());

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

        public OreDictRegexListWidget(int width, int height) {
            background(BACKGROUND_TILE_TEXTURE);
            size(width, height);
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
        private boolean selected = false;

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
            if (!selected) {
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
            for (IWidget child : parent.oreDictList.getChildren()) {
                if (child != this && child instanceof OreDictEntryWidget widget) {
                    widget.selected = false;
                }
            }

            if (selected) {
                selected = false;
                parent.focusedOreDictEntry = null;
            } else {
                selected = true;
                parent.focusedOreDictEntry = this;
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
            IDrawable overlay = getCurrentOverlay(context.getTheme(), widgetTheme);
            if (!selected && !hovering && overlay != null) {
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
}
