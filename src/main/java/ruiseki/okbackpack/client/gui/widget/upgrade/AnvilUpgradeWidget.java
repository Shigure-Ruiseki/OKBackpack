package ruiseki.okbackpack.client.gui.widget.upgrade;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.common.item.anvil.AnvilUpgradeWrapper;
import ruiseki.okcore.helper.LangHelpers;

public class AnvilUpgradeWidget extends ExpandedUpgradeTabWidget<AnvilUpgradeWrapper> {

    private final AnvilUpgradeWrapper wrapper;
    private final TextFieldWidget nameField;
    private final StringValue nameStringValue;
    private String prevName = "";
    private boolean hasInput;
    private boolean editable = false;

    public AnvilUpgradeWidget(int slotIndex, AnvilUpgradeWrapper wrapper, ItemStack stack, IStoragePanel<?> panel,
        String titleKey) {
        super(slotIndex, 5, stack, titleKey, 115);
        this.wrapper = wrapper;

        this.syncHandler("upgrades", slotIndex);

        // Determine initial state from slot 0
        ItemStack leftInput = wrapper.getStorage()
            .getStackInSlot(0);
        hasInput = leftInput != null;
        if (hasInput) {
            String name = wrapper.getRepairedItemName();
            if (name == null || name.isEmpty()) {
                name = leftInput.getDisplayName();
            }
            prevName = name;
            editable = true;
        }

        // Text field for renaming - blocks input when not editable
        nameStringValue = new StringValue(prevName);
        nameField = new TextFieldWidget() {

            @NotNull
            @Override
            public Interactable.Result onMousePressed(int button) {
                if (!editable) return Interactable.Result.IGNORE;
                return super.onMousePressed(button);
            }

            @NotNull
            @Override
            public Interactable.Result onKeyPressed(char character, int keyCode) {
                if (!editable) return Interactable.Result.IGNORE;
                return super.onKeyPressed(character, keyCode);
            }
        }.value(nameStringValue)
            .setMaxLength(256)
            .background(hasInput ? OKBGuiTextures.ANVIL_TEXT_FIELD_ENABLED : OKBGuiTextures.ANVIL_TEXT_FIELD_DISABLED)
            .size(100, 16)
            .pos(8, 28);

        // Slots container with absolute positioning
        SlotGroupWidget slotsGroup = new SlotGroupWidget().coverChildren();

        ItemSlot leftSlot = new ItemSlot().syncHandler("anvil_slot_" + slotIndex, 0)
            .pos(0, 0)
            .name("anvil_left_" + slotIndex);
        slotsGroup.child(leftSlot);

        // Plus sign texture
        @SuppressWarnings({ "rawtypes" })
        Widget plusWidget = (Widget) new Widget() {

            @Override
            public void draw(ModularGuiContext context, WidgetThemeEntry widgetTheme) {
                OKBGuiTextures.ANVIL_PLUS_SIGN.draw(0, 0, 13, 13);
            }
        }.size(13, 13)
            .pos(20, 2);
        slotsGroup.child(plusWidget);

        ItemSlot rightSlot = new ItemSlot().syncHandler("anvil_slot_" + slotIndex, 1)
            .pos(35, 0)
            .name("anvil_right_" + slotIndex);
        slotsGroup.child(rightSlot);

        // Arrow with conditional red cross overlay
        @SuppressWarnings({ "rawtypes" })
        Widget arrowWidget = (Widget) new Widget() {

            @Override
            public void draw(ModularGuiContext context, WidgetThemeEntry widgetTheme) {
                OKBGuiTextures.ANVIL_ARROW.draw(0, 0, 14, 15);
                // Show red cross when first input has item but no output
                ItemStack left = wrapper.getStorage()
                    .getStackInSlot(0);
                ItemStack output = wrapper.getStorage()
                    .getStackInSlot(2);
                if (left != null && output == null) {
                    OKBGuiTextures.ANVIL_RED_CROSS.draw(0, 0, 15, 15);
                }
            }
        }.size(15, 15)
            .pos(55, 1);
        slotsGroup.child(arrowWidget);

        // Output slot
        ItemSlot outputSlot = new ItemSlot().syncHandler("anvil_slot_" + slotIndex, 2)
            .pos(72, 0)
            .name("anvil_output_" + slotIndex);
        slotsGroup.child(outputSlot);

        // Cost display widget
        @SuppressWarnings({ "rawtypes" })
        Widget costWidget = (Widget) new Widget() {

            @Override
            public void draw(ModularGuiContext context, WidgetThemeEntry widgetTheme) {
                int cost = wrapper.getMaximumCost();
                if (cost <= 0) return;

                ItemStack output = wrapper.getStorage()
                    .getStackInSlot(2);
                if (output == null) return;

                FontRenderer font = Minecraft.getMinecraft().fontRenderer;
                Minecraft mc = Minecraft.getMinecraft();

                boolean tooExpensive = cost >= 40
                    && (mc.thePlayer == null || !mc.thePlayer.capabilities.isCreativeMode);
                String costText;
                int color;

                if (tooExpensive) {
                    costText = "\u00a7c" + LangHelpers.localize("container.repair.expensive");
                    color = 0xFF6060;
                } else {
                    costText = LangHelpers.localize("container.repair.cost", cost);
                    boolean canAfford = mc.thePlayer != null
                        && (mc.thePlayer.capabilities.isCreativeMode || mc.thePlayer.experienceLevel >= cost);
                    color = canAfford ? 0x80FF20 : 0xFF6060;
                }

                int textWidth = font.getStringWidth(costText);
                int drawX = (100 + 8 - textWidth) / 2;
                GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                RenderHelper.disableStandardItemLighting();
                GL11.glDisable(GL11.GL_LIGHTING);
                font.drawStringWithShadow(costText, drawX, 0, color);
                GL11.glPopAttrib();
            }
        }.size(100, 12);

        // Layout with absolute positioning
        slotsGroup.pos(13, 48);
        costWidget.pos(8, 70);

        child(nameField);
        child(slotsGroup);
        child(costWidget);
        height(90);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        ItemStack leftInput = wrapper.getStorage()
            .getStackInSlot(0);
        boolean currentHasInput = leftInput != null;

        if (currentHasInput != hasInput) {
            hasInput = currentHasInput;
            if (hasInput) {
                // Item placed - initialize text to display name, enable editing
                String displayName = leftInput.getDisplayName();
                editable = true;
                prevName = displayName;
                nameStringValue.setStringValue(displayName);
                nameField.setText(displayName);
                nameField.background(OKBGuiTextures.ANVIL_TEXT_FIELD_ENABLED);
                wrapper.setRepairedItemName(displayName);
                sendNameUpdate(displayName);
            } else {
                // Item removed - clear text, disable editing
                editable = false;
                prevName = "";
                nameStringValue.setStringValue("");
                nameField.setText("");
                nameField.background(OKBGuiTextures.ANVIL_TEXT_FIELD_DISABLED);
                wrapper.setRepairedItemName("");
                sendNameUpdate("");
                wrapper.updateRepairOutput();
            }
        }

        // Detect text changes from user typing
        if (editable) {
            String txt = nameField.getText();
            if (txt == null) txt = "";
            if (!txt.equals(prevName)) {
                prevName = txt;
                sendNameUpdate(txt);
                // Update client-side wrapper for immediate cost refresh
                wrapper.setRepairedItemName(txt);
                wrapper.updateRepairOutput();
            }
        }
    }

    @Override
    protected AnvilUpgradeWrapper getWrapper() {
        return wrapper;
    }

    private void sendNameUpdate(String name) {
        if (getSlotSyncHandler() != null) {
            getSlotSyncHandler().syncToServer(
                UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_ANVIL_NAME),
                buf -> buf.writeStringToBuffer(name));
        }
    }
}
