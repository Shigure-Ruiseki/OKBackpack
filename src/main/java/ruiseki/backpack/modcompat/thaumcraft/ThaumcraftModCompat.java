package ruiseki.backpack.modcompat.thaumcraft;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import ruiseki.backpack.modcompat.thaumcraft.client.gui.sync.ThaumcraftSHRegistries;
import ruiseki.backpack.modcompat.thaumcraft.item.ItemArcaneCraftingUpgradeConfig;
import ruiseki.backpack.modcompat.thaumcraft.item.ItemEnergizedNodeUpgradeConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.common.item.ItemUpgradeConfig;
import ruiseki.okcore.config.ConfigHandler;
import ruiseki.okcore.enums.Mods;
import ruiseki.okcore.modcompat.IModCompat;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.config.ConfigBlocks;

public class ThaumcraftModCompat implements IModCompat {

    /**
     * If the modcompat can be used.
     */
    public static boolean canBeUsed = false;

    public ThaumcraftModCompat() {

    }

    @Override
    public String getModID() {
        return Mods.Thaumcraft.modid;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getComment() {
        return "Integration for Thaumcraft upgrades.";
    }

    @Override
    public void onInit(Step initStep) {
        if (initStep == Step.PREINIT) {
            canBeUsed = OKBackpack._instance.getModCompatLoader()
                .shouldLoadModCompat(this);

            ThaumcraftSHRegistries.load();

            ConfigHandler configHandler = OKBackpack._instance.getConfigHandler();
            configHandler.add(new ItemArcaneCraftingUpgradeConfig());
            configHandler.add(new ItemEnergizedNodeUpgradeConfig());
        }

        if (initStep == Step.POSTINIT) {

            // Arcane Crafting Upgrade
            ThaumcraftApi.addArcaneCraftingRecipe(
                "ARCTABLE",
                new ItemStack(ItemArcaneCraftingUpgradeConfig._instance.getInstance()),
                new AspectList().add(Aspect.AIR, 10)
                    .add(Aspect.EARTH, 10)
                    .add(Aspect.FIRE, 10)
                    .add(Aspect.WATER, 10)
                    .add(Aspect.ORDER, 10)
                    .add(Aspect.ENTROPY, 10),
                " A ",
                "IUI",
                " C ",
                'A',
                new ItemStack(ConfigBlocks.blockTable, 1, 15),
                'C',
                new ItemStack(Blocks.chest, 1, 0),
                'I',
                "ingotIron",
                'U',
                new ItemStack(ItemUpgradeConfig._instance.getInstance()));

            // Energized Node Upgrade
            ThaumcraftApi.addArcaneCraftingRecipe(
                "VISPOWER",
                new ItemStack(ItemEnergizedNodeUpgradeConfig._instance.getInstance()),
                new AspectList().add(Aspect.AIR, 50)
                    .add(Aspect.EARTH, 50)
                    .add(Aspect.FIRE, 50)
                    .add(Aspect.WATER, 50)
                    .add(Aspect.ORDER, 50)
                    .add(Aspect.ENTROPY, 50),
                " T ",
                "RUC",
                " S ",
                'T',
                new ItemStack(ConfigBlocks.blockStoneDevice, 1, 11),
                'R',
                new ItemStack(ConfigBlocks.blockMetalDevice, 1, 14),
                'C',
                new ItemStack(ConfigBlocks.blockMetalDevice, 1, 2),
                'S',
                new ItemStack(ConfigBlocks.blockStoneDevice, 1, 10),
                'U',
                new ItemStack(ItemUpgradeConfig._instance.getInstance()));
        }
    }
}
