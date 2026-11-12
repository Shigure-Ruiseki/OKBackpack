package ruiseki.okstoragecore.upgrades.anvil;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import ruiseki.backpack.client.gui.sync.OBUpgradeSlotSHRegistries;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedStringSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class AnvilUpgradeTab extends ExpandedUpgradeTab<AnvilUpgradeWrapper> {

    protected static final List<CyclicVariantButtonWidget.Variant> SHIFT_CLICK_INTO_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.shift_click_into_inventory"),
            OSCTextures.SHIFT_CLICK_INTO_INVENTORY),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.shift_click_into_storage"),
            OSCTextures.SHIFT_CLICK_INTO_STORAGE));

    protected TextFieldWidget nameField;

    public AnvilUpgradeTab(int slotIndex, AnvilUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
        super(slotIndex, wrapper, stack, storagePanel, "gui.okbackpack.upgrades.anvil");
    }

    @Override
    protected Flow addUpgradeColumn() {
        return super.addUpgradeColumn().child(this.addButtonRow())
            .child(this.addAnvilColumn());
    }

    protected Flow addButtonRow() {
        return Flow.row()
            .coverChildrenHeight()
            .childPadding(2)
            .child(this.addShiftClickIntoButton());
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
                        OBUpgradeSlotSHRegistries.SHIFT_INTO_STORAGE,
                        buffer -> buffer.writeBoolean(index == 0));
            }).size(20, 20);
    }

    protected Flow addAnvilColumn() {
        return Flow.column()
            .coverChildren()
            .childPadding(2)
            .collapseDisabledChild()
            .child(this.addNameField())
            .child(this.addSlotsRow())
            .child(this.addRequiredField());
    }

    protected TextFieldWidget addNameField() {
        this.nameField = new TextFieldWidget() {

            private boolean lastHasInput;

            @Override
            public void onInit() {
                super.onInit();
                this.lastHasInput = hasInput();
                this.updateBackground();
            }

            private void updateBackground() {
                boolean hasInput = hasInput();
                if (this.lastHasInput != hasInput) {
                    this.lastHasInput = hasInput;
                    background(hasInput ? OSCTextures.ANVIL_TEXT_FIELD_ENABLED : OSCTextures.ANVIL_TEXT_FIELD_DISABLED);
                }
            }

            @Override
            public void onUpdate() {
                super.onUpdate();
                this.updateBackground();
            }

            @NotNull
            @Override
            public Interactable.Result onMousePressed(int button) {
                if (!hasInput()) {
                    return Interactable.Result.IGNORE;
                }
                return super.onMousePressed(button);
            }

            @NotNull
            @Override
            public Interactable.Result onKeyPressed(char character, int keyCode) {
                if (!hasInput()) {
                    return Interactable.Result.IGNORE;
                }
                return super.onKeyPressed(character, keyCode);
            }

            @Override
            public void onFocus(ModularGuiContext context) {
                if (!hasInput()) {
                    context.removeFocus();
                    return;
                }
                super.onFocus(context);
            }
        }.syncHandler("anvil_name_field_" + this.slotIndex)
            .autoUpdateOnChange(true)
            .setMaxLength(256)
            .background(this.hasInput() ? OSCTextures.ANVIL_TEXT_FIELD_ENABLED : OSCTextures.ANVIL_TEXT_FIELD_DISABLED)
            .size(100, 16);

        return this.nameField;
    }

    protected boolean hasInput() {
        return !ItemHelpers.isEmpty(
            wrapper.getStorage()
                .getStackInSlot(0));
    }

    public Flow addSlotsRow() {
        return Flow.row()
            .coverChildren()
            .child(this.addLeftSlot())
            .child(this.addPlusWidget())
            .child(this.addRightSlot())
            .child(this.addArrowWidget())
            .child(this.addOutputSlot());
    }

    protected ItemSlot addLeftSlot() {
        return new ItemSlot().syncHandler("anvil_slot_" + slotIndex, 0)
            .name("anvil_left_" + slotIndex);
    }

    @SuppressWarnings({ "rawtypes" })
    protected Widget<?> addPlusWidget() {
        return (Widget) new Widget().background(OSCTextures.ANVIL_PLUS_SIGN)
            .size(13, 13)
            .margin(2);
    }

    protected ItemSlot addRightSlot() {
        return new ItemSlot().syncHandler("anvil_slot_" + slotIndex, 1)
            .name("anvil_right_" + slotIndex);
    }

    @SuppressWarnings({ "rawtypes" })
    protected Widget<?> addArrowWidget() {
        return (Widget) new Widget() {

            @Override
            public void draw(ModularGuiContext context, WidgetThemeEntry widgetTheme) {
                OSCTextures.ANVIL_ARROW.draw(0, 0, 14, 15);
                // Show red cross when first input has item but no output
                ItemStack left = wrapper.getStorage()
                    .getStackInSlot(0);
                ItemStack output = getStoragePanel().getSyncManager()
                    .findSyncHandler("anvil_result_" + slotIndex, 0, ItemSlotSH.class)
                    .getSlot()
                    .getStack();
                if (left != null && output == null) {
                    OSCTextures.ANVIL_RED_CROSS.draw(0, 0, 15, 15);
                }
            }
        }.size(15, 15)
            .margin(1);
    }

    protected ItemSlot addOutputSlot() {
        return new ItemSlot().syncHandler("anvil_result_" + slotIndex, 0)
            .name("anvil_result_" + slotIndex);
    }

    protected TextWidget<?> addRequiredField() {
        DelegatedStringSH requiredField = this.getSyncManager()
            .findSyncHandler("anvil_required_field_" + slotIndex, DelegatedStringSH.class);
        return new TextWidget<>(IKey.dynamic(requiredField::getStringValue)).background((IDrawable) null)
            .height(16)
            .setEnabledIf(
                field -> !requiredField.getStringValue()
                    .isEmpty())
            .textAlign(Alignment.Center);
    }
}
