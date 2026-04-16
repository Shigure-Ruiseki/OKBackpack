package ruiseki.okbackpack.client.gui.widget.upgrade;

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
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.wrapper.IArcaneCraftingUpgrade;
import ruiseki.okbackpack.api.wrapper.ICraftingUpgrade;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.slot.BigItemSlot;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.client.gui.widget.ShiftButtonWidget;
import ruiseki.okbackpack.common.helpers.BackpackInventoryHelpers;
import ruiseki.okbackpack.common.item.arcane.ArcaneCraftingUpgradeWrapper;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okbackpack.compat.thaumcraft.ThaumcraftHelpers;

public class ArcaneCraftingUpgradeWidget extends ExpandedUpgradeTabWidget<ArcaneCraftingUpgradeWrapper> {

    private static final List<CyclicVariantButtonWidget.Variant> INTO_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.into_backpack"), OKBGuiTextures.INTO_BACKPACK),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.into_inventory"), OKBGuiTextures.INTO_INVENTORY));

    private static final List<CyclicVariantButtonWidget.Variant> USED_BACKPACK_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.used_backpack"), OKBGuiTextures.USED_BACKPACK),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.unused_backpack"),
            OKBGuiTextures.UNUSED_BACKPACK));

    private static final String[] PRIMAL_TAGS = { "aer", "terra", "ignis", "aqua", "ordo", "perditio" };

    private final ArcaneCraftingUpgradeWrapper wrapper;
    private ItemSlot[] craftingMatrix;
    private ItemSlot craftingResult;
    private ItemSlot wandSlot;

    public ArcaneCraftingUpgradeWidget(int slotIndex, ArcaneCraftingUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        super(slotIndex, 5, stack, panel, titleKey, 140);
        this.wrapper = wrapper;

        this.syncHandler("upgrades", slotIndex);

        CyclicVariantButtonWidget craftingDesButton = new CyclicVariantButtonWidget(
            INTO_VARIANTS,
            wrapper.getCraftingDes()
                .ordinal(),
            index -> {
                wrapper.setCraftingDes(ICraftingUpgrade.CraftingDestination.values()[index]);
                updateWrapper();
            }).size(20, 20);

        CyclicVariantButtonWidget usedBackpackButton = new CyclicVariantButtonWidget(
            USED_BACKPACK_VARIANTS,
            wrapper.isUseBackpack() ? 0 : 1,
            index -> {
                wrapper.setUseBackpack(index == 0);
                updateWrapper();
            }).size(20, 20);

        Flow buttonRow = Flow.row()
            .height(20)
            .child(craftingDesButton)
            .child(usedBackpackButton);

        ShiftButtonWidget rotated = new ShiftButtonWidget(OKBGuiTextures.ROTATED_RIGHT, OKBGuiTextures.ROTATED_LEFT)
            .size(16)
            .onMousePressed(button -> {
                if (button == 0) {
                    Interactable.playButtonClickSound();
                    boolean clockwise = !Interactable.hasShiftDown();
                    BackpackInventoryHelpers.rotated(wrapper.getStorage(), clockwise);
                    getSlotSyncHandler().syncToServer(
                        UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_ARCANE_CRAFTING_R),
                        buf -> buf.writeBoolean(clockwise));
                    return true;
                }
                return false;
            });

        ShiftButtonWidget grid = new ShiftButtonWidget(OKBGuiTextures.BALANCE, OKBGuiTextures.SPREAD).size(16)
            .onMousePressed(button -> {
                if (button == 0) {
                    Interactable.playButtonClickSound();
                    boolean balance = !Interactable.hasShiftDown();
                    if (balance) {
                        BackpackInventoryHelpers.balance(wrapper.getStorage());
                    } else {
                        BackpackInventoryHelpers.spread(wrapper.getStorage());
                    }
                    getSlotSyncHandler().syncToServer(
                        UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_ARCANE_CRAFTING_G),
                        buf -> buf.writeBoolean(balance));
                    return true;
                }
                return false;
            });

        ButtonWidget<?> clear = new ButtonWidget<>().overlay(OKBGuiTextures.CLEAR)
            .size(16)
            .onMousePressed(button -> {
                if (button == 0) {
                    Interactable.playButtonClickSound();
                    BackpackInventoryHelpers.clear(
                        panel,
                        wrapper.getStorage(),
                        wrapper.getCraftingDes()
                            .ordinal());
                    getSlotSyncHandler().syncToServer(
                        UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_ARCANE_CRAFTING_C),
                        buf -> buf.writeInt(
                            wrapper.getCraftingDes()
                                .ordinal()));
                    return true;
                }
                return false;
            });

        SlotGroupWidget craftingGroupsWidget = new SlotGroupWidget().name("arcane_matrix")
            .coverChildren();

        craftingMatrix = new ItemSlot[9];
        for (int i = 0; i < 9; i++) {
            ItemSlot itemSlot = new ItemSlot().syncHandler("arcane_slot_" + slotIndex, i)
                .pos(i % 3 * 18, i / 3 * 18)
                .name("arcane_slot_" + i);
            craftingGroupsWidget.child(itemSlot);
            craftingMatrix[i] = itemSlot;
        }

        craftingResult = new BigItemSlot() {

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
                        Gui.drawRect(-3, -3, 21, 21, 0x80000000);
                        GL11.glPopAttrib();
                    }
                }
            }

            @Override
            public boolean canHover() {
                return canCraft();
            }
        };
        craftingResult.syncHandler("arcane_result_" + slotIndex, 0)
            .pos(18, 18 * 3 + 9)
            .name("arcane_result_" + slotIndex);
        craftingGroupsWidget.child(craftingResult);

        wandSlot = new ItemSlot().syncHandler("arcane_wand_" + slotIndex, 0)
            .pos(68, 18 * 3 + 9)
            .name("arcane_wand_" + slotIndex);
        craftingGroupsWidget.child(wandSlot);

        for (int i = 0; i < PRIMAL_TAGS.length; i++) {
            int col = i % 2;
            int row = i / 2;
            SingleAspectWidget aspectWidget = new SingleAspectWidget(wrapper, PRIMAL_TAGS[i]);
            aspectWidget.pos(18 * 3 + 4 + col * 18, row * 18)
                .size(18, 18);
            craftingGroupsWidget.child(aspectWidget);
        }

        @SuppressWarnings({ "rawtypes" })
        Widget insufficientVisText = new Widget() {

            @Override
            public void draw(ModularGuiContext context, WidgetThemeEntry widgetTheme) {
                if (!Mods.Thaumcraft.isLoaded()) return;
                if (!wrapper.hasWand()) return;
                if (!hasInsufficientVis()) return;
                String missingResearch = wrapper.getMissingResearch();
                if (missingResearch != null && !missingResearch.isEmpty()) return;

                Minecraft mc = Minecraft.getMinecraft();
                String text = IKey.lang("gui.backpack.insufficient_vis")
                    .get();
                int textWidth = mc.fontRenderer.getStringWidth(text);
                GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                RenderHelper.disableStandardItemLighting();
                GL11.glDisable(GL11.GL_LIGHTING);
                mc.fontRenderer.drawString(text, (60 - textWidth) / 2, 0, 0xFF4444);
                GL11.glPopAttrib();
            }
        };
        insufficientVisText.pos(47, 18 * 3 + 32)
            .size(60, 10);
        craftingGroupsWidget.child(insufficientVisText);

        @SuppressWarnings({ "rawtypes" })
        Widget missingResearchText = new Widget() {

            @Override
            public void draw(ModularGuiContext context, WidgetThemeEntry widgetTheme) {
                if (!Mods.Thaumcraft.isLoaded()) return;
                if (!wrapper.hasWand()) return;
                String missingResearch = wrapper.getMissingResearch();
                if (missingResearch == null || missingResearch.isEmpty()) return;

                Minecraft mc = Minecraft.getMinecraft();
                String line1 = IKey.lang("gui.backpack.missing_research")
                    .get();
                int line1Width = mc.fontRenderer.getStringWidth(line1);

                String line2 = wrapper.getMissingResearchName();
                if (line2 == null || line2.isEmpty()) line2 = missingResearch;
                int line2Width = mc.fontRenderer.getStringWidth(line2);

                GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                RenderHelper.disableStandardItemLighting();
                GL11.glDisable(GL11.GL_LIGHTING);
                mc.fontRenderer.drawString(line1, (60 - line1Width) / 2, 0, 0xFF4444);
                mc.fontRenderer.drawString(line2, (60 - line2Width) / 2, 10, 0xFFAA00);
                GL11.glPopAttrib();
            }
        };
        missingResearchText.pos(47, 18 * 3 + 32)
            .size(60, 20);
        craftingGroupsWidget.child(missingResearchText);

        Flow craftingRow = Flow.row()
            .coverChildrenHeight()
            .childPadding(2);
        craftingRow.child(craftingGroupsWidget)
            .child(
                Flow.column()
                    .coverChildren()
                    .childPadding(2)
                    .top(1)
                    .child(rotated)
                    .child(grid)
                    .child(clear));

        Flow column = Flow.column()
            .pos(8, 28)
            .coverChildren()
            .childPadding(2)
            .child(buttonRow)
            .child(craftingRow);

        child(column);
        height(160);
    }

    private boolean hasInsufficientVis() {
        if (!Mods.Thaumcraft.isLoaded()) return false;

        Map<String, Integer> required = wrapper.getRequiredAspects();
        if (required == null || required.isEmpty()) return false;

        ItemStack wandStack = wrapper.getStorage()
            .getStackInSlot(IArcaneCraftingUpgrade.WAND_SLOT_INDEX);
        if (wandStack == null) return true;

        for (Map.Entry<String, Integer> entry : required.entrySet()) {
            if (entry.getValue() <= 0) continue;
            int current = ThaumcraftHelpers.getWandVis(wandStack, entry.getKey());
            if (current < entry.getValue() * 100) return true;
        }
        return false;
    }

    private boolean canCraft() {
        String missingResearch = wrapper.getMissingResearch();
        if (missingResearch != null && !missingResearch.isEmpty()) return false;
        Map<String, Integer> required = wrapper.getRequiredAspects();
        if (required == null || required.isEmpty()) return true;
        if (!wrapper.hasWand()) return false;
        return !hasInsufficientVis();
    }

    @Override
    protected ArcaneCraftingUpgradeWrapper getWrapper() {
        return wrapper;
    }

    public void updateWrapper() {
        this.getSyncHandler()
            .syncToServer(UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_ARCANE_CRAFTING_S), buf -> {
                NetworkUtils.writeEnumValue(buf, wrapper.getCraftingDes());
                buf.writeBoolean(wrapper.isUseBackpack());
            });
    }

    private static class SingleAspectWidget extends Widget<SingleAspectWidget> {

        private final ArcaneCraftingUpgradeWrapper wrapper;
        private final String aspectTag;

        public SingleAspectWidget(ArcaneCraftingUpgradeWrapper wrapper, String aspectTag) {
            this.wrapper = wrapper;
            this.aspectTag = aspectTag;
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            if (!Mods.Thaumcraft.isLoaded()) return;

            Map<String, Integer> requiredAspects = wrapper.getRequiredAspects();
            String missingResearch = wrapper.getMissingResearch();
            boolean hasMissingResearch = missingResearch != null && !missingResearch.isEmpty();
            int amount = (wrapper.hasWand() && !hasMissingResearch) ? requiredAspects.getOrDefault(aspectTag, 0) : 0;

            int color = ThaumcraftHelpers.getAspectColor(aspectTag);
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;

            Minecraft mc = Minecraft.getMinecraft();

            float alpha;
            if (!wrapper.hasWand() || hasMissingResearch) {
                alpha = 0.3f;
            } else if (amount > 0 && isInsufficient()) {
                long time = System.currentTimeMillis();
                alpha = (float) (0.4 + 0.6 * Math.abs(Math.sin(time / 200.0)));
            } else {
                alpha = amount > 0 ? 1.0f : 0.3f;
            }

            ResourceLocation aspectTexture = new ResourceLocation(
                "thaumcraft",
                "textures/aspects/" + aspectTag + ".png");
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

            if (amount > 0 && wrapper.hasWand()) {
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
            Map<String, Integer> required = wrapper.getRequiredAspects();
            int amount = required.getOrDefault(aspectTag, 0);
            if (amount <= 0) return false;

            ItemStack wandStack = wrapper.getStorage()
                .getStackInSlot(IArcaneCraftingUpgrade.WAND_SLOT_INDEX);
            if (wandStack == null) return true;

            int current = ThaumcraftHelpers.getWandVis(wandStack, aspectTag);
            return current < amount * 100;
        }
    }
}
