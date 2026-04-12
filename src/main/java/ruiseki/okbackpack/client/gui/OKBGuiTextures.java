package ruiseki.okbackpack.client.gui;

import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.AdaptableUITexture;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.drawable.UITexture;

import ruiseki.okbackpack.Reference;

public class OKBGuiTextures {

    private OKBGuiTextures() {}

    public static final UITexture VANILLA_SEARCH_BACKGROUND = UITexture.builder()
        .location(Reference.MOD_ID, "gui/vanilla_search")
        .imageSize(18, 18)
        .adaptable(1)
        .name("vanilla_search")
        .build();

    public static final UITexture BREEDER_PROGRESS = UITexture.builder()
        .location(Reference.MOD_ID, "gui/widgets/progress_breeder")
        .imageSize(36, 36)
        .build();

    public static final UITexture BASIC_BAR = UITexture.builder()
        .location(Reference.MOD_ID, "gui/progress_white")
        .imageSize(64, 32)
        .build();

    // Furnace GUI textures (from vanilla furnace texture)
    private static final ResourceLocation FURNACE_GUI = new ResourceLocation(
        "minecraft",
        "textures/gui/container/furnace.png");

    public static final UITexture FURNACE_FLAME_BACKGROUND = UITexture.builder()
        .location(FURNACE_GUI)
        .imageSize(256, 256)
        .xy(56, 36, 14, 14)
        .name("furnace_flame_bg")
        .build();

    public static final UITexture FURNACE_FLAME_FOREGROUND = UITexture.builder()
        .location(FURNACE_GUI)
        .imageSize(256, 256)
        .xy(176, 0, 14, 14)
        .name("furnace_flame_fg")
        .build();

    public static final UITexture FURNACE_ARROW_BACKGROUND = UITexture.builder()
        .location(FURNACE_GUI)
        .imageSize(256, 256)
        .xy(79, 35, 24, 17)
        .name("furnace_arrow_bg")
        .build();

    public static final UITexture FURNACE_ARROW_FOREGROUND = UITexture.builder()
        .location(FURNACE_GUI)
        .imageSize(256, 256)
        .xy(176, 14, 24, 17)
        .name("furnace_arrow_fg")
        .build();

    public static final UITexture ROOST_SLOT = UITexture.builder()
        .location(Reference.MOD_ID, "gui/slot/roost_slot")
        .imageSize(18, 18)
        .build();

    public static final UITexture FOOD_SLOT = UITexture.builder()
        .location(Reference.MOD_ID, "gui/slot/food_slot")
        .imageSize(18, 18)
        .build();

    public static final UITexture EMPTY_SLOT = UITexture.builder()
        .location(Reference.MOD_ID, "gui/slot/empty_slot")
        .imageSize(18, 18)
        .build();

    public static final UITexture VARIABLE_SLOT = UITexture.builder()
        .location(Reference.MOD_ID, "gui/slot/variable_slot")
        .imageSize(18, 18)
        .build();

    public static final AdaptableUITexture DML_INVENTORY_TEXTURE = (AdaptableUITexture) UITexture.builder()
        .location(Reference.MOD_ID, "gui/deepMobLearning/player_inventory")
        .imageSize(256, 256)
        .xy(0, 0, 177, 91)
        .adaptable(4)
        .tiled()
        .build();

    public static final AdaptableUITexture DML_INVENTORY_SLOT = (AdaptableUITexture) UITexture.builder()
        .location(Reference.MOD_ID, "gui/deepMobLearning/player_inventory")
        .imageSize(256, 256)
        .xy(8, 8, 18, 18)
        .adaptable(1)
        .tiled()
        .build();

    private static final ResourceLocation GUI_CONTROLS = new ResourceLocation(Reference.MOD_ID, "gui/gui_controls.png");

    private static final ResourceLocation ICON_LOCATION = new ResourceLocation(Reference.MOD_ID, "gui/icons");

