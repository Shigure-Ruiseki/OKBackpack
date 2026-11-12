package ruiseki.backpack.client.gui.sync;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import ruiseki.backpack.backpack.TileBackpack;
import ruiseki.backpack.settings.BackpackMainSettingsCategory;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.common.block.BlockSleepingBag;
import ruiseki.okbackpack.common.entity.properties.BackpackProperty;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.StorageSHRegistry;
import ruiseki.okstoragecore.settings.SettingsManager;
import ruiseki.okstoragecore.settings.main.MainSettingsCategory;
import ruiseki.okstoragecore.settings.memory.MemorySettingsCategory;
import ruiseki.okstoragecore.settings.nosort.NoSortSettingsCategory;

public class BackpackSHRegistries {

    public static final StorageSHRegistry.Entry<BackpackSH> REGISTRY = StorageSHRegistry.get(BackpackSH.class);

    public static final ResourceLocation SET_ANOTHER_PLAYER_CAN_OPEN_SETTING = new ResourceLocation(
        Reference.MOD_ID,
        "backpack_set_another_player_can_open_setting");
    public static final ResourceLocation DEPLOY_SLEEPING_BAG = new ResourceLocation(
        Reference.MOD_ID,
        "backpack_deploy_sleeping_bag");

    public static void load() {
        REGISTRY.registerServer(SET_ANOTHER_PLAYER_CAN_OPEN_SETTING, (handler, buf) -> {
            MainSettingsCategory<?> settings = getGlobalSettings(handler.getWrapper());
            SettingsManager.setSettingByMode(
                handler.getPlayer(),
                settings.getPlayerSettingsTagName(),
                settings,
                BackpackMainSettingsCategory.ANOTHER_PLAYER_CAN_OPEN,
                buf.readBoolean(),
                buf.readBoolean());
        });

        REGISTRY.registerServer(DEPLOY_SLEEPING_BAG, (handler, buf) -> { deploySleepingBag(handler); });
    }

    public static void deploySleepingBag(BackpackSH backpackSH) {
        EntityPlayer player = backpackSH.getSyncManager()
            .getPlayer();
        World world = player.worldObj;
        TileEntity tile = backpackSH.getPanel()
            .getTile();
        if (tile != null) {
            BlockPos pos = new BlockPos(tile.xCoord, tile.yCoord, tile.zCoord);
            if (pos.getTileEntity(world) instanceof TileBackpack te) {
                if (!te.isSleepingBagDeployed()) {
                    BlockSleepingBag.SleepingBagResult result = BlockSleepingBag
                        .canDeploySleepingBag(world, player, pos, true);
                    if (result != null) {
                        if (te.deploySleepingBag(player, world, result.footPos(), result.direction())) {
                            player.closeScreen();
                        }
                    } else if (!world.isRemote) {
                        player.addChatComponentMessage(new ChatComponentTranslation("messages.backpack.cant.bag"));
                    }
                } else {
                    te.removeSleepingBag(world);
                }
            }
            player.closeScreen();
            return;
        }

        BlockPos playerPos = new BlockPos(player);
        BlockSleepingBag.SleepingBagResult result = BlockSleepingBag
            .canDeploySleepingBag(world, player, playerPos, false);
        if (result != null) {
            BlockPos deployPos = result.footPos();
            if (backpackSH.getWrapper()
                .deploySleepingBag(player, world, result.direction(), deployPos)) {
                Block portableBag = deployPos.getBlock(world);
                if (portableBag instanceof BlockSleepingBag sleepingBag) {
                    BackpackProperty.get(player)
                        .setSleepingInPortableBag(true);
                    sleepingBag.onPortableBlockActivated(world, player, deployPos);
                }
            }
        } else if (!world.isRemote) {
            player.addChatComponentMessage(new ChatComponentTranslation("messages.backpack.cant.bag"));
        }
        player.closeScreen();
    }

    public static MemorySettingsCategory getMemorySettings(IStorageWrapper wrapper) {
        return wrapper.getSettingsHandler()
            .getTypeCategory(MemorySettingsCategory.class);
    }

    public static NoSortSettingsCategory getNoSortingSettings(IStorageWrapper wrapper) {
        return wrapper.getSettingsHandler()
            .getTypeCategory(NoSortSettingsCategory.class);
    }

    public static MainSettingsCategory<?> getGlobalSettings(IStorageWrapper wrapper) {
        return wrapper.getSettingsHandler()
            .getGlobalSettingsCategory();
    }
}
