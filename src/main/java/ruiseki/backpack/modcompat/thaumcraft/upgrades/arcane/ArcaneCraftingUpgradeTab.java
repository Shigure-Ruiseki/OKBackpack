package ruiseki.backpack.modcompat.thaumcraft.upgrades.arcane;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.backpack.modcompat.thaumcraft.helper.ThaumcraftHelpers;
import ruiseki.okbackpack.client.gui.slot.ResizableItemSlot;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.client.gui.widget.ShiftButtonWidget;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSHRegistries;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedBooleanSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedStringSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.StorageSlotGrid;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class ArcaneCraftingUpgradeTab extends ExpandedUpgradeTab<ArcaneCraftingUpgradeWrapper> {

    protected static final List<CyclicVariantButtonWidget.Variant> SHIFT_CLICK_INTO_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.shift_click_into_inventory"),
            OSCTextures.SHIFT_CLICK_INTO_INVENTORY),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.shift_click_into_storage"),
            OSCTextures.SHIFT_CLICK_INTO_STORAGE));

    protected static final List<CyclicVariantButtonWidget.Variant> FILL_CRAFTING_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.refill_crafting_grid"),
            OSCTextures.FILL_CRAFTING),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.do_not_refill_crafting_grid"),
            OSCTextures.DO_NOT_FILL_CRAFTING));

    private static final String[] PRIMAL_TAGS = { "aer", "terra", "ignis", "aqua", "ordo", "perditio" };

    private final DelegatedBooleanSH hasWandSH;
    private final DelegatedStringSH missingResearchSH;
    private final DelegatedStringSH missingResearchNameSH;

    public ArcaneCraftingUpgradeTab(int slotIndex, ArcaneCraftingUpgradeWrapper wrapper, ItemStack stack,
        StoragePanel storagePanel, String titleKey) {
        super(slotIndex, wrapper, stack, storagePanel, titleKey);

        this.hasWandSH = storagePanel.getSyncManager()
            .findSyncHandler("arcane_has_wand_" + slotIndex, 0, DelegatedBooleanSH.class);
        this.missingResearchSH = storagePanel.getSyncManager()
            .findSyncHandler("arcane_missing_research_" + slotIndex, 0, DelegatedStringSH.class);
        this.missingResearchNameSH = storagePanel.getSyncManager()
            .findSyncHandler("arcane_missing_research_name_" + slotIndex, 0, DelegatedStringSH.class);
    }

    public ArcaneCraftingUpgradeTab(int slotIndex, ArcaneCraftingUpgradeWrapper wrapper, ItemStack stack,
        StoragePanel storagePanel) {
        this(slotIndex, wrapper, stack, storagePanel, "gui.okbackpack.upgrades.arcane_crafting");
    }

    public boolean hasWand() {
        return hasWandSH.getValue();
    }

    public String getMissingResearchName() {
        return missingResearchNameSH.getValue();
    }

    public String getMissingResearch() {
        return missingResearchSH.getValue();
    }

    @Override
    protected Flow addUpgradeColumn() {
        return super.addUpgradeColumn().child(this.addButtonRow())
            .child(this.addCraftingRow());
    }

    protected Flow addButtonRow() {
        return Flow.row()
            .coverChildrenHeight()
            .childPadding(2)
            .child(this.addShiftClickIntoButton())
            .child(this.addFillCraftingButton());
    }

    protected CyclicVariantButtonWidget addFillCraftingButton() {
        return new CyclicVariantButtonWidget(
            FILL_CRAFTING_VARIANTS,
            this.getWrapper()
                .shouldRefillCraftingGrid() ? 0 : 1,
            index -> {
                this.getWrapper()
                    .setRefillCraftingGrid(index == 0);
                this.getSyncHandler()
                    .syncToServer(UpgradeSlotSHRegistries.FILL_CRAFTING, buffer -> buffer.writeBoolean(index == 0));
            }).size(20, 20);
    }

    protected CyclicVariantButtonWidget addShiftClickIntoButton() {
        return new CyclicVariantButtonWidget(
            SHIFT_CLICK_INTO_VARIANTS,
            this.getWrapper()
                .shouldShiftClickIntoStorage() ? 0 : 1,
            index -> {
                this.getWrapper()
                    .setShiftClickIntoStorage(index == 0);
                this.getSyncHandler()
                    .syncToServer(
                        UpgradeSlotSHRegistries.SHIFT_INTO_STORAGE,
                        buffer -> buffer.writeBoolean(index == 0));
            }).size(20, 20);
    }

    protected Flow addCraftingRow() {
        return Flow.row()
            .coverChildren()
            .childPadding(2)
            .child(this.addCraftingGrid())
            .child(this.addAspectGrid())
            .child(this.addCraftingTweak());
    }

    protected StorageSlotGrid<ItemSlot> addCraftingGrid() {
        int slotIndex = this.getSlotIndex();
        StorageSlotGrid<ItemSlot> craftingGrid = new StorageSlotGrid<ItemSlot>().name("arcane_matrix")
            .coverChildren();
        for (int i = 0; i < 9; i++) {
            craftingGrid.child(
                new ItemSlot().syncHandler("arcane_slot_" + slotIndex, i)
                    .pos(i % 3 * 18, i / 3 * 18)
                    .name("arcane_slot_" + i));
        }
        craftingGrid.child(new ResizableItemSlot(26) {

            @Override
            public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
                super.draw(context, widgetTheme);
                if (!canCraft()) {
                    String missingResearch = wrapper.getMissingResearch();
                    if (missingResearch == null || missingResearch.isEmpty()) {
                        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                        GL11.glDisable(GL11.GL_TEXTURE_2D);
                        GL11.glEnable(GL11.GL_BLEND);
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                        GL11.glDisable(GL11.GL_LIGHTING);
                        Gui.drawRect(0, 0, 26, 26, 0x80000000);
                        GL11.glPopAttrib();
                    }
                }
            }

            @Override
            public boolean canHover() {
                return canCraft();
            }

        }.syncHandler("arcane_result_" + slotIndex, 0)
            .pos(4, 63)
            .name("arcane_result_" + slotIndex));
        craftingGrid.child(
            new ResizableItemSlot().syncHandler("arcane_wand_" + slotIndex, 0)
                .pos(32, 67)
                .name("arcane_wand_" + slotIndex));
        return craftingGrid;
    }

    protected StorageSlotGrid<?> addAspectGrid() {
        return StorageSlotGrid.<SingleAspectWidget>builder()
            .matrix(2, 3, 'A')
            .key('A', index -> new SingleAspectWidget(this, PRIMAL_TAGS[index]))
            .build()
            .topRel(0);
    }

    protected Flow addCraftingTweak() {
        return Flow.column()
            .coverChildren()
            .childPadding(2)
            .topRel(0)
            .child(this.addRotatedButton())
            .child(this.addGridButton())
            .child(this.addClearButton());
    }

    protected ButtonWidget<?> addRotatedButton() {
        return new ShiftButtonWidget().size(16)
            .setNotShift(OSCTextures.ROTATED_RIGHT)
            .setShift(OSCTextures.ROTATED_LEFT)
            .onMousePressed(button -> {
                if (button != 0) return false;
                Interactable.playButtonClickSound();
                boolean clockwise = !Interactable.hasShiftDown();
                this.getSyncHandler()
                    .syncToServer(UpgradeSlotSHRegistries.CRAFTING_ROTATED, buf -> buf.writeBoolean(clockwise));
                return true;
            });
    }

    protected ButtonWidget<?> addGridButton() {
        return new ShiftButtonWidget().size(16)
            .setNotShift(OSCTextures.BALANCE)
            .setShift(OSCTextures.SPREAD)
            .onMousePressed(button -> {
                if (button != 0) return false;
                Interactable.playButtonClickSound();
                boolean balance = !Interactable.hasShiftDown();
                this.getSyncHandler()
                    .syncToServer(
                        balance ? UpgradeSlotSHRegistries.CRAFTING_BALANCE : UpgradeSlotSHRegistries.CRAFTING_SPREAD);
                return true;
            });
    }

    protected ButtonWidget<?> addClearButton() {
        return new ButtonWidget<>().size(16)
            .overlay(OSCTextures.CLEAR)
            .onMousePressed(button -> {
                if (button != 0) return false;
                Interactable.playButtonClickSound();
                this.getSyncHandler()
                    .syncToServer(
                        UpgradeSlotSHRegistries.CRAFTING_CLEAR,
                        buf -> buf.writeBoolean(
                            this.getWrapper()
                                .shouldShiftClickIntoStorage()));
                return true;
            });
    }

    private boolean hasInsufficientVis() {
        Map<String, Integer> required = this.wrapper.getRequiredAspects();
        if (required == null || required.isEmpty()) return false;

        ItemStack wandStack = this.wrapper.getStorage()
            .getStackInSlot(9);
        if (wandStack == null) return true;

        for (Map.Entry<String, Integer> entry : required.entrySet()) {
            if (entry.getValue() <= 0) continue;
            int current = ThaumcraftHelpers.getWandVis(wandStack, entry.getKey());
            if (current < entry.getValue() * 100) return true;
        }
        return false;
    }

    private boolean canCraft() {
        String missingResearch = this.wrapper.getMissingResearch();
        if (missingResearch != null && !missingResearch.isEmpty()) return false;
        Map<String, Integer> required = this.wrapper.getRequiredAspects();
        if (required == null || required.isEmpty()) return true;
        if (!this.hasWandSH.getValue()) return false;
        return !this.hasInsufficientVis();
    }

    private static class SingleAspectWidget extends Widget<SingleAspectWidget> {

        private final ArcaneCraftingUpgradeTab tab;
        private final String aspectTag;

        public SingleAspectWidget(ArcaneCraftingUpgradeTab tab, String aspectTag) {
            this.tab = tab;
            this.aspectTag = aspectTag;
            this.size(18);
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            Map<String, Integer> requiredAspects = this.tab.getWrapper()
                .getRequiredAspects();
            String missingResearch = this.tab.getMissingResearch();
            boolean hasMissingResearch = missingResearch != null && !missingResearch.isEmpty();
            int amount = (this.tab.hasWand() && !hasMissingResearch) ? requiredAspects.getOrDefault(this.aspectTag, 0)
                : 0;

            int color = ThaumcraftHelpers.getAspectColor(this.aspectTag);
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;

            Minecraft mc = Minecraft.getMinecraft();

            float alpha;
            if (!this.tab.hasWand() || hasMissingResearch) {
                alpha = 0.3f;
            } else if (amount > 0 && isInsufficient()) {
                long time = System.currentTimeMillis();
                alpha = (float) (0.4 + 0.6 * Math.abs(Math.sin(time / 200.0)));
            } else {
                alpha = amount > 0 ? 1.0f : 0.3f;
            }

            ResourceLocation aspectTexture = new ResourceLocation(
                "thaumcraft",
                "textures/aspects/" + this.aspectTag + ".png");
            mc.getTextureManager()
                .bindTexture(aspectTexture);

            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(r, g, b, alpha);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            Gui.func_146110_a(1, 1, 0, 0, 16, 16, 16, 16);

            if (amount > 0 && this.tab.hasWand()) {
                String text = String.valueOf(amount);
                mc.fontRenderer.drawString(
                    text,
                    18 - mc.fontRenderer.getStringWidth(text) - 1,
                    18 - mc.fontRenderer.FONT_HEIGHT,
                    0xFFFFFF);
            }
            GL11.glPopAttrib();
        }

        private boolean isInsufficient() {
            Map<String, Integer> required = this.tab.getWrapper()
                .getRequiredAspects();
            int amount = required.getOrDefault(this.aspectTag, 0);
            if (amount <= 0) return false;

            ItemStack wandStack = this.tab.getWrapper()
                .getStorage()
                .getStackInSlot(9);
            if (wandStack == null) return true;

            int current = ThaumcraftHelpers.getWandVis(wandStack, this.aspectTag);
            return current < amount * 100;
        }
    }
}
