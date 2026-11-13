package ruiseki.backpack.upgrades.traverler.chicken;

import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class ChickenUpgradeWrapper extends UpgradeWrapperBase<ChickenUpgradeWrapper, ItemChickenUpgrade>
    implements ITickableUpgrade, ITravelersUpgrade {

    private static final int INTERVAL = 6000;

    public ChickenUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (isInCooldown(world)) return;
        storageWrapper.getInventoryHandler()
            .insertItem(new ItemStack(Items.egg), false);
        setCooldown(world, INTERVAL);
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
