package ruiseki.okbackpack.common.item.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.NetworkRegistry;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IJukeboxUpgrade;
import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.item.wrapper.jukebox.JukeboxPlaybackDecision;
import ruiseki.okbackpack.common.item.wrapper.jukebox.JukeboxPlaybackPlanner;
import ruiseki.okbackpack.common.item.wrapper.jukebox.JukeboxPlaybackState;
import ruiseki.okbackpack.common.network.PacketJukeboxPlaybackState;
import ruiseki.okbackpack.common.network.PacketJukeboxPositionUpdate;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class AdvancedJukeboxUpgradeWrapper extends UpgradeWrapperBase implements IJukeboxUpgrade {

    private final BaseItemStackHandler recordHandler;
    private final Random random = new Random();

    public AdvancedJukeboxUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);
        this.recordHandler = new BaseItemStackHandler(getRecordSlotCount()) {

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack != null && stack.getItem() instanceof ItemRecord;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            protected void onContentsChanged(int slot) {
                NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
                tag.setTag(STORAGE_TAG, this.serializeNBT());
                onRecordSlotChanged(slot);
                storage.markDirty();
            }
        };

        NBTTagCompound handlerTag = ItemNBTHelpers.getCompound(upgrade, STORAGE_TAG, false);
        if (handlerTag != null) recordHandler.deserializeNBT(handlerTag);
    }

    @Override
    public int getRecordSlotCount() {
        return 16;
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.advanced_jukebox_settings";
    }

    @Override
    public BaseItemStackHandler getStorage() {
        return recordHandler;
    }

    @Override
    public boolean isPlaying() {
        return ItemNBTHelpers.getBoolean(upgrade, PLAYING_TAG, false);
    }

    @Override
    public void setPlaying(boolean playing) {
        ItemNBTHelpers.setBoolean(upgrade, PLAYING_TAG, playing);
    }

    @Override
    public int getCurrentSlotIndex() {
        return ItemNBTHelpers.getInt(upgrade, CURRENT_SLOT_INDEX_TAG, 0);
    }

    @Override
    public void setCurrentSlotIndex(int index) {
        ItemNBTHelpers.setInt(upgrade, CURRENT_SLOT_INDEX_TAG, index);
    }

    @Override
    public int getProgressTicks() {
        return ItemNBTHelpers.getInt(upgrade, PROGRESS_TICKS_TAG, 0);
    }

    @Override
    public void setProgressTicks(int ticks) {
        ItemNBTHelpers.setInt(upgrade, PROGRESS_TICKS_TAG, ticks);
    }

    public boolean isShuffleEnabled() {
        return ItemNBTHelpers.getBoolean(upgrade, SHUFFLE_TAG, false);
    }

    public void setShuffleEnabled(boolean enabled) {
        ItemNBTHelpers.setBoolean(upgrade, SHUFFLE_TAG, enabled);
    }

    public JukeboxLoopMode getLoopMode() {
        int ordinal = ItemNBTHelpers.getInt(upgrade, LOOP_MODE_TAG, JukeboxLoopMode.OFF.ordinal());
        JukeboxLoopMode[] modes = JukeboxLoopMode.values();
        if (ordinal < 0 || ordinal >= modes.length) return JukeboxLoopMode.OFF;
        return modes[ordinal];
    }

    public void setLoopMode(JukeboxLoopMode mode) {
        if (mode == null) mode = JukeboxLoopMode.OFF;
        ItemNBTHelpers.setInt(upgrade, LOOP_MODE_TAG, mode.ordinal());
    }

    @Override
    public void play() {
        if (!isEnabled()) return;
        List<Integer> playable = buildPlayableSlots();
        if (playable.isEmpty()) return;

        int slot = getCurrentSlotIndex();
        if (!playable.contains(slot)) {
            slot = playable.get(0);
        }

        ItemStack record = recordHandler.getStackInSlot(slot);
        if (record == null || !(record.getItem() instanceof ItemRecord)) return;

        setCurrentSlotIndex(slot);
        setPlaying(true);
        setProgressTicks(0);
        markDirty();
    }

    @Override
    public void stop() {
        setPlaying(false);
        setProgressTicks(0);
        ItemNBTHelpers.setBoolean(upgrade, PENDING_STOP_SYNC_TAG, true);
        markDirty();
    }

    public void previous() {
        if (!isEnabled() || !isPlaying()) return;
        List<Integer> playable = buildPlayableSlots();
        if (playable.isEmpty()) {
            stop();
            return;
        }

        JukeboxPlaybackState state = buildState();
        JukeboxPlaybackDecision decision;
        if (isShuffleEnabled()) {
            decision = pickRandomSlot(playable, state);
        } else {
            decision = JukeboxPlaybackPlanner.onPrevious(playable, state);
        }
        applyDecision(decision);
    }

    public void next() {
        if (!isEnabled() || !isPlaying()) return;
        List<Integer> playable = buildPlayableSlots();
        if (playable.isEmpty()) {
            stop();
            return;
        }

        JukeboxPlaybackState state = buildState();
        JukeboxPlaybackDecision decision;
        if (isShuffleEnabled()) {
            decision = pickRandomSlot(playable, state);
        } else {
            decision = JukeboxPlaybackPlanner.onNext(playable, state);
        }
        applyDecision(decision);
    }

    @Override
    public boolean isEnabled() {
        return ItemNBTHelpers.getBoolean(upgrade, ENABLED_TAG, true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        ItemNBTHelpers.setBoolean(upgrade, ENABLED_TAG, enabled);
        if (!enabled && isPlaying()) {
            stop();
        }
        markDirty();
    }

    @Override
    public void toggle() {
        setEnabled(!isEnabled());
    }

    protected void onRecordSlotChanged(int slot) {
        if (!isPlaying()) return;
        if (slot == getCurrentSlotIndex()) {
            ItemStack current = recordHandler.getStackInSlot(slot);
            if (current == null || !(current.getItem() instanceof ItemRecord)) {
                stop();
            }
        }
    }

    @Override
    public boolean tick(EntityPlayer player) {
        if (player.worldObj.isRemote) return false;
        boolean wasPlaying = isPlaying();
        int oldSlotIndex = getCurrentSlotIndex();
        boolean dirty = tickPlayback();
        sendPlaybackStateToPlayer(player, wasPlaying, oldSlotIndex);
        return dirty;
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        if (world.isRemote) return false;
        boolean wasPlaying = isPlaying();
        int oldSlotIndex = getCurrentSlotIndex();
        boolean dirty = tickPlayback();
        sendPlaybackStateToNearby(world, pos, wasPlaying, oldSlotIndex);
        return dirty;
    }

    protected boolean tickPlayback() {
        if (!isEnabled()) {
            if (isPlaying()) {
                stop();
                return true;
            }
            return false;
        }
        if (!isPlaying()) return false;

        int slot = getCurrentSlotIndex();
        ItemStack record = recordHandler.getStackInSlot(slot);
        if (record == null || !(record.getItem() instanceof ItemRecord)) {
            stop();
            return true;
        }

        int progress = getProgressTicks();
        int duration = JukeboxUpgradeWrapper.getRecordDuration(record);

        if (progress >= duration) {
            onTrackFinished();
            return true;
        }

        setProgressTicks(progress + 1);
        return false;
    }

    protected void onTrackFinished() {
        List<Integer> playable = buildPlayableSlots();
        JukeboxPlaybackState state = buildState();

        JukeboxPlaybackDecision decision;
        if (isShuffleEnabled() && state.getLoopMode() != JukeboxLoopMode.SINGLE) {
            if (state.getLoopMode() == JukeboxLoopMode.OFF) {
                decision = new JukeboxPlaybackDecision(false, state.getCurrentSlotIndex(), 0);
            } else {
                decision = pickRandomSlot(playable, state);
            }
        } else {
            decision = JukeboxPlaybackPlanner.onTrackFinished(playable, state);
        }
        applyDecision(decision);
    }

    protected List<Integer> buildPlayableSlots() {
        var avail = new ArrayList<Boolean>();
        for (int i = 0; i < recordHandler.getSlots(); i++) {
            ItemStack s = recordHandler.getStackInSlot(i);
            avail.add(s != null && s.getItem() instanceof ItemRecord);
        }
        return JukeboxPlaybackPlanner.buildPlayableSlots(avail);
    }

    private JukeboxPlaybackState buildState() {
        return new JukeboxPlaybackState(
            isPlaying(),
            getCurrentSlotIndex(),
            getProgressTicks(),
            isShuffleEnabled(),
            getLoopMode());
    }

    private void applyDecision(JukeboxPlaybackDecision decision) {
        setPlaying(decision.isPlaying());
        setCurrentSlotIndex(decision.getCurrentSlotIndex());
        setProgressTicks(decision.getProgressTicks());
        markDirty();
    }

    private JukeboxPlaybackDecision pickRandomSlot(List<Integer> playable, JukeboxPlaybackState state) {
        if (playable.isEmpty()) {
            return new JukeboxPlaybackDecision(false, state.getCurrentSlotIndex(), 0);
        }
        if (playable.size() == 1) {
            return new JukeboxPlaybackDecision(true, playable.get(0), 0);
        }
        List<Integer> candidates = new ArrayList<>(playable);
        candidates.remove(Integer.valueOf(state.getCurrentSlotIndex()));
        if (candidates.isEmpty()) {
            return new JukeboxPlaybackDecision(true, playable.get(0), 0);
        }
        int chosen = candidates.get(random.nextInt(candidates.size()));
        return new JukeboxPlaybackDecision(true, chosen, 0);
    }

    private int findUpgradeSlotIndex() {
        var handler = storage.getUpgradeHandler();
        for (int i = 0; i < handler.getSlots(); i++) {
            if (handler.getStackInSlot(i) == upgrade) return i;
        }
        return -1;
    }

    private String getBackpackUuid() {
        if (storage instanceof BackpackWrapper bw) return bw.uuid;
        return "";
    }

    private String getCurrentRecordName() {
        int slot = getCurrentSlotIndex();
        ItemStack record = recordHandler.getStackInSlot(slot);
        if (record != null && record.getItem() instanceof ItemRecord itemRecord) {
            return "records." + itemRecord.recordName;
        }
        return "";
    }

    private void sendPlaybackStateToPlayer(EntityPlayer player, boolean wasPlaying, int oldSlotIndex) {
        if (!(player instanceof EntityPlayerMP playerMP)) return;

        String uuid = getBackpackUuid();
        if (uuid.isEmpty()) return;
        int upgradeSlot = findUpgradeSlotIndex();
        if (upgradeSlot < 0) return;

        boolean nowPlaying = isPlaying();
        int progress = getProgressTicks();
        boolean pendingStop = ItemNBTHelpers.getBoolean(upgrade, PENDING_STOP_SYNC_TAG, false);
        float x = (float) player.posX;
        float y = (float) player.posY;
        float z = (float) player.posZ;
        int carrierEntityId = player.getEntityId();
        var targetPoint = new NetworkRegistry.TargetPoint(player.worldObj.provider.dimensionId, x, y, z, 64);

        if (nowPlaying) {
            boolean newTrack = progress == 1 || (wasPlaying && oldSlotIndex != getCurrentSlotIndex());
            if (newTrack) {
                var packet = new PacketJukeboxPlaybackState(
                    uuid,
                    upgradeSlot,
                    true,
                    getCurrentSlotIndex(),
                    0,
                    x,
                    y,
                    z,
                    getCurrentRecordName(),
                    carrierEntityId);
                OKBackpack.instance.getPacketHandler()
                    .sendToAllAround(packet, targetPoint);
            } else if (progress % 10 == 0) {
                var packet = new PacketJukeboxPositionUpdate(uuid, upgradeSlot, x, y, z);
                OKBackpack.instance.getPacketHandler()
                    .sendToAllAround(packet, targetPoint);
            }
        } else if (wasPlaying || pendingStop) {
            var packet = new PacketJukeboxPlaybackState(
                uuid,
                upgradeSlot,
                false,
                getCurrentSlotIndex(),
                0,
                x,
                y,
                z,
                "",
                carrierEntityId);
            OKBackpack.instance.getPacketHandler()
                .sendToAllAround(packet, targetPoint);
            ItemNBTHelpers.setBoolean(upgrade, PENDING_STOP_SYNC_TAG, false);
        }
    }

    private void sendPlaybackStateToNearby(World world, BlockPos pos, boolean wasPlaying, int oldSlotIndex) {
        String uuid = getBackpackUuid();
        if (uuid.isEmpty()) return;
        int upgradeSlot = findUpgradeSlotIndex();
        if (upgradeSlot < 0) return;

        boolean nowPlaying = isPlaying();
        int progress = getProgressTicks();
        boolean pendingStop = ItemNBTHelpers.getBoolean(upgrade, PENDING_STOP_SYNC_TAG, false);
        float x = pos.x + 0.5f;
        float y = pos.y + 0.5f;
        float z = pos.z + 0.5f;

        var targetPoint = new NetworkRegistry.TargetPoint(world.provider.dimensionId, x, y, z, 64);

        if (nowPlaying) {
            boolean newTrack = progress == 1 || (wasPlaying && oldSlotIndex != getCurrentSlotIndex());
            if (newTrack) {
                var packet = new PacketJukeboxPlaybackState(
                    uuid,
                    upgradeSlot,
                    true,
                    getCurrentSlotIndex(),
                    0,
                    x,
                    y,
                    z,
                    getCurrentRecordName(),
                    -1);
                OKBackpack.instance.getPacketHandler()
                    .sendToAllAround(packet, targetPoint);
            } else if (progress % 10 == 0) {
                var packet = new PacketJukeboxPositionUpdate(uuid, upgradeSlot, x, y, z);
                OKBackpack.instance.getPacketHandler()
                    .sendToAllAround(packet, targetPoint);
            }
        } else if (wasPlaying || pendingStop) {
            var packet = new PacketJukeboxPlaybackState(
                uuid,
                upgradeSlot,
                false,
                getCurrentSlotIndex(),
                0,
                x,
                y,
                z,
                "",
                -1);
            OKBackpack.instance.getPacketHandler()
                .sendToAllAround(packet, targetPoint);
            ItemNBTHelpers.setBoolean(upgrade, PENDING_STOP_SYNC_TAG, false);
        }
    }
}
