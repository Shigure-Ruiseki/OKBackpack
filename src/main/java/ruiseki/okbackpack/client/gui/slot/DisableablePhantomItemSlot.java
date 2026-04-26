package ruiseki.okbackpack.client.gui.slot;

import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;

import ruiseki.okbackpack.client.gui.OKBGuiTextures;

public class DisableablePhantomItemSlot extends PhantomItemSlot {

    private final BooleanSupplier disabledSupplier;

    public DisableablePhantomItemSlot(BooleanSupplier disabledSupplier) {
        this.disabledSupplier = disabledSupplier;
    }

    @Override
    public void drawBackground(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        if (disabledSupplier.getAsBoolean()) {
            OKBGuiTextures.SLOT_DISABLED.draw(context, 0, 0, 18, 18, widgetTheme.getTheme());
        } else {
            super.drawBackground(context, widgetTheme);
        }
    }

    @Override
    public @NotNull Interactable.Result onMousePressed(int mouseButton) {
        if (disabledSupplier.getAsBoolean()) return Interactable.Result.SUCCESS;
        return super.onMousePressed(mouseButton);
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        if (disabledSupplier.getAsBoolean()) return true;
        return super.onMouseRelease(mouseButton);
    }

    @Override
    public @NotNull Interactable.Result onMouseTapped(int mouseButton) {
        if (disabledSupplier.getAsBoolean()) return Interactable.Result.SUCCESS;
        return super.onMouseTapped(mouseButton);
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        if (disabledSupplier.getAsBoolean()) return true;
        return super.onMouseScroll(scrollDirection, amount);
    }
}
