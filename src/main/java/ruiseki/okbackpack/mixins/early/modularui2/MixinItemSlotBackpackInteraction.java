package ruiseki.okbackpack.mixins.early.modularui2;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.client.gui.interaction.BackpackInventoryInteractionOverlay;
import ruiseki.okbackpack.client.gui.interaction.BackpackInventoryInteractionResult;

@Mixin(ItemSlot.class)
public abstract class MixinItemSlotBackpackInteraction {

    @Inject(method = "draw", at = @At("TAIL"), remap = false)
    private void okbackpack$renderInventoryInteractionOverlay(ModularGuiContext context,
        WidgetThemeEntry<?> widgetTheme, CallbackInfo ci) {
        ItemSlot self = (ItemSlot) (Object) this;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null || minecraft.fontRenderer == null) {
            return;
        }

        ItemStack cursorStack = minecraft.thePlayer.inventory.getItemStack();
        if (cursorStack == null || cursorStack.stackSize <= 0) {
            return;
        }

        BackpackInventoryInteractionResult result = BackpackInventoryInteractionOverlay
            .getRenderableResult(minecraft.thePlayer, self.getSlot(), cursorStack);
        BackpackInventoryInteractionOverlay.renderOverlayAt(minecraft.fontRenderer, result, 1, 1);
    }

    @Inject(method = "drawForeground", at = @At("HEAD"), cancellable = true, remap = false)
    private void okbackpack$renderInventoryInteractionTooltip(ModularGuiContext context, CallbackInfo ci) {
        ItemSlot self = (ItemSlot) (Object) this;
        if (context == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null || minecraft.fontRenderer == null) {
            return;
        }

        ItemStack cursorStack = minecraft.thePlayer.inventory.getItemStack();
        if (cursorStack == null || cursorStack.stackSize <= 0) {
            return;
        }

        BackpackInventoryInteractionResult result = BackpackInventoryInteractionOverlay
            .getRenderableResult(minecraft.thePlayer, self.getSlot(), cursorStack);
        if (!result.canRenderOverlay()) {
            return;
        }

        RichTooltip tooltip = self.getTooltip();
        int showUpTimer = tooltip != null ? tooltip.getShowUpTimer() : 0;
        if (!self.isHoveringFor(showUpTimer)) {
            return;
        }

        List<String> lines = BackpackInventoryInteractionOverlay
            .buildTooltipLines(minecraft.thePlayer, self.getSlot(), cursorStack);
        if (lines.isEmpty()) {
            return;
        }

        BackpackInventoryInteractionOverlay.renderTooltip(
            minecraft.fontRenderer,
            lines,
            context.getAbsMouseX(),
            context.getAbsMouseY(),
            (tooltipLines, x, y) -> RichTooltip.injectRichTooltip(result.getBackpackStack(), tooltipLines, x, y));
        ci.cancel();
    }
}