    public static final UITexture CHECK_ICON = icon("check", 0, 0);
    public static final UITexture CROSS_ICON = icon("cross", 16, 0);

    public static final UITexture MATCH_NBT_ICON = icon("consider_nbt", 32, 0);
    public static final UITexture IGNORE_NBT_ICON = icon("ignore_nbt", 48, 0);

    public static final UITexture COMPLETE_HUNGER_ICON = icon("complete_hunger", 96, 0);
    public static final UITexture HALF_HUNGER_ICON = icon("half_hunger", 112, 0);
    public static final UITexture IMMEDIATE_HUNGER_ICON = icon("impose_hunger", 128, 0);

    public static final UITexture MATCH_DURABILITY_ICON = icon("consider_duration", 0, 16);
    public static final UITexture IGNORE_DURABILITY_ICON = icon("ignore_duration", 16, 16);

    public static final UITexture HALF_HEART_ICON = icon("half_heart", 96, 16);
    public static final UITexture IGNORE_HALF_HEART_ICON = icon("ignore_half_heart", 112, 16);

    public static final UITexture BY_MOD_ID_ICON = icon("by_mod_id", 32, 16);
    public static final IDrawable BY_ITEM_ICON = new ItemDrawable(Items.apple);

    public static final IDrawable IN_OUT_ICON = icon("in_out", 0, 32);
    public static final IDrawable IN_ICON = icon("in", 16, 32);
    public static final IDrawable OUT_ICON = icon("out", 32, 32);

    public static final IDrawable ADD_ICON = icon("add", 96, 32);
    public static final IDrawable REMOVE_ICON = icon("remove", 112, 32);
    public static final IDrawable BRAIN_ICON = icon("brain", 128, 32);

    public static final IDrawable BACKPACK_ICON = icon("backpack", 64, 48);
    public static final IDrawable UNLOCK_BACKPACK_ICON = icon("unlock_backpack", 176, 32);
    public static final IDrawable LOCK_BACKPACK_ICON = icon("lock_backpack", 192, 32);
    public static final IDrawable UNLOCK_SEARCH_ICON = icon("unlock_search", 224, 32);
    public static final IDrawable LOCK_SEARCH_ICON = icon("lock_search", 208, 32);
    public static final IDrawable KEEP_TAB_ICON = icon("keep_tab", 80, 80);
    public static final IDrawable NOT_KEEP_TAB_ICON = icon("not_keep_tab", 80, 96);

    public static final IDrawable SHIFT_CLICK_INVENTORY_ICON = icon("shift_click_inv", 64, 96);
    public static final IDrawable SHIFT_CLICK_OPEN_TAB_ICON = icon("shift_click_tab", 80, 32);

    public static final IDrawable SAVE_TEMPLATE_ICON = icon("save_template", 240, 16);
    public static final IDrawable LOAD_TEMPLATE_ICON = icon("load_template", 240, 32);
    public static final IDrawable DELETE_TEMPLATE_ICON = icon("delete_template", 240, 48);
    public static final IDrawable EXPORT_TEMPLATE_ICON = icon("export_template", 240, 64);
    public static final IDrawable IMPORT_TEMPLATE_ICON = icon("import_template", 240, 80);

    public static final IDrawable ONE_IN_FOUR_SLOT_ICON = icon("one_in_four_slot", 0, 80);
    public static final IDrawable ALL_FOUR_SLOT_ICON = icon("all_in_four_slot", 16, 80);
    public static final IDrawable NO_SORT_ICON = icon("no_sort", 32, 80);
    public static final IDrawable NONE_FOUR_SLOT_ICON = icon("none_in_four_slot", 48, 80);

    public static final IDrawable SETTING_ICON = icon("setting", 16, 96);
    public static final IDrawable BACK_ICON = icon("back", 64, 80);

