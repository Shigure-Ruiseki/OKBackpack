package ruiseki.okbackpack.client.gui.slot;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;

import ruiseki.okbackpack.common.item.refill.TargetSlot;
import ruiseki.okcore.helper.LangHelpers;

public class RefillPhantomSlot extends PhantomItemSlot {

    private final Supplier<TargetSlot> targetSlotGetter;
    private final Consumer<UpOrDown> scrollCallback;

    /**
     * @param targetSlotGetter supplies the current TargetSlot to display (including pending changes)
     * @param scrollCallback   called on scroll with direction, the widget manages state transitions
     */
    public RefillPhantomSlot(Supplier<TargetSlot> targetSlotGetter, Consumer<UpOrDown> scrollCallback) {
        this.targetSlotGetter = targetSlotGetter;
        this.scrollCallback = scrollCallback;
    }

    @Override
    public void buildTooltip(ItemStack stack, RichTooltip tooltip) {
        super.buildTooltip(stack, tooltip);
        if (stack == null) return;

        TargetSlot ts = targetSlotGetter.get();
        String slotDescription = resolveTooltipText(ts);
        tooltip.addLine(LangHelpers.localize("gui.backpack.refill.target_tooltip", slotDescription));
        tooltip.addLine(LangHelpers.localize("gui.backpack.refill.scroll_hint"));
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);

        if (!isSynced()) return;
        ItemStack stack = getSlot().getStack();
        if (stack == null) return;

        TargetSlot ts = targetSlotGetter.get();
        String text = resolveAcronym(ts);
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        int textWidth = font.getStringWidth(text);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0, 200);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        font.drawStringWithShadow(text, 18 - 1 - textWidth, 1, 0xFF55FF55);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        if (!isSynced() || getSlot().getStack() == null) {
            return super.onMouseScroll(scrollDirection, amount);
        }
        scrollCallback.accept(scrollDirection);
        return true;
    }

    public static String resolveAcronym(TargetSlot ts) {
        if (ts.getAcronymKey() != null) {
            return LangHelpers.localize(ts.getAcronymKey());
        }
        return String.valueOf(ts.getHotbarIndex() + 1);
    }

    public static String resolveTooltipText(TargetSlot ts) {
        if (ts == TargetSlot.MAIN_HAND || ts == TargetSlot.ANY) {
            return LangHelpers.localize(ts.getTooltipKey());
        }
        return LangHelpers.localize(ts.getTooltipKey(), ts.getHotbarIndex() + 1);
    }
}
