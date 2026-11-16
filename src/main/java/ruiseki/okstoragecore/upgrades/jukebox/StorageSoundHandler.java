package ruiseki.okstoragecore.upgrades.jukebox;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;

import org.joml.Vector3d;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ruiseki.okcore.audio.record.RecordData;
import ruiseki.okcore.client.audio.sound.EntityBoundSound;
import ruiseki.okcore.client.audio.sound.IEntitySound;
import ruiseki.okcore.client.audio.sound.SoundExtended;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.RecordHelpers;
import ruiseki.okcore.helper.VectorHelpers;

public class StorageSoundHandler {

    public static StorageSoundHandler INSTANCE = new StorageSoundHandler();

    private static final int SOUND_STOP_CHECK_INTERVAL = 10;

    private static final Map<UUID, ISound> storageSounds = new ConcurrentHashMap<>();
    private static long lastPlaybackChecked = 0;

    public StorageSoundHandler() {}

    public static void playStorageSound(UUID storageUuid, ISound sound) {
        stopStorageSound(storageUuid);
        storageSounds.put(storageUuid, sound);
        Minecraft.getMinecraft()
            .getSoundHandler()
            .playSound(sound);
    }

    public static void stopStorageSound(UUID storageUuid) {
        ISound sound = storageSounds.remove(storageUuid);
        if (sound != null) {
            Minecraft.getMinecraft()
                .getSoundHandler()
                .stopSound(sound);
        }
    }

    public static void stopAllStorageSounds() {
        for (UUID uuid : storageSounds.keySet()) {
            stopStorageSound(uuid);
        }
    }

    public static boolean isStorageSoundPlayingNear(BlockPos position, double radius) {
        double radiusSqr = radius * radius;
        return storageSounds.values()
            .stream()
            .anyMatch(sound -> {
                Vector3d soundPosition = sound instanceof IEntitySound storageSoundPosition
                    ? storageSoundPosition.getPosition()
                    : VectorHelpers.createVector3d(sound);
                double xDiff = soundPosition.x - position.x;
                double yDiff = soundPosition.y - position.y;
                double zDiff = soundPosition.z - position.z;
                return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff <= radiusSqr;
            });
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;
        if (!storageSounds.isEmpty() && lastPlaybackChecked < world.getTotalWorldTime() - SOUND_STOP_CHECK_INTERVAL) {
            lastPlaybackChecked = world.getTotalWorldTime();
            storageSounds.entrySet()
                .removeIf(
                    entry -> !mc.getSoundHandler()
                        .isSoundPlaying(entry.getValue()));
        }
    }

    public static void playStorageSound(RecordData soundEvent, UUID storageUuid, BlockPos pos) {
        playStorageSound(
            storageUuid,
            new SoundExtended(RecordHelpers.getRecordLocation(soundEvent), VectorHelpers.atCenterOf(pos)));
    }

    public static void playStorageSound(RecordData soundEvent, UUID storageUuid, int entityId, World world,
        EntityPlayer player) {
        float volume = 2;
        float pitch = 1;

        Entity entity = world.getEntityByID(entityId);
        if (entity == null) {
            stopStorageSound(storageUuid);
            return;
        }

        if (player != null && player.getEntityId() == entityId) {
            playStorageSound(
                storageUuid,
                new EntityBoundSound(RecordHelpers.getRecordLocation(soundEvent), volume, pitch, player));
        } else {
            playStorageSound(
                storageUuid,
                new EntityBoundSound(RecordHelpers.getRecordLocation(soundEvent), volume, pitch, entity));
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload evt) {
        storageSounds.clear();
        lastPlaybackChecked = 0;
    }
}
