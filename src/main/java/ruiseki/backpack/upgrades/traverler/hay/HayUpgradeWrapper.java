package ruiseki.backpack.upgrades.traverler.hay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;

import ruiseki.backpack.api.IHarvestDropResponseUpgrade;
import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class HayUpgradeWrapper extends UpgradeWrapperBase<HayUpgradeWrapper, ItemHayUpgrade>
    implements ITravelersUpgrade, IHarvestDropResponseUpgrade {

    private static final float DOUBLE_CROP_CHANCE = 0.40F;
    private static final float GRASS_DROP_CHANCE = 0.15F;

    public HayUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public boolean onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        if (isCropBlock(event.block) && event.world.rand.nextFloat() < DOUBLE_CROP_CHANCE) {
            List<ItemStack> extraDrops = new ArrayList<>();
            for (ItemStack drop : event.drops) {
                if (drop != null) {
                    extraDrops.add(ItemHelpers.copy(drop));
                }
            }
            event.drops.addAll(extraDrops);
        }

        if (event.block == Blocks.tallgrass && event.world.rand.nextFloat() < GRASS_DROP_CHANCE) {
            event.drops.add(getRandomGrassCrop(event.world));
        }
        return false;
    }

    public boolean isCropBlock(Block block) {
        return block instanceof BlockCrops || block instanceof BlockNetherWart
            || block == Blocks.carrots
            || block == Blocks.potatoes;
    }

    public ItemStack getRandomGrassCrop(World world) {
        ItemStack[] candidates = new ItemStack[] { new ItemStack(Items.carrot), new ItemStack(Items.potato),
            new ItemStack(Items.wheat) };
        return candidates[world.rand.nextInt(candidates.length)].copy();
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public boolean hideSettingsTab() {
        return true;
    }
}
