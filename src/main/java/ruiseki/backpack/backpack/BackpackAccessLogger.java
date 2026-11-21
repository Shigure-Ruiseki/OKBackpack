package ruiseki.backpack.backpack;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

import ruiseki.okcore.helper.Helpers;

public class BackpackAccessLogger {

    private static final int REFRESH_INTERVAL_SECONDS = 30;

    private BackpackAccessLogger() {}

    private static long lastCacheRefresh = 0;
    private static final Map<String, Set<AccessLogRecord>> playerLogCache = new HashMap<>();

    public static void logPlayerAccess(EntityPlayer player, Item backpackItem, UUID backpackUuid, String backpackName,
        int clothColor, int trimColor, int columnsTaken) {
        if (player.worldObj.isRemote) {
            return;
        }
        // noinspection ConstantConditions - at this point the registry name of item exists for sure otherwise the
        // player wouldn't be able to open the backpack
        BackpackStorage.get()
            .putAccessLog(
                new AccessLogRecord(
                    Helpers.getLocation(backpackItem),
                    backpackUuid,
                    player.getDisplayName(),
                    backpackName,
                    clothColor,
                    trimColor,
                    Helpers.getEpochMillis(),
                    columnsTaken));
    }

    public static Set<String> getPlayerNames() {
        initPlayerBackpackCache();
        return playerLogCache.keySet();
    }

    public static Collection<AccessLogRecord> getBackpackLogsForPlayer(String playerName) {
        initPlayerBackpackCache();
        return playerLogCache.getOrDefault(playerName, new HashSet<>());
    }

    private static void initPlayerBackpackCache() {
        if (lastCacheRefresh + (REFRESH_INTERVAL_SECONDS * 1000) >= Helpers.getEpochMillis()) {
            return;
        }
        lastCacheRefresh = Helpers.getEpochMillis();

        playerLogCache.clear();
        BackpackStorage.get()
            .getAccessLogs()
            .values()
            .forEach(
                alr -> playerLogCache.computeIfAbsent(alr.getPlayerName(), name -> new HashSet<>())
                    .add(alr));
    }

    public static Collection<AccessLogRecord> getAllBackpackLogs() {
        return BackpackStorage.get()
            .getAccessLogs()
            .values();
    }

    public static Set<UUID> getBackpackUuids() {
        return BackpackStorage.get()
            .getAccessLogs()
            .keySet();
    }

    public static Optional<AccessLogRecord> getBackpackLog(UUID backpackUuid) {
        return Optional.ofNullable(
            BackpackStorage.get()
                .getAccessLogs()
                .get(backpackUuid));
    }
}
