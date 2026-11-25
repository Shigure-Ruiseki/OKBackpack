package ruiseki.okbackpack.client.audio;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemRecord;
import net.minecraft.util.ResourceLocation;

import com.github.bsideup.jabel.Desugar;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BackpackJukeboxSoundManager {

    private static final BackpackJukeboxSoundManager INSTANCE = new BackpackJukeboxSoundManager();

    public static BackpackJukeboxSoundManager getInstance() {
        return INSTANCE;
    }

    private final Map<SoundKey, MovableRecordSound> activeSounds = new HashMap<>();
    private boolean wasPaused = false;

    public void playRecord(String backpackUuid, int upgradeSlot, String recordName, float x, float y, float z,
        int carrierEntityId) {
        if (recordName == null || recordName.isEmpty()) return;

        SoundKey key = new SoundKey(backpackUuid, upgradeSlot);
        MovableRecordSound existing = activeSounds.remove(key);
        if (existing != null) {
            existing.stopSound();
        }

        ItemRecord itemRecord = ItemRecord.getRecord(recordName);
        ResourceLocation resource;
        if (itemRecord != null) {
            resource = itemRecord.getRecordResource(recordName);
        } else {
            resource = new ResourceLocation(recordName);
        }
        if (resource == null) return;

        boolean local = carrierEntityId >= 0 && Minecraft.getMinecraft().thePlayer != null
            && Minecraft.getMinecraft().thePlayer.getEntityId() == carrierEntityId;

        MovableRecordSound sound = new MovableRecordSound(resource, x, y, z, local);
        Minecraft.getMinecraft()
            .getSoundHandler()
            .playSound(sound);
        activeSounds.put(key, sound);
    }

    public void stop(String backpackUuid, int upgradeSlot) {
        SoundKey key = new SoundKey(backpackUuid, upgradeSlot);
        MovableRecordSound sound = activeSounds.remove(key);
        if (sound != null) {
            sound.stopSound();
        }
    }

    public void updatePosition(String backpackUuid, int upgradeSlot, float x, float y, float z) {
        SoundKey key = new SoundKey(backpackUuid, upgradeSlot);
        MovableRecordSound sound = activeSounds.get(key);
        if (sound != null) {
            sound.updatePosition(x, y, z);
        }
    }

    public void stopAll() {
        for (MovableRecordSound sound : activeSounds.values()) {
            sound.stopSound();
        }
        activeSounds.clear();
    }

    public void tick() {
        activeSounds.entrySet()
            .removeIf(
                entry -> entry.getValue()
                    .isDonePlaying());
    }

    private void repairSoundsAfterPause() {
        var handler = Minecraft.getMinecraft()
            .getSoundHandler();
        for (var entry : activeSounds.entrySet()) {
            MovableRecordSound old = entry.getValue();
            if (!old.isDonePlaying() && !handler.isSoundPlaying(old)) {
                var fresh = new MovableRecordSound(
                    old.getPositionedSoundLocation(),
                    old.getXPosF(),
                    old.getYPosF(),
                    old.getZPosF(),
                    old.isLocal());
                handler.playSound(fresh);
                entry.setValue(fresh);
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            boolean isPaused = Minecraft.getMinecraft()
                .isGamePaused();
            if (wasPaused && !isPaused) {
                repairSoundsAfterPause();
            }
            wasPaused = isPaused;
            if (!isPaused) {
                tick();
            }
        }
    }

    @Desugar
    private record SoundKey(String backpackUuid, int upgradeSlot) {}
}