    public static final IDrawable EXP_MAGNET_ICON = icon("exp_magnet", 96, 48);
    public static final IDrawable IGNORE_EXP_MAGNET_ICON = icon("ignore_exp_magnet", 112, 48);
    public static final IDrawable ITEM_MAGNET_ICON = icon("exp_magnet", 128, 48);
    public static final IDrawable IGNORE_ITEM_MAGNET_ICON = icon("ignore_exp_magnet", 144, 48);

    public static final UITexture MATCH_ORE_DICT_ICON = icon("consider_ore_dict", 112, 96);
    public static final UITexture IGNORE_ORE_DICT_ICON = icon("ignore_ore_dict", 128, 96);

    public static final UITexture INTO_BACKPACK = icon("into_backpack", 32, 48);
    public static final UITexture INTO_INVENTORY = icon("small_m", 48, 48);
    public static final UITexture USED_BACKPACK = icon("into_backpack", 176, 80);
    public static final UITexture UNUSED_BACKPACK = icon("unused_backpack", 192, 80);

    public static final UITexture VOID_OVERFLOW = icon("void_overflow", 224, 16);
    public static final UITexture VOID_ANY = icon("void_any", 208, 16);
    public static final UITexture VOID_ALL = icon("void_all", 0, 48);
    public static final UITexture VOID_AUTOMATION = icon("void_automation", 16, 48);

    public static final IDrawable SWAP_WEAPON_ICON = icon("swap_weapon", 32, 64);
    public static final IDrawable NO_SWAP_WEAPON_ICON = icon("no_swap_weapon", 48, 64);
    public static final IDrawable SWAP_TOOL_ICON = icon("swap_tool", 64, 64);
    public static final IDrawable ONLY_TOOL_SWAP_TOOL_ICON = icon("only_tool_swap_tool", 80, 64);
    public static final IDrawable NO_SWAP_TOOL_ICON = icon("no_swap_tool", 96, 64);

    public static final IDrawable JUKEBOX_STOP_ICON = icon("jukebox_stop", 0, 64);
    public static final IDrawable JUKEBOX_PLAY_ICON = icon("jukebox_play", 16, 64);
    public static final IDrawable JUKEBOX_NEXT_ICON = icon("jukebox_next", 32, 96);
    public static final IDrawable JUKEBOX_PREV_ICON = icon("jukebox_prev", 48, 96);

    public static final IDrawable SHUFFLE_OFF_ICON = icon("shuffle_off", 112, 80);
    public static final IDrawable SHUFFLE_ON_ICON = icon("shuffle_on", 96, 80);
    public static final IDrawable LOOP_ALL_ICON = icon("loop_all", 128, 80);
    public static final IDrawable LOOP_SINGLE_ICON = icon("loop_single", 144, 80);
    public static final IDrawable LOOP_OFF_ICON = icon("loop_off", 160, 80);

    public static final UITexture ROTATED_RIGHT = icon("rotated_right", 0, 170);
    public static final UITexture ROTATED_LEFT = icon("rotated_left", 48, 170);
    public static final UITexture BALANCE = icon("balance", 32, 170);
    public static final UITexture SPREAD = icon("spread", 80, 170);
    public static final UITexture CLEAR = icon("clear", 16, 170);

    public static final UITexture SOLID_UP_ARROW_ICON = icon("solid_up_arrow", 0, 144, 12, 12);
    public static final UITexture SOLID_DOWN_ARROW_ICON = icon("solid_down_arrow", 12, 156, 12, 12);
    public static final UITexture DOT_DOWN_ARROW_ICON = icon("dot_down_arrow", 24, 156, 12, 12);
    public static final UITexture DOT_UP_ARROW_ICON = icon("dot_up_arrow", 36, 156, 12, 12);

