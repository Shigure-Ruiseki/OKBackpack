package ruiseki.okstoragecore.client.gui;

import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.drawable.UITexture;

import ruiseki.okstoragecore.Reference;

public class OSCTextures {

    private static final ResourceLocation ICON_LOCATION = new ResourceLocation(Reference.MOD_ID, "gui/icons");
    private static final ResourceLocation GUI_CONTROLS = new ResourceLocation(Reference.MOD_ID, "gui/gui_controls.png");
    private static final ResourceLocation CRAFTING_TWEAKS = new ResourceLocation(
        Reference.MOD_ID,
        "gui/crafting_tweaks");

    public static final IDrawable SHIFT_CLICK_INVENTORY = icon("shift_click_inv", 64, 96);
    public static final IDrawable SHIFT_CLICK_OPEN_TAB = icon("shift_click_tab", 80, 32);

    public static final UITexture CHECK_ICON = icon("check", 0, 0);
    public static final UITexture CROSS_ICON = icon("cross", 16, 0);

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

    public static final UITexture FULL_HUNGER = icon("full_hunger", 96, 0);
    public static final UITexture HALF_HUNGER = icon("half_hunger", 112, 0);
    public static final UITexture ANY_HUNGER = icon("any_hunger", 128, 0);

    public static final UITexture HALF_HEART = icon("half_heart", 96, 16);
    public static final UITexture IGNORE_HALF_HEART = icon("ignore_half_heart", 112, 16);

    public static final UITexture MATCH_MOD = icon("match_mod", 32, 16);
    public static final IDrawable MATCH_ITEM = new ItemDrawable(Items.apple);
    public static final UITexture MATCH_TAG = icon("match_tag", 64, 0);

    public static final UITexture MATCH_DURABILITY = icon("match_duration", 0, 16);
    public static final UITexture IGNORE_DURABILITY = icon("ignore_duration", 16, 16);

    public static final IDrawable ADD = icon("add", 96, 32);
    public static final IDrawable REMOVE = icon("remove", 112, 32);

    public static final UITexture MATCH_NBT = icon("match_nbt", 32, 0);
    public static final UITexture IGNORE_NBT = icon("ignore_nbt", 48, 0);

    public static final UITexture TOGGLE_DISABLE_ICON = icon("disable", 0, 128, 4, 10);
    public static final UITexture TOGGLE_ENABLE_ICON = icon("enable", 4, 128, 4, 10);

    // Battery upgrade GUI textures
    public static final UITexture BATTERY_BAR_BACKGROUND = UITexture.builder()
        .location(GUI_CONTROLS)
        .imageSize(256, 256)
        .xy(29, 30, 18, 54)
        .name("battery_bar_bg")
        .build();

    public static final UITexture BATTERY_OVERLAY = UITexture.builder()
        .location(GUI_CONTROLS)
        .imageSize(256, 256)
        .xy(47, 56, 16, 18)
        .name("battery_overlay")
        .build();

    public static final UITexture BATTERY_CHARGE_SEGMENT = UITexture.builder()
        .location(GUI_CONTROLS)
        .imageSize(256, 256)
        .xy(47, 74, 16, 6)
        .name("battery_charge_segment")
        .build();

    public static final UITexture BATTERY_CONNECTION_TOP = UITexture.builder()
        .location(GUI_CONTROLS)
        .imageSize(256, 256)
        .xy(47, 48, 16, 4)
        .name("battery_connection_top")
        .build();

    public static final UITexture BATTERY_CONNECTION_BOTTOM = UITexture.builder()
        .location(GUI_CONTROLS)
        .imageSize(256, 256)
        .xy(47, 52, 16, 4)
        .name("battery_connection_bottom")
        .build();

    public static UITexture icon(String name, int x, int y) {
        return icon(name, x, y, 16, 16, ICON_LOCATION);
    }

    public static UITexture icon(String name, int x, int y, ResourceLocation location) {
        return icon(name, x, y, 16, 16, location);
    }

    public static UITexture icon(String name, int x, int y, int w, int h) {
        return icon(name, x, y, w, h, ICON_LOCATION);
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
