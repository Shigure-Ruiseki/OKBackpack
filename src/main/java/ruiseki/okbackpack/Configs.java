package ruiseki.okbackpack;

import ruiseki.okbackpack.capabilities.CapabilityBackpackWrapperConfig;
import ruiseki.okbackpack.init.block.BlockDiamondBackpackConfig;
import ruiseki.okbackpack.init.block.BlockGoldBackpackConfig;
import ruiseki.okbackpack.init.block.BlockIronBackpackConfig;
import ruiseki.okbackpack.init.block.BlockLeatherBackpackConfig;
import ruiseki.okbackpack.init.block.BlockObsidianBackpackConfig;
import ruiseki.okbackpack.init.block.BlockSleepingBagConfig;
import ruiseki.okbackpack.init.item.ItemUpgradeConfig;
import ruiseki.okbackpack.init.item.anvil.ItemAnvilUpgradeConfig;
import ruiseki.okbackpack.init.item.battery.ItemBatteryUpgradeConfig;
import ruiseki.okbackpack.init.item.compacting.ItemAdvCompactingUpgradeConfig;
import ruiseki.okbackpack.init.item.compacting.ItemCompactingUpgradeConfig;
import ruiseki.okbackpack.init.item.cooking.ItemAutoSmeltingUpgradeConfig;
import ruiseki.okbackpack.init.item.cooking.ItemSmeltingUpgradeConfig;
import ruiseki.okbackpack.init.item.crafting.ItemCraftingUpgradeConfig;
import ruiseki.okbackpack.init.item.deposit.ItemAdvDepositUpgradeConfig;
import ruiseki.okbackpack.init.item.deposit.ItemDepositUpgradeConfig;
import ruiseki.okbackpack.init.item.everlasting.ItemEverlastingUpgradeConfig;
import ruiseki.okbackpack.init.item.feeding.ItemAdvFeedingUpgradeConfig;
import ruiseki.okbackpack.init.item.feeding.ItemFeedingUpgradeConfig;
import ruiseki.okbackpack.init.item.filter.ItemAdvFilterUpgradeConfig;
import ruiseki.okbackpack.init.item.filter.ItemFilterUpgradeConfig;
import ruiseki.okbackpack.init.item.inception.ItemInceptionUpgradeConfig;
import ruiseki.okbackpack.init.item.infinity.ItemInfinityUpgradeConfig;
import ruiseki.okbackpack.init.item.infinity.ItemSurvivalInfinityUpgradeConfig;
import ruiseki.okbackpack.init.item.jukebox.ItemAdvJukeBoxUpgradeConfig;
import ruiseki.okbackpack.init.item.jukebox.ItemJukeBoxUpgradeConfig;
import ruiseki.okbackpack.init.item.magnet.ItemAdvMagnetUpgradeConfig;
import ruiseki.okbackpack.init.item.magnet.ItemMagnetUpgradeConfig;
import ruiseki.okbackpack.init.item.pickup.ItemAdvPickupUpgradeConfig;
import ruiseki.okbackpack.init.item.pickup.ItemPickupUpgradeConfig;
import ruiseki.okbackpack.init.item.pump.ItemAdvPumpUpgradeConfig;
import ruiseki.okbackpack.init.item.pump.ItemPumpUpgradeConfig;
import ruiseki.okbackpack.init.item.refill.ItemAdvRefillUpgradeConfig;
import ruiseki.okbackpack.init.item.refill.ItemRefillUpgradeConfig;
import ruiseki.okbackpack.init.item.restock.ItemAdvRestockUpgradeConfig;
import ruiseki.okbackpack.init.item.restock.ItemRestockUpgradeConfig;
import ruiseki.okbackpack.init.item.stack.ItemStackDowngradeTier1Config;
import ruiseki.okbackpack.init.item.stack.ItemStackDowngradeTier2Config;
import ruiseki.okbackpack.init.item.stack.ItemStackDowngradeTier3Config;
import ruiseki.okbackpack.init.item.stack.ItemStackUpgradeOmegaConfig;
import ruiseki.okbackpack.init.item.stack.ItemStackUpgradeStarterConfig;
import ruiseki.okbackpack.init.item.stack.ItemStackUpgradeTier1Config;
import ruiseki.okbackpack.init.item.stack.ItemStackUpgradeTier2Config;
import ruiseki.okbackpack.init.item.stack.ItemStackUpgradeTier3Config;
import ruiseki.okbackpack.init.item.stack.ItemStackUpgradeTier4Config;
import ruiseki.okbackpack.init.item.tank.ItemTankUpgradeConfig;
import ruiseki.okbackpack.init.item.toolswapper.ItemAdvToolSwapperUpgradeConfig;
import ruiseki.okbackpack.init.item.toolswapper.ItemToolSwapperUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemBatUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemBlazeUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemBookShelfUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemCactusUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemCakeUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemChickenUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemCowUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemCreeperUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemDragonUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemGhastUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemGlowstoneUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemHayUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemLapisUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemMagmaCubeUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemOcelotUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemQuartzUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemQuiverUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemRainbowUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemRedstoneUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemSlimeUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemSpiderUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemSpongeUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemSquidUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemWitherUpgradeConfig;
import ruiseki.okbackpack.init.item.traverler.ItemWolfUpgradeConfig;
import ruiseki.okbackpack.init.item.voiding.ItemAdvVoidUpgradeConfig;
import ruiseki.okbackpack.init.item.voiding.ItemVoidUpgradeConfig;
import ruiseki.okbackpack.init.item.xppump.ItemXpPumpUpgradeConfig;
import ruiseki.okcore.config.ConfigHandler;

