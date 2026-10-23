package ruiseki.okstoragecore.client.gui;

import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.UITexture;

import ruiseki.okstoragecore.Reference;

public class OSCTextures {

    private static final ResourceLocation ICON_LOCATION = new ResourceLocation(Reference.MOD_ID, "gui/icons");
    private static final ResourceLocation CRAFTING_TWEAKS = new ResourceLocation(
        Reference.MOD_ID,
        "gui/crafting_tweaks");

    public static final IDrawable SHIFT_CLICK_INVENTORY = icon("shift_click_inv", 64, 96);
    public static final IDrawable SHIFT_CLICK_OPEN_TAB = icon("shift_click_tab", 80, 32);

    public static final IDrawable SETTING = icon("setting", 16, 96);
    public static final IDrawable BACK = icon("back", 64, 80);

    public static final IDrawable FILL_CRAFTING = icon("fill_crafting", 32, 112);
    public static final IDrawable DO_NOT_FILL_CRAFTING = icon("not_fill_crafting", 48, 112);
    public static final UITexture SHIFT_CLICK_INTO_STORAGE = icon("into_storage", 32, 48);
    public static final UITexture SHIFT_CLICK_INTO_INVENTORY = icon("into_inventory", 48, 48);

    public static final UITexture ROTATED_RIGHT = icon("rotated_right", 0, 0, CRAFTING_TWEAKS);
    public static final UITexture CLEAR = icon("clear", 16, 0, CRAFTING_TWEAKS);
    public static final UITexture BALANCE = icon("balance", 32, 0, CRAFTING_TWEAKS);
    public static final UITexture ROTATED_LEFT = icon("rotated_left", 48, 0, CRAFTING_TWEAKS);
    public static final UITexture SPREAD = icon("spread", 80, 0, CRAFTING_TWEAKS);

    public static UITexture icon(String name, int x, int y) {
        return icon(name, x, y, 16, 16, ICON_LOCATION);
    }

    public static UITexture icon(String name, int x, int y, ResourceLocation location) {
        return icon(name, x, y, 16, 16, location);
    }

    public static UITexture icon(String name, int x, int y, int w, int h, ResourceLocation location) {
        return UITexture.builder()
            .location(location)
            .imageSize(256, 256)
            .xy(x, y, w, h)
            .name(name)
            .build();
    }
}
