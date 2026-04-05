package ruiseki.okbackpack.client.audio;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundPoolEntry;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.common.item.jukebox.RecordDurationCache;
import ruiseki.okbackpack.common.network.PacketRecordDuration;

/**
 * Client-side helper that resolves record OGG files via MC's sound registry and resource manager.
 * This properly handles resource packs and modded records with non-standard asset paths.
 */
@SideOnly(Side.CLIENT)
public class RecordDurationClientHelper {

    public static InputStream openRecordOgg(ResourceLocation soundEvent) {
        try {
            SoundHandler handler = Minecraft.getMinecraft()
                .getSoundHandler();
            if (handler == null) return null;

            SoundEventAccessorComposite composite = handler.getSound(soundEvent);
            if (composite == null) return null;

            SoundPoolEntry entry = composite.func_148720_g();
            if (entry == null || entry == SoundHandler.missing_sound) return null;

            ResourceLocation oggLocation = entry.getSoundPoolEntryLocation();
            return Minecraft.getMinecraft()
                .getResourceManager()
                .getResource(oggLocation)
                .getInputStream();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Resolves the duration of a record on the client via MC's sound system,
     * then sends it to the server so it can track playback timing correctly.
     *
     * @param recordName the full sound event name (e.g. "records.cat")
     */
    public static void resolveAndSyncDuration(String recordName) {
        if (recordName == null || recordName.isEmpty()) return;

        try {
            var res = new ResourceLocation(recordName);
            try (InputStream is = openRecordOgg(res)) {
                if (is == null) return;
                int ticks = readOggDurationTicks(is);
                if (ticks <= 0) return;

                // Strip "records." prefix for cache key consistency
                String shortName = recordName.startsWith("records.") ? recordName.substring("records.".length())
                    : recordName;
                RecordDurationCache.putDuration(shortName, ticks);

                OKBackpack.instance.getPacketHandler()
                    .sendToServer(new PacketRecordDuration(shortName, ticks));
            }
        } catch (Exception ignored) {}
    }

    private static int readOggDurationTicks(InputStream is) throws Exception {
        var baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) != -1) {
            baos.write(buf, 0, n);
        }
        byte[] data = baos.toByteArray();

        int sampleRate = parseVorbisSampleRate(data);
        if (sampleRate <= 0) return -1;

        long granulePosition = findLastGranulePosition(data);
        if (granulePosition <= 0) return -1;

        return (int) ((granulePosition * 20L) / sampleRate);
    }

    private static int parseVorbisSampleRate(byte[] data) {
        int pageStart = findOggPageFrom(data, 0);
        if (pageStart < 0 || pageStart + 27 > data.length) return -1;

        int segmentCount = data[pageStart + 26] & 0xFF;
        if (pageStart + 27 + segmentCount > data.length) return -1;

        int dataStart = pageStart + 27 + segmentCount;

        if (dataStart + 16 > data.length) return -1;
        if (data[dataStart] != 1) return -1;
        if (data[dataStart + 1] != 'v' || data[dataStart + 2] != 'o'
            || data[dataStart + 3] != 'r'
            || data[dataStart + 4] != 'b'
            || data[dataStart + 5] != 'i'
            || data[dataStart + 6] != 's') return -1;

        return (data[dataStart + 12] & 0xFF) | ((data[dataStart + 13] & 0xFF) << 8)
            | ((data[dataStart + 14] & 0xFF) << 16)
            | ((data[dataStart + 15] & 0xFF) << 24);
    }

    private static long findLastGranulePosition(byte[] data) {
        for (int i = data.length - 4; i >= 0; i--) {
            if (data[i] == 'O' && data[i + 1] == 'g' && data[i + 2] == 'g' && data[i + 3] == 'S') {
                if (i + 14 <= data.length) {
                    long gp = (data[i + 6] & 0xFFL) | ((data[i + 7] & 0xFFL) << 8)
                        | ((data[i + 8] & 0xFFL) << 16)
                        | ((data[i + 9] & 0xFFL) << 24)
                        | ((data[i + 10] & 0xFFL) << 32)
                        | ((data[i + 11] & 0xFFL) << 40)
                        | ((data[i + 12] & 0xFFL) << 48)
                        | ((data[i + 13] & 0xFFL) << 56);
                    if (gp > 0) return gp;
                }
            }
        }
        return -1;
    }

    private static int findOggPageFrom(byte[] data, int from) {
        for (int i = from; i <= data.length - 4; i++) {
            if (data[i] == 'O' && data[i + 1] == 'g' && data[i + 2] == 'g' && data[i + 3] == 'S') {
                return i;
            }
        }
        return -1;
    }
}