public class Configs {

    public static void registerBlocks(ConfigHandler configHandler) {

        // Items
        configHandler.add(new ItemRefillUpgradeConfig());
        configHandler.add(new ItemAdvRefillUpgradeConfig());
        configHandler.add(new ItemToolSwapperUpgradeConfig());
        configHandler.add(new ItemAdvToolSwapperUpgradeConfig());

        // Capabilities
        configHandler.add(new CapabilityBackpackWrapperConfig());

        // Blocks
        configHandler.add(new BlockLeatherBackpackConfig());
        configHandler.add(new BlockIronBackpackConfig());
        configHandler.add(new BlockGoldBackpackConfig());
        configHandler.add(new BlockDiamondBackpackConfig());
        configHandler.add(new BlockObsidianBackpackConfig());
        configHandler.add(new BlockSleepingBagConfig());

        // Items
        configHandler.add(new ItemUpgradeConfig());

        configHandler.add(new ItemStackUpgradeStarterConfig());
        configHandler.add(new ItemStackUpgradeTier1Config());
        configHandler.add(new ItemStackUpgradeTier2Config());
        configHandler.add(new ItemStackUpgradeTier3Config());
        configHandler.add(new ItemStackUpgradeTier4Config());
        configHandler.add(new ItemStackUpgradeOmegaConfig());
        configHandler.add(new ItemStackDowngradeTier1Config());
        configHandler.add(new ItemStackDowngradeTier2Config());
        configHandler.add(new ItemStackDowngradeTier3Config());

        configHandler.add(new ItemCraftingUpgradeConfig());

        configHandler.add(new ItemFeedingUpgradeConfig());
        configHandler.add(new ItemAdvFeedingUpgradeConfig());

        configHandler.add(new ItemBatteryUpgradeConfig());

        configHandler.add(new ItemTankUpgradeConfig());

        configHandler.add(new ItemPickupUpgradeConfig());
        configHandler.add(new ItemAdvPickupUpgradeConfig());

        configHandler.add(new ItemMagnetUpgradeConfig());
        configHandler.add(new ItemAdvMagnetUpgradeConfig());

        configHandler.add(new ItemFilterUpgradeConfig());
        configHandler.add(new ItemAdvFilterUpgradeConfig());

        configHandler.add(new ItemInceptionUpgradeConfig());

        configHandler.add(new ItemEverlastingUpgradeConfig());

        configHandler.add(new ItemVoidUpgradeConfig());
        configHandler.add(new ItemAdvVoidUpgradeConfig());

        configHandler.add(new ItemRestockUpgradeConfig());
        configHandler.add(new ItemAdvRestockUpgradeConfig());

        configHandler.add(new ItemDepositUpgradeConfig());
        configHandler.add(new ItemAdvDepositUpgradeConfig());

        configHandler.add(new ItemAnvilUpgradeConfig());

        configHandler.add(new ItemCompactingUpgradeConfig());
        configHandler.add(new ItemAdvCompactingUpgradeConfig());

        configHandler.add(new ItemPumpUpgradeConfig());
        configHandler.add(new ItemAdvPumpUpgradeConfig());
        configHandler.add(new ItemXpPumpUpgradeConfig());

        configHandler.add(new ItemInfinityUpgradeConfig());
        configHandler.add(new ItemSurvivalInfinityUpgradeConfig());

        configHandler.add(new ItemJukeBoxUpgradeConfig());
        configHandler.add(new ItemAdvJukeBoxUpgradeConfig());

        configHandler.add(new ItemSmeltingUpgradeConfig());
        configHandler.add(new ItemAutoSmeltingUpgradeConfig());

        configHandler.add(new ItemBatUpgradeConfig());
        configHandler.add(new ItemBlazeUpgradeConfig());
        configHandler.add(new ItemBookShelfUpgradeConfig());
        configHandler.add(new ItemCactusUpgradeConfig());
        configHandler.add(new ItemCakeUpgradeConfig());
        configHandler.add(new ItemChickenUpgradeConfig());
        configHandler.add(new ItemCowUpgradeConfig());
        configHandler.add(new ItemCreeperUpgradeConfig());
        configHandler.add(new ItemDragonUpgradeConfig());
        configHandler.add(new ItemGlowstoneUpgradeConfig());
        configHandler.add(new ItemHayUpgradeConfig());
        configHandler.add(new ItemLapisUpgradeConfig());
        configHandler.add(new ItemGhastUpgradeConfig());
        configHandler.add(new ItemMagmaCubeUpgradeConfig());
        configHandler.add(new ItemOcelotUpgradeConfig());
        configHandler.add(new ItemQuartzUpgradeConfig());
        configHandler.add(new ItemQuiverUpgradeConfig());
        configHandler.add(new ItemRainbowUpgradeConfig());
        configHandler.add(new ItemRedstoneUpgradeConfig());
        configHandler.add(new ItemSlimeUpgradeConfig());
        configHandler.add(new ItemSpiderUpgradeConfig());
        configHandler.add(new ItemSpongeUpgradeConfig());
        configHandler.add(new ItemSquidUpgradeConfig());
        configHandler.add(new ItemWitherUpgradeConfig());
        configHandler.add(new ItemWolfUpgradeConfig());
    }
}
