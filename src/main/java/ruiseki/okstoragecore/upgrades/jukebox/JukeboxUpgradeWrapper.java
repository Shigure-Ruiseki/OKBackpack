package ruiseki.okstoragecore.upgrades.jukebox;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.helper.VectorHelpers;
import ruiseki.okcore.item.handler.IItemHandlerModifiable;
import ruiseki.okcore.item.handler.ItemStackHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.IStorageUpgrade;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class JukeboxUpgradeWrapper extends UpgradeWrapperBase<JukeboxUpgradeWrapper, ItemJukeboxUpgrade>
    implements ITickableUpgrade, IStorageUpgrade {

    private static final int KEEP_ALIVE_SEND_INTERVAL = 5;
    private final ItemStackHandler discInventory;
    private int slotCount;
    private long lastKeepAliveSendTime = 0;
    private boolean isPlaying;

    private final LinkedList<Integer> playlist = new LinkedList<>();
    private final LinkedList<Integer> history = new LinkedList<>();

    private final Set<Integer> discsRemoved = new HashSet<>();
    private final Set<Integer> discsAdded = new HashSet<>();

    @Nullable
    private EntityPlayer entityPlaying = null;
    @Nullable
    private World levelPlaying = null;
    @Nullable
    private BlockPos posPlaying = null;

    private final Runnable onFinishedCallback = this::onDiscFinished;

    protected JukeboxUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        discInventory = new ItemStackHandler(upgradeItem.getNumberOfSlots()) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                NBTHelpers.setCompoundTag(upgrade, "discInventory", this.serializeNBT());
                save();
                if (ItemHelpers.isEmpty(getStackInSlot(slot))) {
                    discsRemoved.add(slot);
                    discsAdded.remove(slot);
                } else {
                    if (!playlist.contains(slot)) {
                        discsAdded.add(slot);
                        discsRemoved.remove(slot);
                    }
                }
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return ItemHelpers.isEmpty(stack) || DiscHandlerRegistry.isSupported(stack);
            }

            @Override
            public void setSize(int size) {
                if (size < upgradeItem.getNumberOfSlots()) {
                    size = upgradeItem.getNumberOfSlots();
                }
                super.setSize(size);
            }
        };
        NBTHelpers.getCompoundTag(upgrade, "discInventory")
            .ifPresent(this.discInventory::deserializeNBT);
        isPlaying = NBTHelpers.getBoolean(upgrade, "isPlaying")
            .orElse(false);
        this.slotCount = upgradeItem.getNumberOfSlots();
    }

    public int getSlotCount() {
        return slotCount;
    }

    public boolean isShuffleEnabled() {
        return NBTHelpers.getBoolean(upgrade, "shuffle")
            .orElse(false);
    }

    public void setShuffleEnabled(boolean shuffleEnabled) {
        NBTHelpers.setBoolean(upgrade, "shuffle", shuffleEnabled);
        save();

        initPlaylist(true);
    }

    public RepeatMode getRepeatMode() {
        return NBTHelpers.getEnumConstant(upgrade, "repeatMode", RepeatMode::fromName)
            .orElse(RepeatMode.NO);
    }

    public void setRepeatMode(RepeatMode repeatMode) {
        NBTHelpers.setEnumConstant(upgrade, "repeatMode", repeatMode);
        save();
    }

    public ItemStack getDisc() {
        return getDiscSlotActive() > -1 ? discInventory.getStackInSlot(getDiscSlotActive()) : ItemHelpers.EMPTY;
    }

    public int getDiscSlotActive() {
        return NBTHelpers.getInteger(upgrade, "discSlotActive")
            .orElse(-1);
    }

    public void setDiscSlotActive(int discSlotActive) {
        NBTHelpers.setInteger(upgrade, "discSlotActive", discSlotActive);
        save();
    }

    public void play(World level, BlockPos pos) {
        if (isPlaying) {
            return;
        }
        if (level instanceof WorldServer serverLevel && useProvidedPlaybackLocation(serverLevel)) {
            return;
        }

        levelPlaying = level;
        posPlaying = pos;
        playNext();
    }

    public void play(EntityPlayer entity) {
        if (isPlaying) {
            return;
        }
        if (entity.worldObj instanceof WorldServer serverLevel && useProvidedPlaybackLocation(serverLevel)) {
            return;
        }
        entityPlaying = entity;
        playNext();
    }

    private boolean useProvidedPlaybackLocation(WorldServer initiatingLevel) {
        if (!(storageWrapper instanceof IJukeboxPlaybackLocationProvider playbackLocationProvider)) {
            return false;
        }
        return playbackLocationProvider.getJukeboxPlaybackLocation(initiatingLevel)
            .map(playbackLocation -> {
                levelPlaying = playbackLocation.world();
                posPlaying = playbackLocation.blockPos();
                entityPlaying = playbackLocation.entity();
                playNext();
                return true;
            })
            .orElse(false);
    }

    private void playDisc() {
        World level = entityPlaying != null ? entityPlaying.worldObj : levelPlaying;
        if (!(level instanceof WorldServer serverLevel) || (posPlaying == null && entityPlaying == null)) return;
        ItemStack disc = getDisc();
        if (ItemHelpers.isEmpty(disc)) return;
        storageWrapper.getContentsUuid()
            .ifPresent(
                storageUuid -> DiscHandlerRegistry.findHandler(disc)
                    .ifPresent(handler -> {
                        if (entityPlaying != null) {
                            handler.playDisc(
                                serverLevel,
                                entityPlaying,
                                VectorHelpers.createVector3d(entityPlaying),
                                storageUuid,
                                disc,
                                entityPlaying.getEntityId(),
                                onFinishedCallback);
                        } else {
                            handler.playDisc(
                                serverLevel,
                                entityPlaying,
                                posPlaying,
                                storageUuid,
                                disc,
                                onFinishedCallback);
                        }
                        handler.getMusicLengthInTicks(disc, level, entityPlaying)
                            .ifPresent(
                                lengthInTicks -> NBTHelpers.setLong(
                                    upgrade,
                                    "finishTime",
                                    Math.toIntExact(level.getTotalWorldTime() + lengthInTicks)));
                    }));
        setIsPlaying(true);
    }

    private void onDiscFinished() {
        if (getRepeatMode() == RepeatMode.ONE) {
            playDisc();
        } else if (getRepeatMode() == RepeatMode.ALL) {
            playNext();
        } else {
            playNext(false);
        }
    }

    private void setIsPlaying(boolean playing) {
        isPlaying = playing;
        NBTHelpers.setBoolean(upgrade, "isPlaying", playing);
        if (isPlaying) {
            // storageWrapper.getRenderInfo().setUpgradeRenderData(JukeboxUpgradeRenderData.TYPE, new
            // JukeboxUpgradeRenderData(true));
        } else {
            // removeRenderData();
            setDiscSlotActive(-1);
        }
        save();
    }

    // private void removeRenderData() {
    // storageWrapper.getRenderInfo().removeUpgradeRenderData(JukeboxUpgradeRenderData.TYPE);
    // }

    public void stop(EntityLivingBase entity) {
        if (!(entity.worldObj instanceof WorldServer)) {
            return;
        }
        World soundLevel = (entityPlaying != null ? entityPlaying.worldObj
            : levelPlaying != null ? levelPlaying : entity.worldObj);
        if (!(soundLevel instanceof WorldServer)) return;
        Vector3d soundPosition = entityPlaying != null ? VectorHelpers.createVector3d(entityPlaying)
            : posPlaying != null ? VectorHelpers.atCenterOf(posPlaying) : VectorHelpers.createVector3d(entity);
        storageWrapper.getContentsUuid()
            .ifPresent(
                storageUuid -> ServerStorageSoundHandler
                    .stopPlayingDisc((WorldServer) soundLevel, soundPosition, storageUuid));
        entityPlaying = null;
        levelPlaying = null;
        posPlaying = null;
        setIsPlaying(false);
        NBTHelpers.removeTag(upgrade, "finishTime");
        setDiscSlotActive(-1);
        playlist.clear();
        history.clear();
    }

    @Override
    public IItemHandlerModifiable getStorage() {
        return this.discInventory;
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (!world.isRemote) {
            if (!discsRemoved.isEmpty()) {
                discsRemoved.forEach(index -> {
                    playlist.remove(index);
                    history.remove(index);
                });
                discsRemoved.clear();
            }
            if (!discsAdded.isEmpty()) {
                playlist.addAll(discsAdded);
                discsAdded.clear();
            }
        }

        if (isPlaying && lastKeepAliveSendTime < world.getTotalWorldTime() - KEEP_ALIVE_SEND_INTERVAL) {
            Vector3d soundPosition = entityPlaying != null ? VectorHelpers.createVector3d(entityPlaying)
                : posPlaying != null ? VectorHelpers.atCenterOf(posPlaying)
                    : entity != null ? VectorHelpers.createVector3d(entity) : VectorHelpers.atCenterOf(pos);
            storageWrapper.getContentsUuid()
                .ifPresent(
                    storageUuid -> ServerStorageSoundHandler
                        .updateKeepAlive(storageUuid, world, soundPosition, () -> setIsPlaying(false)));
            lastKeepAliveSendTime = world.getTotalWorldTime();
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    @Override
    public void onBeforeRemoved() {
        // removeRenderData();
    }

    public void next() {
        if (!isPlaying) {
            return;
        }
        playNext();
    }

    public void playNext() {
        playNext(true);
    }

    public void playNext(boolean startOverIfAtTheEnd) {
        if (playlist.isEmpty() && startOverIfAtTheEnd) {
            initPlaylist(false);
        }
        if (playlist.isEmpty()) {
            return;
        }
        if (getDiscSlotActive() != -1) {
            history.add(getDiscSlotActive());
            if (history.size() > discInventory.getSlots()) {
                history.poll();
            }
        }
        Integer discIndex = playlist.poll();
        if (discIndex == null) {
            return;
        }
        setDiscSlotActive(discIndex);

        playDisc();
    }

    private void initPlaylist(boolean excludeActive) {
        playlist.clear();
        for (int i = 0; i < discInventory.getSlots(); i++) {
            if (!ItemHelpers.isEmpty(discInventory.getStackInSlot(i))
                && (!excludeActive || !isPlaying || i != getDiscSlotActive())) {
                playlist.add(i);
            }
        }
        if (isShuffleEnabled()) {
            Collections.shuffle(playlist);
        }
    }

    public void previous() {
        if (!isPlaying) {
            return;
        }
        playPrevious();
    }

    public void playPrevious() {
        if (history.isEmpty()) {
            return;
        }
        playlist.addFirst(getDiscSlotActive());
        Integer discIndex = history.pollLast();
        if (discIndex == null) {
            return;
        }
        setDiscSlotActive(discIndex);
        playDisc();
    }

    public long getDiscFinishTime() {
        return NBTHelpers.getLong(upgrade, "finishTime")
            .orElse(0L);
    }
}
