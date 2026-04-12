package ruiseki.okbackpack.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widgets.ButtonWidget;

import lombok.Getter;
import lombok.Setter;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;

public class CyclicVariantButtonWidget extends ButtonWidget<CyclicVariantButtonWidget> {

    @Getter
    protected int index = 0;

    protected final List<Variant> variants;

    private int iconOffset = 2;
    private int iconSize = 16;

    private int buttonWidth = 20;
    private int buttonHeight = 20;

    private boolean hasCustomTexture = false;

    private IDrawable notHoveredTexture = OKBGuiTextures.STANDARD_BUTTON;
    private IDrawable hoveredTexture = OKBGuiTextures.STANDARD_BUTTON_HOVERED;

    private final List<IntConsumer> mousePressedUpdaters = new ArrayList<>();

    @Getter
    @Setter
    private boolean inEffect = true;

    public CyclicVariantButtonWidget(List<Variant> variants, int index, int iconOffset, int iconSize,
        IntConsumer mousePressedUpdater) {
        this.variants = variants;
        this.index = index;
        this.iconOffset = iconOffset;
        this.iconSize = iconSize;

        if (mousePressedUpdater != null) {
            this.mousePressedUpdaters.add(mousePressedUpdater);
        }

        this.size(buttonWidth, buttonHeight)
            .onMousePressed(mouseButton -> {

                if (mouseButton == 1) {
                    this.index = (this.index - 1 + variants.size()) % variants.size();
                } else {
                    this.index = (this.index + 1) % variants.size();
                }

                for (IntConsumer updater : mousePressedUpdaters) {
                    updater.accept(this.index);
                }

                markTooltipDirty();
                return true;
            })
            .tooltipDynamic(tooltip -> {

                tooltip.addLine(variants.get(this.index).name);

                if (!inEffect) {
                    tooltip.addLine(
                        IKey.lang("gui.backpack.not_in_effect")
                            .style(IKey.RED));
                }

                tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
            });
    }

    public CyclicVariantButtonWidget(List<Variant> variants, int index, IntConsumer mousePressedUpdater) {
        this(variants, index, 2, 16, mousePressedUpdater);
    }

    public CyclicVariantButtonWidget(List<Variant> variants, IntConsumer mousePressedUpdater) {
        this(variants, 0, 2, 16, mousePressedUpdater);
    }

    public CyclicVariantButtonWidget addMousePressedUpdater(IntConsumer updater) {
        if (updater != null && !this.mousePressedUpdaters.contains(updater)) {
            this.mousePressedUpdaters.add(updater);
        }
        return this;
    }

    public CyclicVariantButtonWidget setIndex(int index) {
        this.index = Math.floorMod(index, variants.size());
        markTooltipDirty();
        return this;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {

        if (hasCustomTexture) {

            if (isHovering()) {
                hoveredTexture.draw(context, 0, 0, buttonWidth, buttonHeight, widgetTheme.getTheme());
            } else {
                notHoveredTexture.draw(context, 0, 0, buttonWidth, buttonHeight, widgetTheme.getTheme());
            }
        }

        super.draw(context, widgetTheme);
    }

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.drawOverlay(context, widgetTheme);

        IDrawable drawable = variants.get(index).drawable;

        if (context != null) {
            drawable.draw(context, iconOffset, iconOffset, iconSize, iconSize, widgetTheme.getTheme());
        }
    }

    public static class Variant {

        public final IKey name;
        public final IDrawable drawable;

        public Variant(IKey name, IDrawable drawable) {
            this.name = name;
            this.drawable = drawable;
        }
    }
}
