package ruiseki.backpack.upgrades.traverler.cow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
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

public class CowUpgradeWrapper extends UpgradeWrapperBase<CowUpgradeWrapper, ItemCowUpgrade>
    implements ITickableUpgrade, ITravelersUpgrade {

    private static final int INTERVAL = 12000;

    public CowUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (this.isInCooldown(world) || entity == null) return;
        Collection<PotionEffect> activeEffects = new ArrayList<>(entity.getActivePotionEffects());
        for (PotionEffect effect : activeEffects) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            if (potion != null && potion.isBadEffect) {
                entity.removePotionEffect(effect.getPotionID());
            }
        }
        this.setCooldown(world, INTERVAL);
    }
}
