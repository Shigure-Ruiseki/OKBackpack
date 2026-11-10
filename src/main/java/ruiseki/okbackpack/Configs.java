package ruiseki.okbackpack;

import ruiseki.backpack.backpack.BlockBackpackTestConfig;
import ruiseki.backpack.capabilities.CapabilityBackpackWrapperConfig;
import ruiseki.backpack.init.item.anvil.ItemAnvilUpgradeConfig;
import ruiseki.backpack.init.item.battery.ItemBatteryUpgradeConfig;
import ruiseki.backpack.init.item.compacting.ItemAdvCompactingUpgradeConfig;
import ruiseki.backpack.init.item.compacting.ItemCompactingUpgradeConfig;
import ruiseki.backpack.init.item.crafting.ItemCraftingUpgradeConfig;
import ruiseki.backpack.init.item.deposit.ItemAdvDepositUpgradeConfig;
import ruiseki.backpack.init.item.deposit.ItemDepositUpgradeConfig;
import ruiseki.backpack.init.item.everlasting.ItemEverlastingUpgradeConfig;
import ruiseki.backpack.init.item.feeding.ItemAdvFeedingUpgradeConfig;
import ruiseki.backpack.init.item.feeding.ItemFeedingUpgradeConfig;
import ruiseki.backpack.init.item.filter.ItemAdvFilterUpgradeConfig;
import ruiseki.backpack.init.item.filter.ItemFilterUpgradeConfig;
import ruiseki.backpack.init.item.inception.ItemInceptionUpgradeConfig;
import ruiseki.backpack.init.item.magnet.ItemAdvMagnetUpgradeConfig;
import ruiseki.backpack.init.item.magnet.ItemMagnetUpgradeConfig;
import ruiseki.backpack.init.item.pickup.ItemAdvPickupUpgradeConfig;
import ruiseki.backpack.init.item.pickup.ItemPickupUpgradeConfig;
import ruiseki.backpack.init.item.pump.ItemAdvPumpUpgradeConfig;
import ruiseki.backpack.init.item.pump.ItemPumpUpgradeConfig;
import ruiseki.backpack.init.item.restock.ItemAdvRestockUpgradeConfig;
import ruiseki.backpack.init.item.restock.ItemRestockUpgradeConfig;
import ruiseki.backpack.init.item.stack.ItemStackDowngradeTier1Config;
import ruiseki.backpack.init.item.stack.ItemStackDowngradeTier2Config;
import ruiseki.backpack.init.item.stack.ItemStackDowngradeTier3Config;
import ruiseki.backpack.init.item.stack.ItemStackUpgradeOmegaConfig;
import ruiseki.backpack.init.item.stack.ItemStackUpgradeStarterConfig;
import ruiseki.backpack.init.item.stack.ItemStackUpgradeTier1Config;
import ruiseki.backpack.init.item.stack.ItemStackUpgradeTier2Config;
import ruiseki.backpack.init.item.stack.ItemStackUpgradeTier3Config;
import ruiseki.backpack.init.item.stack.ItemStackUpgradeTier4Config;
import ruiseki.backpack.init.item.tank.ItemTankUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemBatUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemBlazeUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemBookShelfUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemCactusUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemCakeUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemChickenUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemCowUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemCreeperUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemDragonUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemGhastUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemGlowstoneUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemHayUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemLapisUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemMagmaCubeUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemOcelotUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemQuartzUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemQuiverUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemRainbowUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemRedstoneUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemSlimeUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemSpiderUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemSpongeUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemSquidUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemWitherUpgradeConfig;
import ruiseki.backpack.init.item.traverler.ItemWolfUpgradeConfig;
import ruiseki.backpack.init.item.voiding.ItemAdvVoidUpgradeConfig;
import ruiseki.backpack.init.item.voiding.ItemVoidUpgradeConfig;
import ruiseki.okbackpack.common.block.BlockDiamondBackpackConfig;
import ruiseki.okbackpack.common.block.BlockGoldBackpackConfig;
import ruiseki.okbackpack.common.block.BlockIronBackpackConfig;
import ruiseki.okbackpack.common.block.BlockLeatherBackpackConfig;
import ruiseki.okbackpack.common.block.BlockObsidianBackpackConfig;
import ruiseki.okbackpack.common.block.BlockSleepingBagConfig;
import ruiseki.okbackpack.common.item.ItemUpgradeConfig;
import ruiseki.okbackpack.common.item.arcane.ItemArcaneCraftingUpgradeConfig;
import ruiseki.okbackpack.common.item.energizednode.ItemEnergizedNodeUpgradeConfig;
import ruiseki.okbackpack.common.item.infinity.ItemInfinityUpgradeConfig;
import ruiseki.okbackpack.common.item.infinity.ItemSurvivalInfinityUpgradeConfig;
import ruiseki.okbackpack.common.item.jukebox.ItemAdvancedJukeBoxUpgradeConfig;
import ruiseki.okbackpack.common.item.jukebox.ItemJukeBoxUpgradeConfig;
import ruiseki.okbackpack.common.item.refill.ItemAdvancedRefillUpgradeConfig;
import ruiseki.okbackpack.common.item.refill.ItemRefillUpgradeConfig;
import ruiseki.okbackpack.common.item.smelter.ItemAutoBlastingUpgradeConfig;
import ruiseki.okbackpack.common.item.smelter.ItemAutoSmeltingUpgradeConfig;
import ruiseki.okbackpack.common.item.smelter.ItemAutoSmokingUpgradeConfig;
import ruiseki.okbackpack.common.item.smelter.ItemBlastingUpgradeConfig;
import ruiseki.okbackpack.common.item.smelter.ItemSmeltingUpgradeConfig;
import ruiseki.okbackpack.common.item.smelter.ItemSmokingUpgradeConfig;
import ruiseki.okbackpack.common.item.toolswapper.ItemAdvancedToolSwapperUpgradeConfig;
import ruiseki.okbackpack.common.item.toolswapper.ItemToolSwapperUpgradeConfig;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okcore.config.ConfigHandler;

