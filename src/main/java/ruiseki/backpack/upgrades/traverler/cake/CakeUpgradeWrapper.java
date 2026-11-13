package ruiseki.backpack.upgrades.traverler.cake;

import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class CakeUpgradeWrapper extends UpgradeWrapperBase<CakeUpgradeWrapper, ItemCakeUpgrade>
    implements ITickableUpgrade, ITravelersUpgrade {

    private static final int INTERVAL = 6000;

    public CakeUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (isInCooldown(world) || !(entity instanceof EntityPlayer player)) return;
        player.getFoodStats()
            .addStats(3, 1.0f);
        player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 200, 0, true));
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
