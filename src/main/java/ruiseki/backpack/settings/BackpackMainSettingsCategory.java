package ruiseki.backpack.settings;

import java.util.function.Consumer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.PlayerEvent;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.storagecore.settings.MainSetting;
import ruiseki.storagecore.settings.SettingsManager;
import ruiseki.storagecore.settings.main.MainSettingsCategory;

@EventBusSubscriber
public class BackpackMainSettingsCategory extends MainSettingsCategory<BackpackMainSettingsCategory> {

    public static final String SOPHISTICATED_BACKPACK_SETTINGS_PLAYER_TAG = "sophisticatedBackpackSettings";
    public static final MainSetting<Boolean> ANOTHER_PLAYER_CAN_OPEN = new MainSetting<>(
        "anotherPlayerCanOpen",
        NBTHelpers::getBoolean,
        NBTTagCompound::setBoolean,
        true);

    public static final String NAME = "backpackGlobal";

    static {
        SettingsManager.addSetting(ANOTHER_PLAYER_CAN_OPEN);
    }

    public BackpackMainSettingsCategory(NBTTagCompound categoryNbt, Consumer<NBTTagCompound> saveNbt) {
        super(categoryNbt, saveNbt, SOPHISTICATED_BACKPACK_SETTINGS_PLAYER_TAG);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        NBTTagCompound oldData = event.original.getEntityData();
        NBTTagCompound newData = event.entity.getEntityData();

        if (oldData.hasKey(SOPHISTICATED_BACKPACK_SETTINGS_PLAYER_TAG)) {
            // noinspection ConstantConditions
            newData.setTag(
                SOPHISTICATED_BACKPACK_SETTINGS_PLAYER_TAG,
                oldData.getTag(SOPHISTICATED_BACKPACK_SETTINGS_PLAYER_TAG));
        }
    }
}
