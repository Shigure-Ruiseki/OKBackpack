package ruiseki.okbackpack.common.item.jukebox;

import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * Caches record durations in ticks.
 * On the server, starts with DEFAULT_DURATION and is updated by the client via PacketRecordDuration.
 * On the client, resolves real durations via RecordDurationClientHelper.
 */
public class RecordDurationCache {

    private static final Object2IntMap<String> cache = new Object2IntOpenHashMap<>();
    private static final int DEFAULT_DURATION_TICKS = 300 * 20;

    public static int getDurationTicks(ItemStack record) {
        if (record == null || !(record.getItem() instanceof ItemRecord itemRecord)) return 0;

        String recordName = itemRecord.recordName;
        if (recordName == null) return DEFAULT_DURATION_TICKS;

        if (cache.containsKey(recordName)) {
            return cache.getInt(recordName) + 100;
        }
        return DEFAULT_DURATION_TICKS;
    }

    /**
     * Stores a resolved duration for a record name.
     * Called from the client's OGG parser and from the server when receiving PacketRecordDuration.
     */
    public static void putDuration(String recordName, int durationTicks) {
        if (recordName == null || durationTicks <= 0) return;
        cache.put(recordName, durationTicks);
    }
}
