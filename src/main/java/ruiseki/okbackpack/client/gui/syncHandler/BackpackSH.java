package ruiseki.okbackpack.client.gui.syncHandler;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.item.PlayerMainInvWrapper;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import ruiseki.okbackpack.common.SortType;
import ruiseki.okbackpack.common.block.BackpackPanel;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockSleepingBag;
import ruiseki.okbackpack.common.block.TEBackpack;
import ruiseki.okbackpack.common.entity.properties.BackpackProperty;
import ruiseki.okbackpack.common.helpers.BackpackInventoryHelpers;

public class BackpackSH extends SyncHandler {

    public static final int UPDATE_SET_SORT_TYPE = 0;
    public static final int UPDATE_SORT_INV = 1;
    public static final int UPDATE_TRANSFER_TO_BACKPACK_INV = 2;
    public static final int UPDATE_TRANSFER_TO_PLAYER_INV = 3;
    public static final int UPDATE_SETTING = 4;
    public static final int DEPLOY_SLEEPING_BAG = 5;

    private final PlayerMainInvWrapper playerInv;
    private final BackpackWrapper wrapper;
    private final BackpackPanel panel;

    public BackpackSH(PlayerMainInvWrapper playerInv, BackpackWrapper wrapper, BackpackPanel panel) {
        this.playerInv = playerInv;
        this.wrapper = wrapper;
        this.panel = panel;
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        switch (id) {
            case UPDATE_SET_SORT_TYPE:
                setSortType(buf);
                break;

            case UPDATE_SORT_INV:
                sortInventory(buf);
                break;

            case UPDATE_TRANSFER_TO_BACKPACK_INV:
                transferToBackpack(buf);
                break;

            case UPDATE_TRANSFER_TO_PLAYER_INV:
                transferToPlayerInventory(buf);
                break;

            case UPDATE_SETTING:
                updateBackpack(buf);
                break;

            case DEPLOY_SLEEPING_BAG:
                deploySleepingBag();
                break;
            default:
                return;
        }
        wrapper.markDirty();
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == UPDATE_SET_SORT_TYPE || id == UPDATE_SORT_INV
            || id == UPDATE_TRANSFER_TO_BACKPACK_INV
            || id == UPDATE_TRANSFER_TO_PLAYER_INV
            || id == UPDATE_SETTING
            || id == DEPLOY_SLEEPING_BAG) {
            wrapper.syncToServer();
        }
    }

    public void setSortType(PacketBuffer buf) {
        SortType sortType = NetworkUtils.readEnumValue(buf, SortType.class);
        setSortType(sortType);
    }

    public void setSortType(SortType sortType) {
        wrapper.sortType = sortType;
    }

    public void sortInventory(PacketBuffer buf) throws IOException {
        for (int i = 0; i < wrapper.backpackSlots; i++) {
            wrapper.backpackHandler.setStackInSlot(i, buf.readItemStackFromBuffer());
        }
    }

    public void transferToBackpack(boolean transferMatched) {
        BackpackInventoryHelpers.transferPlayerInventoryToBackpack(wrapper, playerInv, transferMatched);
    }

    public void transferToBackpack(PacketBuffer buf) {
        boolean transferMatched = buf.readBoolean();
        BackpackInventoryHelpers.transferPlayerInventoryToBackpack(wrapper, playerInv, transferMatched);
    }

    public void transferToPlayerInventory(boolean transferMatched) {
        BackpackInventoryHelpers.transferBackpackToPlayerInventory(wrapper, playerInv, transferMatched);
    }

    public void transferToPlayerInventory(PacketBuffer buf) {
        boolean transferMatched = buf.readBoolean();
        BackpackInventoryHelpers.transferBackpackToPlayerInventory(wrapper, playerInv, transferMatched);
    }

    public void updateBackpack(PacketBuffer buf) throws IOException {
        boolean lock = buf.readBoolean();
        String playerUuid = buf.readStringFromBuffer(36);
        boolean tab = buf.readBoolean();
        wrapper.lockBackpack = lock;
        wrapper.playerUuid = playerUuid;
        wrapper.keepTab = tab;
    }

    public void deploySleepingBag() {
        EntityPlayer player = getSyncManager().getPlayer();

        World world = player.worldObj;
        TileEntity tile = panel.getTile();
        if (tile != null && world.getTileEntity(tile.xCoord, tile.yCoord, tile.zCoord) instanceof TEBackpack te) {
            if (!te.isSleepingBagDeployed()) {
                int[] can = BlockSleepingBag
                    .canDeploySleepingBag(world, player, tile.xCoord, tile.yCoord, tile.zCoord, true);
                if (can[0] > -1) {
                    if (te.deploySleepingBag(player, world, can[0], can[1], can[2], can[3])) {
                        player.closeScreen();
                    }
                } else if (!world.isRemote) {
                    player.addChatComponentMessage(new ChatComponentTranslation("messages.backpack.cant.bag"));
                }
            } else {
                te.removeSleepingBag(world);
            }
            player.closeScreen();
        } else if (tile == null) {
            int[] can = BlockSleepingBag
                .canDeploySleepingBag(world, player, (int) player.posX, (int) player.posY, (int) player.posZ, false);
            if (can[0] > -1) {
                if (wrapper.deploySleepingBag(player, world, can[0], can[1], can[2], can[3])) {
                    Block portableBag = world.getBlock(can[1], can[2], can[3]);
                    if (portableBag instanceof BlockSleepingBag) {
                        BackpackProperty.get(player)
                            .setSleepingInPortableBag(true);
                        ((BlockSleepingBag) portableBag)
                            .onPortableBlockActivated(world, player, can[1], can[2], can[3]);
                    }
                }
            } else if (!world.isRemote) {
                player.addChatComponentMessage(new ChatComponentTranslation("messages.backpack.cant.bag"));
            }
            player.closeScreen();
        }
    }
}
