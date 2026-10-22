package ruiseki.okstoragecore.client.gui;

import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.UITexture;

import ruiseki.okstoragecore.Reference;

public class OSCTextures {

    private static final ResourceLocation ICON_LOCATION = new ResourceLocation(Reference.MOD_ID, "gui/icons");

    public static final IDrawable SETTING = icon("setting", 16, 96);
    public static final IDrawable BACK = icon("back", 64, 80);

    public static final IDrawable FILL_CRAFTING = icon("fill_crafting", 32, 112);
    public static final IDrawable DO_NOT_FILL_CRAFTING = icon("not_fill_crafting", 48, 112);

    public static UITexture icon(String name, int x, int y) {
        return icon(name, x, y, 16, 16);
    }

    public static UITexture icon(String name, int x, int y, int w, int h) {
        return UITexture.builder()
            .location(ICON_LOCATION)
            .imageSize(256, 256)
            .xy(x, y, w, h)
            .name(name)
            .build();
    }
}
