package ruiseki.okstoragecore.upgrades.jukebox;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import org.joml.Vector3d;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.RecordHelpers;
import ruiseki.okcore.helper.VectorHelpers;
import ruiseki.okstoragecore.OKStorageCore;
import ruiseki.okstoragecore.network.packet.PacketPlayDisc;
import ruiseki.okstoragecore.network.packet.PacketStopDiscPlayback;

public class ServerStorageSoundHandler {

    public static ServerStorageSoundHandler INSTANCE = new ServerStorageSoundHandler();

    private static final int KEEP_ALIVE_CHECK_INTERVAL = 10;
    private static final Map<Integer, Long> lastWorldCheck = new HashMap<>();
    private static final Map<Integer, Map<UUID, SoundInfo>> worldStorageSoundInfos = new HashMap<>();

    public ServerStorageSoundHandler() {}

    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.world instanceof WorldServer serverLevel)) {
            return;
        }
        int dim = serverLevel.provider.dimensionId;

        if (!worldStorageSoundInfos.containsKey(dim)) {
            return;
        }

        removeFinished(serverLevel, dim);

        if (lastWorldCheck.computeIfAbsent(dim, key -> serverLevel.getTotalWorldTime())
            > serverLevel.getTotalWorldTime() - KEEP_ALIVE_CHECK_INTERVAL || !worldStorageSoundInfos.containsKey(dim)) {
            return;
        }
        lastWorldCheck.put(dim, serverLevel.getTotalWorldTime());

        worldStorageSoundInfos.get(dim)
            .entrySet()
            .removeIf(entry -> {
                if (entry.getValue()
                    .getLastKeepAliveTime() < serverLevel.getTotalWorldTime() - KEEP_ALIVE_CHECK_INTERVAL) {
                    sendStopMessage(
                        serverLevel,
                        entry.getValue()
                            .getLastPosition(),
                        entry.getKey());
                    return true;
                }
                return false;
            });
    }

    private void removeFinished(WorldServer serverLevel, int dim) {
        List<UUID> storageIdsToRemove = new ArrayList<>();
        worldStorageSoundInfos.get(dim)
            .forEach((storageId, soundInfo) -> {
                if (soundInfo.getFinishTime() <= serverLevel.getTotalWorldTime()) {
                    storageIdsToRemove.add(storageId);
                }
            });
        storageIdsToRemove.forEach(storageId -> removeSoundInfo(serverLevel, storageId, true));
    }

    public static void updateKeepAlive(UUID storageUuid, World world, Vector3d position, Runnable onNoLongerRunning) {
        int dim = world.provider.dimensionId;
        if (!worldStorageSoundInfos.containsKey(dim) || !worldStorageSoundInfos.get(dim)
            .containsKey(storageUuid)) {
            onNoLongerRunning.run();
            return;
        }
        if (worldStorageSoundInfos.get(dim)
            .containsKey(storageUuid)) {
            worldStorageSoundInfos.get(dim)
                .get(storageUuid)
                .update(world.getTotalWorldTime(), position);
        }
    }

    public static boolean isStorageSoundPlayingNear(World level, Vector3d position, double radius) {
        Map<UUID, SoundInfo> soundInfos = worldStorageSoundInfos.get(level.provider.dimensionId);
        if (soundInfos == null) {
            return false;
        }

        double radiusSqr = radius * radius;
        return soundInfos.values()
            .stream()
            .anyMatch(
                soundInfo -> soundInfo.getLastPosition()
                    .distance(position) <= radiusSqr);
    }

    private static class SoundInfo {

        private final WeakReference<Runnable> onFinishedHandler;
        private long lastKeepAliveTime;
        private Vector3d lastPosition;
        private final long finishTime;

        private SoundInfo(Runnable onFinishedHandler, long lastKeepAliveTime, Vector3d lastPosition, long finishTime) {
            this.onFinishedHandler = new WeakReference<>(onFinishedHandler);
            this.lastKeepAliveTime = lastKeepAliveTime;
            this.lastPosition = lastPosition;
            this.finishTime = finishTime;
        }

        public long getLastKeepAliveTime() {
            return lastKeepAliveTime;
        }

        public Vector3d getLastPosition() {
            return lastPosition;
        }

        public long getFinishTime() {
            return finishTime;
        }

        public void update(long gameTime, Vector3d position) {
            lastKeepAliveTime = gameTime;
            lastPosition = position;
        }

        public void runOnFinished() {
            Runnable handler = onFinishedHandler.get();
            if (handler != null) {
                handler.run();
            }
        }
    }

    public static void startPlayingDisc(WorldServer serverLevel, EntityPlayer player, BlockPos position,
        UUID storageUuid, Item item, Runnable onFinishedHandler) {
        Vector3d pos = VectorHelpers.atCenterOf(position);
        OKStorageCore._instance.getPacketHandler()
            .sendToAllAround(
                new PacketPlayDisc(storageUuid, Item.getIdFromItem(item), position),
                serverLevel,
                pos,
                128);
        putSoundInfo(
            serverLevel,
            storageUuid,
            onFinishedHandler,
            pos,
            serverLevel.getTotalWorldTime()
                + (item instanceof ItemRecord recordItem ? RecordHelpers.getDurationTicks(player, recordItem) : 0));
    }

    public static void startPlayingDisc(WorldServer serverLevel, EntityPlayer player, Vector3d position,
        UUID storageUuid, int entityId, Item item, Runnable onFinishedHandler) {
        OKStorageCore._instance.getPacketHandler()
            .sendToAllAround(
                new PacketPlayDisc(storageUuid, Item.getIdFromItem(item), entityId),
                serverLevel,
                position,
                128);
        putSoundInfo(
            serverLevel,
            storageUuid,
            onFinishedHandler,
            position,
            serverLevel.getTotalWorldTime()
                + (item instanceof ItemRecord recordItem ? RecordHelpers.getDurationTicks(player, recordItem) : 0));
    }

    public static void putSoundInfo(WorldServer serverLevel, UUID storageUuid, Runnable onFinishedHandler, Vector3d pos,
        long finishTime) {
        worldStorageSoundInfos.computeIfAbsent(serverLevel.provider.dimensionId, dim -> new HashMap<>())
            .put(storageUuid, new SoundInfo(onFinishedHandler, serverLevel.getTotalWorldTime(), pos, finishTime));
    }

    public static void stopPlayingDisc(WorldServer serverWorld, Vector3d position, UUID storageUuid) {
        removeSoundInfo(serverWorld, storageUuid, false);
        sendStopMessage(serverWorld, position, storageUuid);
    }

    private static void removeSoundInfo(WorldServer serverWorld, UUID storageUuid, boolean finished) {
        int dim = serverWorld.provider.dimensionId;
        if (worldStorageSoundInfos.containsKey(dim) && worldStorageSoundInfos.get(dim)
            .containsKey(storageUuid)) {
            SoundInfo soundInfo = worldStorageSoundInfos.get(dim)
                .remove(storageUuid);
            if (finished) {
                soundInfo.runOnFinished();
            }
        }
    }

    private static void sendStopMessage(WorldServer serverWorld, Vector3d position, UUID storageUuid) {
        OKStorageCore._instance.getPacketHandler()
            .sendToAllAround(new PacketStopDiscPlayback(storageUuid), serverWorld, position, 128);
    }
}