    public static final UITexture TOGGLE_DISABLE_ICON = icon("disable", 0, 128, 4, 10);
    public static final UITexture TOGGLE_ENABLE_ICON = icon("enable", 4, 128, 4, 10);
    public static final UITexture SMALL_M_ICON = icon("small_m", 0, 156, 12, 12);
    public static final UITexture SMALL_A_ICON = icon("small_a", 24, 144, 12, 12);
    public static final UITexture SMALL_1_ICON = icon("small_1", 36, 144, 12, 12);
    public static final UITexture SMALL_O_ICON = icon("small_ore_dict", 64, 144, 12, 12);

    public static final UITexture STANDARD_BUTTON = UITexture.builder()
        .location(Reference.MOD_ID, "gui/gui_controls.png")
        .imageSize(256, 256)
        .xy(29, 0, 18, 18)
        .build();
    public static final UITexture STANDARD_BUTTON_HOVERED = UITexture.builder()
        .location(Reference.MOD_ID, "gui/gui_controls.png")
        .imageSize(256, 256)
        .xy(47, 0, 18, 18)
        .build();

    public static final UITexture BIG_SLOT_TEXTURE = UITexture.builder()
        .location(Reference.MOD_ID, "gui/gui_controls.png")
        .imageSize(256, 256)
        .xy(71, 216, 26, 26)
        .build();

    public static final UITexture SLEEPING_BAG = UITexture.builder()
        .location(Reference.MOD_ID, "textures/items/sleepingBag.png")
        .imageSize(16, 16)
        .xy(0, 0, 16, 16)
        .build();

    public static final UITexture ANVIL_TEXT_FIELD_ENABLED = UITexture.builder()
        .location(GUI_CONTROLS)
        .imageSize(256, 256)
        .xy(28, 99, 100, 16)
        .name("anvil_text_field_enabled")
        .build();

    public static final UITexture ANVIL_TEXT_FIELD_DISABLED = UITexture.builder()
        .location(GUI_CONTROLS)
        .imageSize(256, 256)
        .xy(28, 115, 100, 16)
        .name("anvil_text_field_disabled")
        .build();

    public static final UITexture ANVIL_PLUS_SIGN = UITexture.builder()
        .location(GUI_CONTROLS)
        .imageSize(256, 256)
        .xy(113, 203, 13, 13)
        .name("anvil_plus_sign")
        .build();

    public static final UITexture ANVIL_ARROW = UITexture.builder()
        .location(GUI_CONTROLS)
        .imageSize(256, 256)
        .xy(56, 221, 14, 15)
        .name("anvil_arrow")
        .build();

    public static final UITexture ANVIL_RED_CROSS = UITexture.builder()
        .location(GUI_CONTROLS)
        .imageSize(256, 256)
        .xy(113, 216, 15, 15)
        .name("anvil_red_cross")
        .build();

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

    // Tank upgrade GUI textures
    public static final UITexture TANK_BAR_TOP = UITexture.builder()
        .location(GUI_CONTROLS)
        .imageSize(256, 256)
        .xy(29, 30, 18, 18)
        .name("tank_bar_top")
        .build();

    public static final UITexture TANK_BAR_MIDDLE = UITexture.builder()
        .location(GUI_CONTROLS)
        .imageSize(256, 256)
        .xy(29, 48, 18, 18)
        .name("tank_bar_middle")
        .build();

    public static final UITexture TANK_BAR_BOTTOM = UITexture.builder()
        .location(GUI_CONTROLS)
        .imageSize(256, 256)
        .xy(29, 66, 18, 18)
        .name("tank_bar_bottom")
        .build();

    public static final UITexture TANK_OVERLAY = UITexture.builder()
        .location(GUI_CONTROLS)
        .imageSize(256, 256)
        .xy(47, 30, 16, 18)
        .name("tank_overlay")
        .build();

    public static final UITexture TANK_SLOT_ARROW = UITexture.builder()
        .location(GUI_CONTROLS)
        .imageSize(256, 256)
        .xy(97, 216, 15, 8)
        .name("tank_slot_arrow")
        .build();

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