public class Configs {

    public static void registerBlocks(ConfigHandler configHandler) {
        // Blocks
        configHandler.add(new BlockLeatherBackpackConfig());
        configHandler.add(new BlockIronBackpackConfig());
        configHandler.add(new BlockGoldBackpackConfig());
        configHandler.add(new BlockDiamondBackpackConfig());
        configHandler.add(new BlockObsidianBackpackConfig());
        configHandler.add(new BlockSleepingBagConfig());

        // Items
        configHandler.add(new ItemUpgradeConfig());
        configHandler.add(new ItemInfinityUpgradeConfig());
        configHandler.add(new ItemSurvivalInfinityUpgradeConfig());
        configHandler.add(new ItemJukeBoxUpgradeConfig());
        configHandler.add(new ItemAdvancedJukeBoxUpgradeConfig());
        configHandler.add(new ItemRefillUpgradeConfig());
        configHandler.add(new ItemAdvancedRefillUpgradeConfig());
        configHandler.add(new ItemBlastingUpgradeConfig());
        configHandler.add(new ItemSmeltingUpgradeConfig());
        configHandler.add(new ItemSmokingUpgradeConfig());
        configHandler.add(new ItemAutoBlastingUpgradeConfig());
        configHandler.add(new ItemAutoSmeltingUpgradeConfig());
        configHandler.add(new ItemAutoSmokingUpgradeConfig());
        configHandler.add(new ItemToolSwapperUpgradeConfig());
        configHandler.add(new ItemAdvancedToolSwapperUpgradeConfig());

        if (Mods.Thaumcraft.isModLoaded()) {
            if (GeneralConfig.enableArcaneCraftingUpgrade) {
                configHandler.add(new ItemArcaneCraftingUpgradeConfig());
            }
            configHandler.add(new ItemEnergizedNodeUpgradeConfig());
        }

        configHandler.add(new CapabilityBackpackWrapperConfig());

        configHandler.add(new BlockBackpackTestConfig());

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
